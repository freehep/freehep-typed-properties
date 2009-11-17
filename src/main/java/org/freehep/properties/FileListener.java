// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.io.File;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public interface FileListener {

	public void fileCreated(File file);

	public void fileChanged(File file);

	public void fileRemoved(File file);
}
