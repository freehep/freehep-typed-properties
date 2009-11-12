// Copyright FreeHEP, 2007-2009
package org.freehep.properties.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.freehep.properties.Assert;
import org.freehep.properties.PersistentTypedProperties;
import org.junit.BeforeClass;
import org.junit.Test;




/**
 *
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class SharedPersistentTypedPropertiesTest {
    
    private static PersistentTypedProperties properties1;
    private static PersistentTypedProperties properties2;

    @BeforeClass 
    public static void readFile() {
        String filename = "SharedProperties.properties";
        File setPropertiesFile = new File(filename);
        setPropertiesFile.delete();
        properties1 = new PersistentTypedProperties(setPropertiesFile, null, false, 1000);
        properties2 = new PersistentTypedProperties(setPropertiesFile, null, false, 1000);
    }

    @Test
    public void testSharedBoolean() throws InterruptedException {
        properties1.set("Flag", true);
        while (!properties2.get("Flag", false)) {
            Thread.sleep(500);
        }
    }
    
    @Test
    public void testSharedFloat() throws InterruptedException {
        properties2.set("Float", 0.5f);
        while (properties1.get("Float", 0.7f) > 0.6) {
            Thread.sleep(500);
        }
    }   

    @Test
    public void testSharedList() throws InterruptedException {
        properties1.set("StringList", Arrays.asList(new String[] {"One", "Two", "Three"}));
        while (properties2.get("StringList", null) == null) {
            Thread.sleep(500);
        }
        Assert.assertEquals(properties2.get("StringList", (List<String>)null).get(1), "Two");
    }   
}
