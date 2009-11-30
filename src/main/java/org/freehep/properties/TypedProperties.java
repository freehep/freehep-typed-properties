// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Matcher;

/**
 * TypedProperties is similar to java.util.Properties but allows for storage of
 * "typed" elements rather than just Strings.
 * <p>
 * Simple elements are stored as:
 * 
 * <pre>
 * key = javatype value
 *      where javatype is for instance java.lang.String, java.lang.Integer etc.
 * </pre>
 * 
 * Complex types (arrays and tables) are also allowed. They are stored as:
 * 
 * <pre>
 * key{subkey} = javatype value
 *      for tables
 * 
 * key[index] = javatype value
 *      for arrays
 * </pre>
 * 
 * Tables can be recursive.
 * <p>
 * Methods are available to set and get properties of different types. The get
 * methods all need a default which needs to be of the requested type, but can
 * be set to null or some other default.
 * <p>
 * There is NO conversion from int to float etc.
 * <p>
 * Defaults for the whole table can be given by a backing TypedProperties table.
 * If a property is not found the default table will be searched. The can be
 * cascaded into many tables.
 * <p>
 * Setting a simple property to a value that is already set in the default, will
 * remove the property from this table. A lookup will still give the value from
 * the default table. Persisting this table will result in this property not
 * being written.
 * <p>
 * TypedProperties is persistent by means of a standard properties file.
 * <p>
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class TypedProperties {

	private boolean readOnly;
	private String name;
	private TypedProperties defaults = null;
	private TypedProperties parent = null;
	private SortedMap<String, Object> properties = new TreeMap<String, Object>();
	private Map<String, Class<?>> types = new HashMap<String, Class<?>>();

	private List<PropertyChangeListener> listeners = new ArrayList<PropertyChangeListener>();

	public static final TypedProperties EMPTY_PROPERTIES = new Empty();

	/**
	 * Creates TypedProperties not associated to a file, for usage inside other
	 * TypedProperties. There are no defaults. This table gets added to the
	 * parent under key.
	 */
	public TypedProperties(TypedProperties parent, String key) {
		this();
		this.parent = parent;
		this.name = key;

		if (parent != null) {
			readOnly = parent.readOnly;
			parent.set(key, this);
		}
	}

	public TypedProperties(TypedProperties defaults, boolean readOnly) {
		this();
		this.defaults = defaults;
		this.readOnly = readOnly;
	}

	public TypedProperties() {
		parent = null;
		defaults = null;
		readOnly = false;
		name = null;
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		listeners.add(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		listeners.remove(l);
	}

	private void firePropertyChangeEvent() {
		PropertyChangeEvent event = new PropertyChangeEvent(this, null, null,
				null);
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(event);
		}
	}

	public void setReadOnly() {
		readOnly = true;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public <T> void set(String key, T value) {
		if (readOnly) {
			throw new UnsupportedOperationException();
		}

		setProperty(key, value.getClass(), value);
	}

	public <T> void set(String key, Class<?> type, T value) {
		if (readOnly) {
			throw new UnsupportedOperationException();
		}

		setProperty(key, type, value);
	}

	protected <T> void setProperty(String key, Class<?> type, T value) {
		// handle key patters as key{subkey}...
		Matcher m = TypedPropertiesConverter.MAP_PATTERN.matcher(key);
		if (m.matches()) {
			String mainKey = m.group(1);
			TypedProperties subProperties = (TypedProperties) properties
					.get(mainKey);
			if (subProperties == null) {
				subProperties = new TypedProperties(this, mainKey);
			}

			subProperties.set(m.group(2) + m.group(3), type, value);
			return;
		}

		Class<?> expectedType = getType(key);
		// special case for Lists
		if ((expectedType != null) && List.class.isAssignableFrom(expectedType)) {
			expectedType = List.class;
		}

		// check type
		if ((expectedType != null) && !expectedType.isAssignableFrom(type)) {
			throw new ClassCastException("Cannot set property " + key
					+ " with type: " + type + ", expected type: "
					+ expectedType);
		}

		T defaultValue = defaults != null ? defaults.get(key, (T) null) : null;
		boolean changed = false;
		if ((value == null) || (value.equals(defaultValue))) {
			changed = properties.containsKey(key);
			properties.remove(key);
			types.remove(key);
		} else {
			changed = true;
			properties.put(key, value);
			types.put(key, type);
		}
		if (changed) {
			firePropertyChangeEvent();
		}
	}

	/**
	 * Looks up a property by key.
	 * <P>
	 * This method is tricky. The key is looked up in the current table and
	 * returned if existing. If defaults exist, the lookup is deferred to
	 * defaults.
	 * <P>
	 * If still unknown, the root is searched if parents exist, and the defaults
	 * defined of the root is searched. The defaults at the root are searched
	 * recursively.
	 * <P>
	 * While searching for the root, the reverse key is stacked, as to be able
	 * to reach the same table in a parallel (defaults) tree. If this table
	 * exist, it is searched with this method. This can again result in its root
	 * and defaults being searched.
	 * <P>
	 * If somewhere along the line a sub-table is missing, this defaults does
	 * obviously not define this key (and table), and so the root's defaults are
	 * used to look up the (total) key again.
	 * <P>
	 * 
	 * @param key
	 * @param defaultValue
	 * @return property for given key
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String key, T defaultValue) {
		// handle key pattern such as key{subkey}...
		Matcher m = TypedPropertiesConverter.MAP_PATTERN.matcher(key);
		if (m.matches()) {
			String mainKey = m.group(1);
			TypedProperties subProperties = (TypedProperties) properties
					.get(mainKey);
			if (subProperties != null) {
				return subProperties.get(m.group(2) + m.group(3), defaultValue);
			}
		} else {
			Object o = properties.get(key);
			if (o != null) {
				// special case, for non-typed files if a default value (type) is given
				if (getType(key).equals(String.class) && defaultValue != null) {
					PropertyConverter<?> converter = converters.get(defaultValue.getClass());
					if ((converter != null) && (converter instanceof SimpleTypePropertyConverter<?>)) {
						return ((SimpleTypePropertyConverter<T>)converter).toObject((String)o);
					}
				}
				return (T)o;
			}
		}

		// Find root, keep track of keys
		Stack<String> keys = new Stack<String>();
		TypedProperties p = this;
		while (p.parent != null) {
			keys.push(p.name);
			p = p.parent;
		}
		TypedProperties root = p;
		// Find parallel(s)
		while (root.defaults != null) {
			p = root.defaults;
			for (int i = keys.size() - 1; i >= 0; i--) {
				String subKey = keys.get(i);
				p = p.get(subKey, (TypedProperties) null);
				if (p == null) {
					break;
				}
			}
			if (p != null) {
				return p.get(key, defaultValue);
			}
			root = root.defaults;
		}
		return defaultValue;
	}

	/**
	 * Return the type (e.g. java.lang.String, java.lang.Integer) for a certain
	 * key.
	 * 
	 * @param key
	 *            property name
	 * @return type of property
	 */
	public Class<?> getType(String key) {
		// handle key pattern such as key{subkey}...
		Matcher m = TypedPropertiesConverter.MAP_PATTERN.matcher(key);
		if (m.matches()) {
			String mainKey = m.group(1);
			TypedProperties subProperties = (TypedProperties) properties
					.get(mainKey);
			if (subProperties != null) {
				return subProperties.getType(m.group(2) + m.group(3));
			}
		} else {
			Class<?> type = types.get(key);
			if (type != null) {
				return type;
			}
		}

		// Find root, keep track of keys
		Stack<String> keys = new Stack<String>();
		TypedProperties p = this;
		while (p.parent != null) {
			keys.push(p.name);
			p = p.parent;
		}
		TypedProperties root = p;
		// Find parallel(s)
		while (root.defaults != null) {
			p = root.defaults;
			for (int i = keys.size() - 1; i >= 0; i--) {
				String subKey = keys.get(i);
				p = p.get(subKey, (TypedProperties) null);
				if (p == null) {
					break;
				}
			}
			if (p != null) {
				return p.getType(key);
			}
			root = root.defaults;
		}
		return null;
	}

	private TypedProperties getRoot(TypedProperties o, Stack<String> keys) {
		return o;
	}

	/**
	 * Store properties into p where parentkey is used to construct
	 * parentkey{key}
	 * 
	 * @param p
	 *            java.util.Properties to store properties into
	 * @param parentKey
	 *            key to use for table
	 */
	protected void store(Properties p, String parentKey) {
		for (String key : properties.keySet()) {
			Object value = properties.get(key);
			key = (parentKey != null) ? parentKey + "{" + key + "}" : key;
			Class<?> type = value.getClass();
			if (value instanceof List<?>) {
				type = List.class;
			}
			store(p, key, type, value);
		}
	}

	@SuppressWarnings("unchecked")
	private <V> void store(Properties p, String key, Class<?> type, V value) {
		PropertyConverter<V> converter = (PropertyConverter<V>) converters
				.get(type);
		if (converter == null) {
			System.err
					.println("Store: No Converter defined for '" + type + "'");
		} else {
			converter.store(p, key, type, value);
		}
	}

	protected void removeAll() {
		properties.clear();
	}

	/**
	 * Get a property (sub)table
	 * 
	 * @param key
	 *            property name
	 * @return property table
	 */
	public TypedProperties get(String key) {
		return get(key, EMPTY_PROPERTIES);
	}

	/**
	 * Bypass readOnly
	 * 
	 * @param key
	 * @param value
	 */
	private void set(String key, TypedProperties value) {
		setProperty(key, value.getClass(), value);
	}

	/**
	 * Set property integer
	 * 
	 * @param key
	 *            property name
	 * @param value
	 *            property integer
	 */
	public void set(String key, int value) {
		set(key, (Integer) value);
	}

	public int get(String key, int defaultValue) {
		return get(key, (Integer) defaultValue);
	}

	/**
	 * Set property float
	 * 
	 * @param key
	 *            property name
	 * @param value
	 *            property float
	 */
	public void set(String key, float value) {
		set(key, (Float) value);
	}

	public float get(String key, float defaultValue) {
		return get(key, (Float) defaultValue);
	}

	/**
	 * Set property boolean
	 * 
	 * @param key
	 *            property name
	 * @param value
	 *            property boolean
	 */
	public void set(String key, boolean value) {
		set(key, (Boolean) value);
	}

	public boolean get(String key, boolean defaultValue) {
		return get(key, (Boolean) defaultValue);
	}

    private static HashMap<Class<?>, PropertyConverter<?>> converters;

    public synchronized static PropertyConverter<?> getConverter(Class<?> type) {
		if (converters == null) {
			converters = new HashMap<Class<?>, PropertyConverter<?>>();
		}
		return converters.get(type);
    }
    
	/**
	 * Register Converter to use for "type" to String and vice-versa
	 * conversions.
	 * 
	 * @param type
	 *            name of the type to store converter for
	 * @param converter
	 *            converter for this type
	 */
	public synchronized static void register(Class<?> type, PropertyConverter<?> converter) {
		if (converters == null) {
			converters = new HashMap<Class<?>, PropertyConverter<?>>();
		}
		converters.put(type, converter);
	}

    static {
		// simple types
		register(String.class, new StringPropertyConverter());
		register(File.class, new FilePropertyConverter());
		register(URL.class, new URLPropertyConverter());
		register(Integer.class, new IntegerPropertyConverter());
		register(Float.class, new FloatPropertyConverter());
		register(Boolean.class, new BooleanPropertyConverter());

		// combined types
		register(TypedProperties.class,
				new TypedPropertiesConverter(converters));
		register(List.class, new ListPropertyConverter(converters));
	}

	private static final class Empty extends TypedProperties {
		private Empty() {
			super(null, "empty");
			setReadOnly();
		}
	}
}
