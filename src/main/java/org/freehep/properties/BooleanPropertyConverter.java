// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class BooleanPropertyConverter extends
		SimpleTypePropertyConverter<Boolean> {
	@Override
	public final String toString(Boolean value) {
		return Boolean.toString(value);
	}

	@Override
	public final Boolean toObject(String value) {
		return Boolean.parseBoolean(value != null ? value : "false");
	}

}
