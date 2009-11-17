// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class IntegerPropertyConverter extends
		SimpleTypePropertyConverter<Integer> {
	@Override
	public String toString(Integer value) {
		return Integer.toString(value);
	}

	@Override
	public Integer toObject(String value) {
		return Integer.parseInt(value);
	}
}
