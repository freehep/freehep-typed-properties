// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class FileInfo {
	private long modifiedTime;
	private List<FileListener> listeners;

	FileInfo(long modifiedTime) {
		this.modifiedTime = modifiedTime;
		listeners = new ArrayList<FileListener>(1);
	}

	void addListener(FileListener fileListener) {
		listeners.add(fileListener);
	}

	int removeListener(FileListener fileListener) {
		listeners.remove(fileListener);
		return listeners.size();
	}

	void check(File file) {
		long newModifiedTime = file.exists() ? file.lastModified() : -1;

		// Check if file has changed
		if (newModifiedTime != modifiedTime) {
			// Notify listeners
			for (FileListener listener : listeners) {
				if (modifiedTime == -1) {
					listener.fileCreated(file);
				} else if (newModifiedTime == -1) {
					listener.fileRemoved(file);
				} else {
					listener.fileChanged(file);
				}
			}
			modifiedTime = newModifiedTime;
		}
	}
}
