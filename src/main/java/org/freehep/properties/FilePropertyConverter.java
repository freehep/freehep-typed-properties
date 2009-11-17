// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.io.File;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class FilePropertyConverter extends SimpleTypePropertyConverter<File> {
	@Override
	public String toString(File value) {
		// store all paths in Unix fashion
		String s = value.getPath().replaceAll("\\" + File.separator, "/");
		return s;
	}

	@Override
	public File toObject(String value) {
		// load all paths in Unix fashion
		String s = value.replaceAll("/", "\\" + File.separator);
		return new File(s);
	}
}
