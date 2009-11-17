// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class FileMonitor {
	private static Map<Long, FileMonitor> monitors = new HashMap<Long, FileMonitor>(
			1);

	private Timer timer;
	private Map<File, FileInfo> fileInfo;

	/**
	 * Create a file monitor instance with specified polling interval.
	 * 
	 * @param pollingInterval
	 *            Polling interval in milli seconds.
	 */
	private FileMonitor(long pollingInterval) {
		fileInfo = new HashMap<File, FileInfo>(1);

		timer = new Timer(true);
		timer.schedule(new FileMonitorNotifier(), 0, pollingInterval);
	}

	public static FileMonitor getInstance(long pollingInterval) {
		FileMonitor monitor = monitors.get(pollingInterval);
		if (monitor == null) {
			monitor = new FileMonitor(pollingInterval);
			monitors.put(pollingInterval, monitor);
		}

		return monitor;
	}

	/**
	 * Stop the file monitor polling.
	 */
	public void stop() {
		timer.cancel();
	}

	/**
	 * Add listener to this file monitor.
	 * 
	 * @param fileListener
	 *            Listener to add.
	 */
	public FileInfo addListener(File file, FileListener fileListener) {
		FileInfo info = fileInfo.get(file);
		if (info == null) {
			info = new FileInfo(file.exists() ? file.lastModified() : -1);
			fileInfo.put(file, info);
		}
		info.addListener(fileListener);
		return info;
	}

	/**
	 * Remove listener from this file monitor.
	 * 
	 * @param file
	 *            file for which to remove the listener
	 * @param fileListener
	 *            Listener to remove, if null all listeners are removed
	 */
	public void removeListener(File file, FileListener fileListener) {
		FileInfo info = fileInfo.get(file);
		if (info != null) {
			int size = fileListener != null ? info.removeListener(fileListener)
					: 0;
			if (size == 0) {
				fileInfo.remove(file);
			}
		}
	}

	/**
	 * This is the timer thread which is executed every n milliseconds according
	 * to the setting of the file monitor. It investigates the file in question
	 * and notify listeners if changed.
	 */
	private class FileMonitorNotifier extends TimerTask {
		@Override
		public void run() {
			for (File file : new ArrayList<File>(fileInfo.keySet())) {
				FileInfo info = fileInfo.get(file);
				info.check(file);
			}
		}
	}
}
