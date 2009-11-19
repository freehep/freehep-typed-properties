// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.util.Properties;

/**
 * Converter interface defined to convert properties to and from Strings for
 * persistency. You can register converters on typeNames so that they are called
 * when a conversion is needed when you store or load a property of a certain
 * type.
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public interface PropertyConverter<T> {
	/**
	 * Store property with key, typename and value in p. You would normally need
	 * to convert value to String and maybe do something to the key.
	 * 
	 * @param p
	 *            java.util.Properties to store property into
	 * @param key
	 *            key to store property under (may be changed)
	 * @param type
	 *            type to store, to be suffixed to value
	 * @param value
	 *            value to store
	 */
	public void store(Properties p, String key, Class<?> type, T value);

	/**
	 * Load key, typeName, value into properties table. You would normally
	 * convert value to typeName's type and then store it under key. If key has
	 * some special meaning you may need to act on it.
	 * 
	 * @param properties
	 *            table to load property into
	 * @param key
	 *            key under which name to load property
	 * @param type
	 *            type to convert value to
	 * @param value
	 *            value to be loaded into table
	 */
	public void load(TypedProperties properties, String key, Class<?> type,
			String value);

}
