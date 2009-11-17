// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.util.Properties;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public abstract class SimpleTypePropertyConverter<T> implements
		PropertyConverter<T> {
	public void store(Properties p, String key, Class<?> type, T value) {
		p.setProperty(key, type.getName() + " " + toString(value));
	}

	public void load(TypedProperties properties, String key, Class<?> type,
			String value) {
		properties.setProperty(key, type, toObject(value));
	}

	public abstract String toString(T value);

	public abstract T toObject(String value);

}
