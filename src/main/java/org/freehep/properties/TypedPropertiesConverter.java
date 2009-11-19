// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class TypedPropertiesConverter implements
		PropertyConverter<TypedProperties> {
	static final Pattern mapPattern = Pattern
			.compile("([^\\{]+)\\{([^\\}]+)\\}(.*)");
	private Map<Class<?>, PropertyConverter<?>> converters;

	/**
	 * @param converters
	 */
	public TypedPropertiesConverter(
			Map<Class<?>, PropertyConverter<?>> converters) {
		this.converters = converters;
	}

	public void store(Properties p, String key, Class<?> type,
			TypedProperties value) {
		value.store(p, key);
	}

	public void load(TypedProperties properties, String key, Class<?> type,
			String value) {
		Matcher m = mapPattern.matcher(key);
		if (m.matches()) {
			String mainKey = m.group(1);
			TypedProperties subProperties = properties.get(mainKey,
					(TypedProperties) null);
			if (subProperties == null) {
				subProperties = new TypedProperties(properties, mainKey);
			}

			key = m.group(2);
			if (!m.group(3).equals("")) {
				// FIXME, assumed it is {}{}
				load(subProperties, key + m.group(3), type, value);
			} else {
				PropertyConverter<?> converter = converters.get(type);
				if (converter == null) {
					System.err
							.println("TypedProperties.load: No Converter defined for '"
									+ type + "' of '" + key + "'");
				} else {
					try {
						converter.load(subProperties, key, type, value);
					} catch (Exception e) {
						System.err
								.println("TypedProperties.load: Could not load property '"
										+ key + "' with type '" + type + "'");
					}
				}
			}
		}
	}	
}
