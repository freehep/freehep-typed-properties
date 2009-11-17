// Copyright FreeHEP, 2007-2009
package org.freehep.properties.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.freehep.properties.Assert;
import org.freehep.properties.PersistentTypedProperties;
import org.freehep.properties.TypedProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class NonTypedPropertiesTest {

	protected String testFileName = "NonTypedTestProperties.properties";
	protected TypedProperties properties;

	@Before
	public void readFile() {
		properties = new PersistentTypedProperties(new File(testFileName), true);
	}

	@Test
	public void getType() {
		org.junit.Assert.assertEquals(String.class, properties.getType("Name"));
	}

	@Test
	public void getString() {
		org.junit.Assert.assertEquals("Duns", properties.get("Name", ""));
	}

	@Test
	public void getInteger() {
		org.junit.Assert.assertEquals(123456, properties.get("Telephone", 0));
	}

	@Test
	public void getBoolean() {
		org.junit.Assert.assertTrue(properties.get("Programmer", false));
	}

	@Test
	public void getFloat() {
		org.junit.Assert.assertEquals(42.7, properties.get("Age", 0.0f), 0.001);
	}

	@Test(expected = ClassCastException.class)
	public void getWrongType() {
		properties.get("Name", 20);
	}

	@Test
	public void getWrongString() {
		org.junit.Assert.assertEquals("default", properties.get("unknown", "default"));
	}

	@Test
	public void getWrongInteger() {
		org.junit.Assert.assertEquals(1234, properties.get("unknown", 1234));
	}

	@Test
	public void getWrongBoolean() {
		org.junit.Assert.assertTrue(properties.get("unknown", true));
	}

	@Test
	@Ignore
	public void getFileName() {
		org.junit.Assert.assertEquals(properties.get("FileName", (File) null), new File(
				"TestFileName"));
	}

	@Test
	@Ignore
	public void getURL() {
		try {
			org.junit.Assert.assertEquals(properties.get("URL", (URL) null), new URL(
					"http://java.freehep.org/TypedProperties"));
		} catch (MalformedURLException mfue) {
			// ignored
		}
	}
}
