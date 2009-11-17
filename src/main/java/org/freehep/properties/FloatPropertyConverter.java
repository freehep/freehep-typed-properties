// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class FloatPropertyConverter extends SimpleTypePropertyConverter<Float> {
	@Override
	public String toString(Float value) {
		return Float.toString(value);
	}

	@Override
	public Float toObject(String value) {
		return Float.parseFloat(value);
	}
}
