// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.io.File;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public interface FileListener {

	void fileCreated(File file);

	void fileChanged(File file);

	void fileRemoved(File file);
}
