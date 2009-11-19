// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class ListPropertyConverter<T> implements PropertyConverter<List<T>> {
	private static final Pattern ARRAY_PATTERN = Pattern
			.compile("([^\\[]+)\\[(\\d+)\\](.*)");
	private Map<Class<?>, PropertyConverter<T>> converters;

	public ListPropertyConverter(Map<Class<?>, PropertyConverter<T>> converters) {
		this.converters = converters;
	}

	public void store(Properties p, String key, Class<?> type, List<T> list) {
		for (int i = 0; i < list.size(); i++) {
			T entry = list.get(i);
			Class<?> entryType = entry.getClass();
			PropertyConverter<T> converter = converters.get(entryType);
			if (converter == null) {
				System.err.println("List.store: No Converter defined for '"
						+ entryType + "' of '" + key + "'");
			} else {
				converter.store(p, key + "[" + i + "]", entryType, entry);
			}
		}
	}

	public void load(TypedProperties properties, String key,
			Class<?> entryType, String value) {
		Matcher m = ARRAY_PATTERN.matcher(key);
		if (m.matches()) {
			String mainKey = m.group(1);
			List<Object> list = properties.get(mainKey, (List<Object>) null);
			if (list == null) {
				list = new ArrayList<Object>();
				properties.setProperty(mainKey, List.class, list);
			}

			int index = Integer.parseInt(m.group(2));
			String entryKey = m.group(3);
			PropertyConverter<?> converter = converters.get(entryType);
			if (converter == null) {
				System.err.println("List.load: No Converter defined for '"
						+ entryType + "' of '" + key + "'");
			} else if (converter instanceof SimpleTypePropertyConverter) {
				SimpleTypePropertyConverter<?> simpleTypePropertyConverter = (SimpleTypePropertyConverter<?>) converter;
				while (list.size() <= index) {
					list.add(null);
				}
				list.set(index, simpleTypePropertyConverter.toObject(value));
			} else {
				try {
					converter.load(properties, entryKey, entryType, value);
				} catch (Exception e) {
					System.err.println("List.load: Could not load property '"
							+ entryKey + "' with type '" + entryType + "'");
				}
			}
		}
	}

}
