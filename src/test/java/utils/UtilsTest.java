package utils;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UtilsTest {

	@Test
	void testCompareStrings() throws IOException {
		Utils.loadProperties();
		Assertions.assertTrue(Utils.compareStringsIngoringAccent("hans", "hans"));
		Assertions.assertTrue(Utils.compareStringsIngoringAccent("queen a", "queen a"));
		Assertions.assertFalse(Utils.compareStringsIngoringAccent("queen a", "queen b"));
		Assertions.assertFalse(Utils.compareStringsIngoringAccent("le danseur", "le hans"));
	}

	@Test
	void testGetLargerInt() {
		Assertions.assertEquals(5, Utils.getLargerInt(1, 5));
		Assertions.assertEquals(5, Utils.getLargerInt(-3, 5));
		Assertions.assertEquals(1, Utils.getLargerInt(1, -3));
	}

	@Test
	void testIsQId() {
		Assertions.assertTrue(Utils.isQId("Q1334"));
		Assertions.assertFalse(Utils.isQId("1334"));
		Assertions.assertFalse(Utils.isQId(null));
	}

	@Test
	void testToLowerCase() {
		Assertions.assertEquals("hans", Utils.toLowerCase("Hans"));
		Assertions.assertEquals("mEIER", Utils.toLowerCase("MEIER"));
		Assertions.assertEquals("", Utils.toLowerCase(""));
		Assertions.assertEquals("", Utils.toLowerCase(null));
	}

}
