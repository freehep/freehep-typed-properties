// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 *
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class BooleanPropertyConverter extends SimpleTypePropertyConverter<Boolean> {
    public String toString(Boolean value) {
        return Boolean.toString(value);
    }

    public Boolean toObject(String value) {
        if (value == null)
            value = "false";
        return Boolean.parseBoolean(value);
    }

}
