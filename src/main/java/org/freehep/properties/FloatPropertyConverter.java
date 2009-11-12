// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 *
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class FloatPropertyConverter extends SimpleTypePropertyConverter<Float> {
    public String toString(Float value) {
        return Float.toString(value);
    }

    public Float toObject(String value) {
        return Float.parseFloat(value);
    }
}
