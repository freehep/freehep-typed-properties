// Copyright FreeHEP, 2007-2009
package org.freehep.properties;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class URLPropertyConverter extends SimpleTypePropertyConverter<URL> {

	@Override
	public String toString(URL value) {
		return value.toExternalForm();
	}

	@Override
	public URL toObject(String value) {
		try {
			return new URL(value);
		} catch (MalformedURLException mfue) {
			throw new RuntimeException(mfue);
		}
	}
}
