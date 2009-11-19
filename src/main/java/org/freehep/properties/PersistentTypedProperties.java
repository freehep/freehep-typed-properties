// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

/**
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class PersistentTypedProperties extends TypedProperties {

	private static final String DIGEST_NAME = "MD5";
	private File propertiesFile;
	private byte[] digest;
	private boolean storeChanges;
	private FileMonitor monitor;
	private FileListener fileListener;
	private FileInfo fileInfo;
	private boolean listenForUpdates;

	public PersistentTypedProperties(File propertiesFile) {
		this(propertiesFile, null, false);
	}

	/**
	 * Creates a persistent TypedProperties from given File if exist.
	 * 
	 * @param propertiesFile
	 *            file to persist properties into
	 */
	public PersistentTypedProperties(File propertiesFile, boolean readOnly) {
		this(propertiesFile, null, readOnly);
	}

	public PersistentTypedProperties(File propertiesFile,
			TypedProperties defaults) {
		this(propertiesFile, defaults, false);
	}

	/**
	 * Creates a persistent TypedProperties from given File if exist, with
	 * defaults.
	 * 
	 * @param propertiesFile
	 *            file to persist properties into
	 * @param defaults
	 *            TypedProperties defaults
	 */
	public PersistentTypedProperties(File propertiesFile,
			TypedProperties defaults, boolean readOnly) {
		this(propertiesFile, defaults, readOnly, 10000);
	}

	public PersistentTypedProperties(File propertiesFile,
			TypedProperties defaults, boolean readOnly, long pollingInterval) {
		super(defaults, readOnly);
		this.propertiesFile = propertiesFile;

		storeChanges = false;
		try {
			load(propertiesFile);
		} catch (IOException e) {
			// ignore non existing files
		} catch (NoSuchAlgorithmException e) {
			System.err
					.println("PersistentTypedProperties: cannot find MD5 digest");
		}
		storeChanges = true;

		addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					if (storeChanges) {
						store();
					}
				} catch (IOException e) {
					System.err.println("Could not persist "
							+ PersistentTypedProperties.this.propertiesFile);
					System.err.println(e);
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					System.err
							.println("PersistentTypedProperties: cannot find MD5 digest");
				}
			}
		});

		monitor = FileMonitor.getInstance(pollingInterval);
		fileListener = new FileListener() {
			private boolean changed(File file) {
				boolean loaded = false;
				try {
					storeChanges = false;
					loaded = load(file);
					storeChanges = true;
				} catch (IOException e) {
					System.err.println("Could not (re)load " + file);
				} catch (NoSuchAlgorithmException e) {
					System.err.println("Could not (re)load " + file);
				}
				return loaded;
			}

			public void fileCreated(File file) {
				if (!listenForUpdates) {
					return;
				}
				if (changed(file)) {
					System.err.println(file + " created, loaded");
				}
			}

			public void fileChanged(File file) {
				if (!listenForUpdates) {
					return;
				}
				if (changed(file)) {
					System.err.println(file + " changed, reloaded");
				}
			}

			public void fileRemoved(File file) {
				if (!listenForUpdates) {
					return;
				}
				System.err.println(getClass()
						+ " unexpected error 'fileRemoved' " + file);
			}
		};
		listenForUpdates = true;
		fileInfo = monitor.addListener(propertiesFile, fileListener);
	}

	public File getFile() {
		return propertiesFile;
	}

	@Override
	protected void finalize() throws Throwable {
		monitor.removeListener(propertiesFile, fileListener);
		super.finalize();
	}

	private boolean load(File file) throws IOException,
			NoSuchAlgorithmException {
		Properties p = new Properties();
		MessageDigest messageDigest = MessageDigest.getInstance(DIGEST_NAME);
		FileInputStream fis = new FileInputStream(file);
		FileChannel channel = fis.getChannel();
		FileLock lock = channel.lock(0L, Long.MAX_VALUE, true);
		DigestInputStream dis = new DigestInputStream(fis, messageDigest);
		try {
			p.load(dis);
		} finally {
			lock.release();
			dis.close();
			fis.close();
		}
		byte[] newDigest = messageDigest.digest();
		if (Arrays.equals(newDigest, digest)) {
			return false;
		}

		digest = newDigest;

		// clear all properties, as removed ones need to be referring to the
		// defaults
		removeAll();

		for (Iterator<Entry<Object, Object>> it = p.entrySet().iterator(); it
				.hasNext();) {
			Entry<Object, Object> entry = it.next();
			String key = (String) entry.getKey();
			String fullValue = (String) entry.getValue();

			String[] typeValue = fullValue.split(" ", 2);
			String typeName;
			String value;
			switch (typeValue.length) {
			case 0:
				typeName = String.class.getName();
				value = "";
				break;
			case 1:
				if (typeValue[0].equals(String.class.getName())) {
					// empty typed definition (eg Address=java.lang.String)
					typeName = String.class.getName();
					value = "";
				} else {
					typeName = guessType(typeValue[0]);
					value = typeValue[0];
				}
				break;
			default:
				typeName = typeValue[0];
				value = typeValue[1];
				break;
			}

			Class<?> type = String.class;
			try {
				type = Class.forName(typeName);
			} catch (ClassNotFoundException e) {
				// ignore
				System.err.println(e);
			}

			Class<?> lookupType = type;
			for (int i = 0; i < key.length(); i++) {
				if (key.charAt(i) == '{') {
					lookupType = TypedProperties.class;
					break;
				} else if (key.charAt(i) == '[') {
					lookupType = List.class;
					break;
				}
			}
			PropertyConverter<?> converter = converters.get(lookupType);
			if (converter == null) {
				System.err
						.println("PersistentTypedProperties.load: No Converter defined for '"
								+ lookupType + "' for '" + key + "'");
			} else {
				try {
					converter.load(this, key, type, value);
				} catch (Exception e) {
					System.err
							.println("PersistentTypedProperties.load: Could not load property '"
									+ key
									+ "' with type '"
									+ lookupType.getName() + "'");
					System.err.println(e);
				}
			}
		}
		return true;
	}

	private String guessType(String value) {
		try {
			Integer.parseInt(value);
			return Integer.class.getName();
		} catch (NumberFormatException nfe) {
		}

		try {
			Float.parseFloat(value);
			return Float.class.getName();
		} catch (NumberFormatException nfe) {
		}

		if (value.equalsIgnoreCase("true")
				|| value.equalsIgnoreCase("false")) {
			return Boolean.class.getName();
		}

		return String.class.getName();
	}

	/**
	 * Persist into given Writer.
	 * 
	 * @param writer
	 *            writer to write property file to
	 * @param comments
	 *            comments to write into the file header
	 * @throws IOException
	 *             thrown if file cannot be written
	 * @throws NoSuchAlgorithmException
	 */
	private void store(File file, String comments) throws IOException,
			NoSuchAlgorithmException {
		if (isReadOnly()) {
			throw new UnsupportedOperationException();
		}

		Properties p = new Properties() {
			private static final long serialVersionUID = 4112578634029874840L;

			/**
			 * make write alphabetic
			 */
			@Override
			public synchronized Enumeration<Object> keys() {
				SortedSet<Object> set = new TreeSet<Object>();
				set.addAll(Collections.list(super.keys()));
				return Collections.enumeration(set);
			}
		};

		store(p, null);

		File dir = file.getParentFile();
		if (dir != null) {
			dir.mkdirs();
		}

		FileOutputStream fos = null;
		DigestOutputStream dos = null;
		FileLock lock = null;
		MessageDigest messageDigest = null;
		try {
			fos = new FileOutputStream(file);
			FileChannel channel = fos.getChannel();
			lock = channel.lock();
			messageDigest = MessageDigest.getInstance(DIGEST_NAME);
			dos = new DigestOutputStream(fos, messageDigest);
			p.store(dos, comments);
		} finally {
			if (lock != null) {
				lock.release();
			}
			if (dos != null) {
				dos.close();
			}
			if (fos != null) {
				fos.close();
			}
			if (messageDigest != null) {
				digest = messageDigest.digest();
			}
		}
	}

	/**
	 * Persist into File given at construction.
	 * 
	 * @throws IOException
	 *             thrown if cannot be persisted
	 * @throws NoSuchAlgorithmException
	 */
	private void store() throws IOException, NoSuchAlgorithmException {
		if (isReadOnly()) {
			throw new UnsupportedOperationException();
		}
		listenForUpdates = false;
		store(propertiesFile, "");
		listenForUpdates = true;
	}

}
