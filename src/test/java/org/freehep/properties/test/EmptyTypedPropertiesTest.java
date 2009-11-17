// Copyright FreeHEP, 2007-2009
package org.freehep.properties.test;

import java.io.File;

import org.freehep.properties.PersistentTypedProperties;
import org.freehep.properties.TypedProperties;
import org.junit.Before;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class EmptyTypedPropertiesTest extends ReadOnlyTypedPropertiesTest {

	@Override
	@Before
	public void readFile() {
		TypedProperties readOnlyProperties = new PersistentTypedProperties(
				new File(testFileName), true);
		properties = new TypedProperties(readOnlyProperties, true);
	}
}
