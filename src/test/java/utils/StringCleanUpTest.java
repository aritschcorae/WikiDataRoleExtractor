package utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class StringCleanUpTest {

	@Test
	void testRemoveHTMLTagAndPlaceholders() {
		Assertions.assertEquals("Three assistants (acolytes)", StringCleanUp.removeHTMLTagAndPlaceholders("<td colspan=\"3\">Three assistants (acolytes)"));
		Assertions.assertEquals("hans Three assistants", StringCleanUp.removeHTMLTagAndPlaceholders("hans <td colspan=\"3\">Three assistants"));
		String cleanUp = StringCleanUp.removeHTMLTagAndPlaceholders("<li><b>Desiree Armfeldt</b>: Self-absorbed, once-successful actress, now touring the countryside in what is clearly not the \"glamorous life\". Harboured love for Fredrik for years since their affair. Mezzo F<span class=\"music-symbol\" style=\"font-family: Arial Unicode MS, Lucida Sans Unicode;\"><span class=\"music-sharp\">&#x266f;</span></span><sub>3</sub>–A<span class=\"music-symbol\" style=\"font-family: Arial Unicode MS, Lucida Sans Unicode;\"><span class=\"music-flat\">&#x266d;</span></span><sub>4</sub></li>");
		Assertions.assertEquals("Desiree Armfeldt: Self-absorbed, once-successful actress, now touring the countryside in what is clearly not the \"glamorous life\". Harboured love for Fredrik for years since their affair. Mezzo F<sub>4</sub>", cleanUp);

	}
	

	@Test
	void testRemoveAfterKeywords() {
		Assertions.assertEquals("", StringCleanUp.removeAfterKeyWords("soprano) favorite d'Orosmane: Enrichetta Méric-Lalande"));
		Assertions.assertEquals("grand maître des provisions", StringCleanUp.removeAfterKeyWords("grand maître des provisions (ténor)"));
		Assertions.assertEquals("Un vieux gitan", StringCleanUp.removeAfterKeyWords("Un vieux gitan (basse): Raffaele Marconi (secondo basso)"));
	}

}
