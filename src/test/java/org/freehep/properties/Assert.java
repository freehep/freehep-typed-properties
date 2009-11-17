// Copyright FreeHEP 2007-2009
package org.freehep.properties;

import java.lang.reflect.Array;

import org.junit.internal.ArrayComparisonFailure;

/**
 * 
 * @author Mark Donszelmann (Mark.Donszelmann@gmail.com)
 */
public class Assert extends org.junit.Assert {

	private Assert() {
		// static only
	}

	/**
	 * Compare two float arrays using L2-norm with an epsilon tolerance for
	 * equality.
	 */
	// FIXME, use this one in the float compare
	private static boolean compare(float[] reference, float[] data,
			float epsilon) {
		assert (epsilon >= 0);

		float error = 0;
		float ref = 0;

		for (int i = 0; i < data.length; ++i) {
			float diff = reference[i] - data[i];
			if (i < 10) {
				System.err.println(reference[i] + " " + data[i] + " " + diff);
			}
			error += diff * diff;
			ref += reference[i] * reference[i];
		}

		float normRef = (float) Math.sqrt(ref);
		if (Math.abs(ref) < 1e-7) {
			System.err.println("ERROR, reference l2-norm is 0 " + ref);
			return false;
		}
		float normError = (float) Math.sqrt(error);
		error = normError / normRef;
		boolean result = error < epsilon;
		if (!result) {
			System.err.println("ERROR, l2-norm error " + error
					+ " is greater than epsilon " + epsilon);
		}
		return result;
	}

	/**
	 * Asserts that two object arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message. If
	 * <code>expecteds</code> and <code>actuals</code> are <code>null</code>,
	 * they are considered equal.
	 * 
	 * @param message
	 *            the identifying message or <code>null</code> for the
	 *            {@link AssertionError}
	 * @param expecteds
	 *            Object array or array of arrays (multi-dimensional array) with
	 *            expected values.
	 * @param actuals
	 *            Object array or array of arrays (multi-dimensional array) with
	 *            actual values
	 */
	private static void internalArrayEquals(String message, Object expecteds,
			Object actuals, double delta) throws ArrayComparisonFailure {
		if (expecteds == actuals) {
			return;
		}
		String header = message == null ? "" : message + ": ";
		if (expecteds == null) {
			fail(header + "expected array was null");
		}
		if (actuals == null) {
			fail(header + "actual array was null");
		}
		int actualsLength = Array.getLength(actuals);
		int expectedsLength = Array.getLength(expecteds);
		if (actualsLength != expectedsLength) {
			fail(header + "array lengths differed, expected.length="
					+ expectedsLength + " actual.length=" + actualsLength);
		}

		for (int i = 0; i < expectedsLength; i++) {
			Object expected = Array.get(expecteds, i);
			Object actual = Array.get(actuals, i);
			if (isArray(expected) && isArray(actual)) {
				try {
					internalArrayEquals(message, expected, actual, delta);
				} catch (ArrayComparisonFailure e) {
					e.addDimension(i);
					throw e;
				}
			} else {
				try {
					if (expected instanceof Number && actual instanceof Number) {
						assertEquals(((Number) expected).doubleValue(),
								((Number) actual).doubleValue(), delta);
					} else {
						assertEquals(expected, actual);
					}
				} catch (AssertionError e) {
					throw new ArrayComparisonFailure(header, e, i);
				}
			}
		}
	}

	private static boolean isArray(Object expected) {
		return expected != null && expected.getClass().isArray();
	}

	private static void internalArrayEquals(String message, Object expecteds,
			Object actuals) throws ArrayComparisonFailure {
		if (expecteds == actuals) {
			return;
		}
		String header = message == null ? "" : message + ": ";
		if (expecteds == null) {
			fail(header + "expected array was null");
		}
		if (actuals == null) {
			fail(header + "actual array was null");
		}
		int actualsLength = Array.getLength(actuals);
		int expectedsLength = Array.getLength(expecteds);
		if (actualsLength != expectedsLength) {
			fail(header + "array lengths differed, expected.length="
					+ expectedsLength + " actual.length=" + actualsLength);
		}

		for (int i = 0; i < expectedsLength; i++) {
			Object expected = Array.get(expecteds, i);
			Object actual = Array.get(actuals, i);
			if (isArray(expected) && isArray(actual)) {
				try {
					internalArrayEquals(message, expected, actual);
				} catch (ArrayComparisonFailure e) {
					e.addDimension(i);
					throw e;
				}
			} else {
				try {
					assertEquals(expected, actual);
				} catch (AssertionError e) {
					throw new ArrayComparisonFailure(header, e, i);
				}
			}
		}
	}

	// NEW METHODS
	public static void assertArrayEquals(float[] expecteds, float[] actuals,
			float delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(String message, float[] expecteds,
			float[] actuals, float delta) {
		internalArrayEquals(message, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(double[] expecteds, double[] actuals,
			double delta) {
		assertArrayEquals(null, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(String message, double[] expecteds,
			double[] actuals, double delta) {
		internalArrayEquals(message, expecteds, actuals, delta);
	}

	public static void assertArrayEquals(boolean[] expecteds, boolean[] actuals) {
		assertArrayEquals(null, expecteds, actuals);
	}

	private static void assertArrayEquals(String message, boolean[] expecteds,
			boolean[] actuals) {
		internalArrayEquals(message, expecteds, actuals);
	}
}
