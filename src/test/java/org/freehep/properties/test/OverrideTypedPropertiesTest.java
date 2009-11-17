// Copyright FreeHEP, 2007-2009
package org.freehep.properties.test;

import java.io.File;
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
public class OverrideTypedPropertiesTest {

	private String testFileName = "TestProperties.properties";
	private TypedProperties properties;

	@Before
	public void readFile() {
		File overridePropertiesFile = new File("OverrideProperties.properties");
		overridePropertiesFile.delete();
		TypedProperties readOnlyProperties = new PersistentTypedProperties(
				new File(testFileName), true);
		TypedProperties emptyProperties = new TypedProperties(
				readOnlyProperties, true);
		properties = new PersistentTypedProperties(overridePropertiesFile,
				emptyProperties);

		// override some values
		properties.set("Name", "Victor");
		properties.set("valid", true);

		properties.set("author{tel}", 2201);
		properties.set("author{valid}", true);

		properties.set("table{2}{valid}", 0.2f);

		properties.set("table{3}{name}", "testtable3");

		properties.set("FileName", new File("Path\\AnotherFileName"));
	}

	@Test
	public void getType() {
		org.junit.Assert.assertEquals(String.class, properties.getType("Name"));
	}

	@Test
	public void getString() {
		org.junit.Assert.assertEquals("Victor", properties.get("Name", ""));
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
	public void getAnotherBoolean() {
		org.junit.Assert.assertTrue(properties.get("valid", false));
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
		org.junit.Assert.assertEquals(2201, properties.get("author{tel}", 0));
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
		org.junit.Assert.assertEquals(2201, table.get("tel", 0));
		org.junit.Assert.assertTrue(table.get("valid", false));
	}

	@Test
	public void getWrongTable() {
		TypedProperties table = properties.get("unknown");
		org.junit.Assert.assertEquals(TypedProperties.EMPTY_PROPERTIES, table);

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
		org.junit.Assert.assertEquals(0.4f, properties.get("table{1}{valid}", 0.0f),
				0.0001f);
	}

	@Test
	public void getTableTableWrongString() {
		org.junit.Assert.assertEquals("default", properties.get("table{1}{unknown}",
				"default"));
	}

	@Test
	public void getTableTableWrongInteger() {
		org.junit.Assert.assertEquals(1234, properties.get("table{1}{unknown}", 1234));
	}

	@Test
	public void getTableTableWrongFloat() {
		org.junit.Assert.assertEquals(1.234f,
				properties.get("table{1}{unknown}", 1.234f), 0.0001f);
	}

	@Test
	public void getTableTableContent() {
		TypedProperties table = properties.get("table{2}");
		org.junit.Assert.assertNotNull(table);
		org.junit.Assert.assertEquals("testtable2", table.get("name", ""));
		org.junit.Assert.assertEquals(5, table.get("seqNo", 0));
		org.junit.Assert.assertEquals(0.2f, table.get("valid", 0.0f), 0.0001f);
	}

	@Test
	public void getTableWrongTable() {
		TypedProperties table = properties.get("table{unknown}");
		org.junit.Assert.assertEquals(TypedProperties.EMPTY_PROPERTIES, table);
	}

	@Test(expected = ClassCastException.class)
	public void setWrongType() {
		properties.set("Name", 20);
	}
}
