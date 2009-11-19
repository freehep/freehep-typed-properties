// Copyright FreeHEP, 2007-2009
package org.freehep.properties.test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.freehep.properties.Assert;
import org.freehep.properties.PersistentTypedProperties;
import org.freehep.properties.TypedProperties;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class ReadOnlyTypedPropertiesTest {

	protected String testFileName = "TestProperties.properties";
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

	@Test(expected = NumberFormatException.class)
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

	@Test(expected = UnsupportedOperationException.class)
	public void setString() {
		properties.set("Name", "READONLY");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setInteger() {
		properties.set("Telephone", 007);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setBoolean() {
		properties.set("Programmer", false);
	}

	@Test
	public void getStringList() {
		org.junit.Assert.assertEquals(Arrays
				.asList(new String[] { "One", "Two", "Three" }), properties
				.get("StringList", (List<String>) null));
	}

	@Test
	public void getBooleanList() {
		org.junit.Assert.assertEquals(Arrays.asList(new Boolean[] { true, false, true }),
				properties.get("BooleanList", (List<Boolean>) null));
	}

	@Test
	public void getIntegerList() {
		org.junit.Assert.assertEquals(Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6 }),
				properties.get("IntegerList", (List<Integer>) null));
	}

	@Test
	public void getFloatList() {
		org.junit.Assert.assertEquals(Arrays.asList(new Float[] { 1.2f, 2.3f, 3.4f, 4.5f,
				5.6f, 6.7f }), properties.get("FloatList", (List<Float>) null));
	}

	@Test
	public void getArrayUnknown() {
		org.junit.Assert.assertArrayEquals(new int[] { 123, 456 }, properties.get(
				"UnknownArray", new int[] { 123, 456 }));
	}

	@Test
	public void getTableString() {
		org.junit.Assert.assertEquals("Tony", properties.get("author{name}", ""));
	}

	@Test
	public void getTableInteger() {
		org.junit.Assert.assertEquals(9624, properties.get("author{tel}", 0));
	}

	@Test
	public void getTableWrongString() {
		org.junit.Assert.assertEquals("default", properties.get("author{unknown}",
				"default"));
	}

	@Test
	public void getTableWrongInteger() {
		org.junit.Assert.assertEquals(1234, properties.get("author{unknown}", 1234));
	}

	@Test
	public void getTableContent() {
		TypedProperties table = properties.get("author");
		org.junit.Assert.assertNotNull(table);
		org.junit.Assert.assertEquals("Tony", table.get("name", ""));
		org.junit.Assert.assertEquals(9624, table.get("tel", 0));
	}

	@Test
	public void getWrongTable() {
		TypedProperties table = properties.get("unknown");
		org.junit.Assert.assertEquals(TypedProperties.EMPTY_PROPERTIES, table);

	}

	@Test(expected = UnsupportedOperationException.class)
	public void setTableContent() {
		TypedProperties table = properties.get("author");
		org.junit.Assert.assertNotNull(table);
		table.set("name", "READONLY");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setTableString() {
		properties.set("author{name}", "Duns");
	}

	@Test
	public void getTableTableString() {
		org.junit.Assert.assertEquals("testtable1", properties.get("table{1}{name}", ""));
	}

	@Test
	public void getTableTableInteger() {
		org.junit.Assert.assertEquals(1, properties.get("table{1}{seqNo}", 0));
	}

	@Test
	public void getTableTableFloat() {
		Assert.assertEquals(0.4f, properties.get("table{1}{valid}", 0.0f),
				0.0001f);
	}

	@Test
	public void getTableTableWrongString() {
		Assert.assertEquals("default", properties.get("table{1}{unknown}",
				"default"));
	}

	@Test
	public void getTableTableWrongInteger() {
		Assert.assertEquals(1234, properties.get("table{1}{unknown}", 1234));
	}

	@Test
	public void getTableTableWrongFloat() {
		Assert.assertEquals(1.234f,
				properties.get("table{1}{unknown}", 1.234f), 0.0001f);
	}

	@Test
	public void getTableTableContent() {
		TypedProperties table = properties.get("table{2}");
		Assert.assertNotNull(table);
		Assert.assertEquals("testtable2", table.get("name", ""));
		Assert.assertEquals(5, table.get("seqNo", 0));
		Assert.assertEquals(0.3f, table.get("valid", 0.0f), 0.0001f);
	}

	@Test
	public void getTableWrongTable() {
		TypedProperties table = properties.get("table{unknown}");
		Assert.assertEquals(TypedProperties.EMPTY_PROPERTIES, table);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setTableTableString() {
		properties.set("table{1}{name}", "testtable3");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void setTableTableContent() {
		TypedProperties table = properties.get("table{2}");
		Assert.assertNotNull(table);
		table.set("name", "READONLY");
	}

	@Test
	public void getFileName() {
		Assert.assertEquals(new File("TestFileName"), properties.get(
				"FileName", (File) null));
	}

	@Test
	public void getFileList() {
		Assert.assertEquals(Arrays.asList(new File[] {
				new File("SimpleName.ext"), new File("SimpleName2.ext"),
				new File("SimpleName3.ext") }), properties.get("FileList",
				(List<File>) null));
	}

	@Test
	public void getURL() {
		try {
			Assert.assertEquals(properties.get("URL", (URL) null), new URL(
					"http://java.freehep.org/TypedProperties"));
		} catch (MalformedURLException mfue) {
			// ignored
		}
	}

	@Test
	public void getEmptyString() {
		Assert.assertEquals("", properties.get("Address", "none"));
	}

}
