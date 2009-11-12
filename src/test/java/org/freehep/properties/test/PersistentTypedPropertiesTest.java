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
public class PersistentTypedPropertiesTest {

    private String testFileName = "TestProperties.properties";
    private TypedProperties properties;
    
    @Before 
    public void readFile() {
        File setPropertiesFile = new File("SetProperties.properties");
        setPropertiesFile.delete();
        TypedProperties readOnlyProperties = new PersistentTypedProperties(new File(testFileName), true); 
        TypedProperties emptyProperties = new TypedProperties(readOnlyProperties, true);
        properties = new PersistentTypedProperties(setPropertiesFile, emptyProperties);
    }
    
    @Test
    public void getType() {
        Assert.assertEquals(String.class, properties.getType("Name"));
    }
    
    @Test
    public void getString() {
        Assert.assertEquals("Duns", properties.get("Name", ""));
    }
    
    @Test
    public void getInteger() {
        Assert.assertEquals(123456, properties.get("Telephone", 0));
    }
    
    @Test
    public void getBoolean() {
        Assert.assertTrue(properties.get("Programmer", false));
    }
        
    @Test(expected=ClassCastException.class)
    public void getWrongType() {
        properties.get("Name", 20);
    }
        
    @Test
    public void getWrongString() {
        Assert.assertEquals("default", properties.get("unknown", "default"));
    }

    @Test
    public void getWrongInteger() {
        Assert.assertEquals(1234, properties.get("unknown", 1234));
    }

    @Test
    public void getWrongBoolean() {
        Assert.assertTrue(properties.get("unknown", true));
    }
 
    @Test
    public void setString() {
        properties.set("Name","Max");
        Assert.assertEquals("Max", properties.get("Name", ""));
    }
    
    @Test
    public void setInteger() {
        properties.set("Telephone", 007);
        Assert.assertEquals(007, properties.get("Telephone", 1234));
    }
    
    @Test
    public void setBoolean() {
        properties.set("Programmer",false);
        Assert.assertFalse(properties.get("Programmer", true));
    }
    
    @Test
    public void getStringListType() {
        Assert.assertEquals(List.class, properties.getType("StringList"));
    }
    
    @Test 
    public void getStringList() {
        Assert.assertEquals(Arrays.asList(new String[] {"One", "Two", "Three"}), properties.get("StringList", (List<String>)null));
    }
    
    @Test 
    public void getBooleanList() {
        Assert.assertEquals(Arrays.asList(new Boolean[] {true, false, true}), properties.get("BooleanList", (List<Boolean>)null));       
    }

    @Test 
    public void getIntegerList() {
        Assert.assertEquals(Arrays.asList(new Integer[] {1, 2, 3, 4, 5, 6}), properties.get("IntegerList", (List<Integer>)null));
    }
    
    @Test 
    public void getFloatList() {
        Assert.assertEquals(Arrays.asList(new Float[] {1.2f, 2.3f, 3.4f, 4.5f, 5.6f, 6.7f}), properties.get("FloatList", (List<Float>)null));
    }
    
    @Test 
    public void getArrayUnknown() {
        Assert.assertArrayEquals(new int[] {123, 456}, properties.get("UnknownArray", new int[] {123, 456}));
    }
    
    @Test
    public void getTableStringType() {
        Assert.assertEquals(String.class, properties.getType("author{name}"));
    }
    
    @Test
    public void getTableString() {
        Assert.assertEquals("Tony", properties.get("author{name}", ""));
    }
    
    @Test
    public void getTableInteger() {
        Assert.assertEquals(9624, properties.get("author{tel}", 0));
    }
    
    @Test
    public void getTableWrongString() {
        Assert.assertEquals("default", properties.get("author{unknown}", "default"));
    }
    
    @Test
    public void getTableWrongInteger() {
        Assert.assertEquals(1234, properties.get("author{unknown}", 1234));
    }    
    
    @Test
    public void getTableContent() {
        TypedProperties table = properties.get("author");
        Assert.assertNotNull(table);
        Assert.assertEquals("Tony", table.get("name", ""));
        Assert.assertEquals(9624, table.get("tel", 0));       
    }
    
    @Test
    public void getWrongTable() {
        TypedProperties table = properties.get("unknown");
        Assert.assertEquals(TypedProperties.EMPTY_PROPERTIES, table);
        
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void setTableContent() {
        TypedProperties table = properties.get("author");
        Assert.assertNotNull(table);
        table.set("name", "READONLY");
    }
 
    @Test
    public void setTableString() {
        properties.set("author{name}", "Duns");
    }    
    
    @Test
    public void getTableTableString() {
        Assert.assertEquals("testtable1", properties.get("table{1}{name}", ""));
    }
    
    @Test
    public void getTableTableInteger() {
        Assert.assertEquals(1, properties.get("table{1}{seqNo}", 0));
    }
    
    @Test
    public void getTableTableFloat() {
        Assert.assertEquals(0.4f, properties.get("table{1}{valid}", 0.0f), 0.0001f);
    }
    
    @Test
    public void getTableTableWrongString() {
        Assert.assertEquals("default", properties.get("table{1}{unknown}", "default"));
    }
    
    @Test
    public void getTableTableWrongInteger() {
        Assert.assertEquals(1234, properties.get("table{1}{unknown}", 1234));
    }
    
    @Test
    public void getTableTableWrongFloat() {
        Assert.assertEquals(1.234f, properties.get("table{1}{unknown}", 1.234f), 0.0001f);
    }
 
    @Test
    public void setTableTableString() {
        properties.set("table{1}{name}", "testtable3");
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
    
    @Test(expected=UnsupportedOperationException.class)
    public void setTableTableContent() {
        TypedProperties table = properties.get("table{2}");
        Assert.assertNotNull(table);
        table.set("name", "READONLY");
    }
    
    @Test(expected=ClassCastException.class)
    public void setWrongTypeArray() {
        properties.set("IntegerList", new float[] { 1.0f, 2.0f });
    }
    
    @Test
    public void setFloatList() {
        List<Float> list = Arrays.asList(new Float[] { 1.0f, 2.0f });
        properties.set("FloatList", list); 
        Assert.assertEquals(list, properties.get("FloatList", (List<Float>)null));
    }
}
