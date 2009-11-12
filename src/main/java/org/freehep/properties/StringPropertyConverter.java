// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 *
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class StringPropertyConverter extends SimpleTypePropertyConverter<String> {

    public String toString(String value) {
        return value.toString();
    }

    public String toObject(String value) {
        return value;
    }
}
