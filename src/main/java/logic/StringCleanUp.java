package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringCleanUp {

	private static final String HTML_ANCHOR_TAG_START = "<a href";
	private static final String HTML_ANCHOR_TAG_CLOSE = "</a>";
	private static final String HTML_TAG_START = "<";
	private static final String HTML_TAG_CLOSE = "</";

	public static void maina(String[] args) throws IOException {

		String roles = "src\\main\\resources\\characters_special.txt";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("src\\main\\resources\\characters_special_noLink.txt"), StandardCharsets.UTF_8));
		
		BufferedReader br = new BufferedReader(new FileReader(new File(roles), StandardCharsets.UTF_8));
		while (br.ready()) {
			String playToProcess = br.readLine();
			String clean = removeHTMLTagAndPlaceholders(playToProcess);
			bw.write(clean);
			bw.newLine();
		}
		br.close();
		bw.close();
	}
	
	public static void main (String args[]) {
		System.out.println(removeHTMLTagAndPlaceholders("<td colspan=\"3\">Three assistants (acolytes)"));
		System.out.println(removeHTMLTagAndPlaceholders("hans <td colspan=\"3\">Three assistants"));
		System.out.println(removeAfterKeyWords("soprano) favorite d'Orosmane: Enrichetta Méric-Lalande"));
		System.out.println(removeAfterKeyWords("grand maître des provisions (ténor)"));
		System.out.println(removeAfterKeyWords("Un vieux gitan (basse): Raffaele Marconi (secondo basso)"));
		
	}
	
	/**
	 * Removes place holder and HTML tags which are poluting the String
	 * 
	 * @param stringToCleanUp
	 * @return Cleaned up String
	 */
	public static String removeHTMLTagAndPlaceholders(String stringToCleanUp) {
		String result = removeLinkTag(stringToCleanUp);
		result = removeTag(result, "sup");
		result = removeTag(result, "span");
		result = removeTag(result, "small");
		result = removeTag(result, "abbr");
		result = removeTag(result, "/abbr");
		result = removeColspanTag(result, "td");
		result = removeColspanTag(result, "img");
		result = removeColspanTag(result, "time");
		for (String toRemove : Utils.stringsToRemoveList) {
			result = result.replace(toRemove, Utils.EMPTY_STRING);
		}
		result = result.replace("|?|", "||");
		result = result.replace("|�|", "||");
		result = result.replaceAll("\\\\", "\\");
		return result;
	}
	
	
	/**
	 * Takes a String and returns a substring of the string until on of the keywords if found.
	 * 
	 * @param toCleanUP
	 * @return Substring until one of the keyword is matched
	 */
	public static String removeAfterKeyWords(String toCleanUP) {
		String result = toCleanUP;
		for (String keyword : Utils.trimKeywordList) {
			if(result.toLowerCase().contains(keyword)) {
				result = result.substring(0, result.toLowerCase().indexOf(keyword));
			}
		}
		if(!result.isEmpty() && result.charAt(result.length()-1) == '(') {
			result = result.substring(0, result.length() -1);
		}
		return result;
	}
	
	private static String removeColspanTag(String stringToCleanUp, String tag) {
		String result = stringToCleanUp;
		while(result.contains(HTML_TAG_START + tag)) {
			String firstPart = result.substring(result.indexOf(HTML_TAG_START+tag));
			String tagToRemove = firstPart.substring(0, firstPart.indexOf(">") + 1);
			result = result.replace(tagToRemove, Utils.EMPTY_STRING);
		}
		return result;
	}

	private static String removeTag(String role, String a) {
		String tagLessRole = role;
		while (tagLessRole.contains(HTML_TAG_START + a) && tagLessRole.contains(HTML_TAG_CLOSE + a)) {
			String start = tagLessRole.substring(0, tagLessRole.indexOf(HTML_TAG_START + a));
			String end = tagLessRole.substring(tagLessRole.indexOf(HTML_TAG_CLOSE + a) + 3 + a.length());
			tagLessRole = start + end;
		}
		return tagLessRole;
	}
	
	private static String removeLinkTag(String role) {
		String linkLessRole = role;
		if(role.contains(HTML_ANCHOR_TAG_START)) {
			while(linkLessRole.contains(HTML_ANCHOR_TAG_START)) {
				linkLessRole = removeLink(linkLessRole);
			}
		} 
		return linkLessRole;
	}

	private static String removeLink(String playToProcess) {
		String firstLink = getFirstLink(playToProcess);
		String value = getLinkValue(firstLink);
		String linklessLine = playToProcess.replace(firstLink, value);
		return linklessLine;
	}

	private static String getLinkValue(String firstLink) {
		String result = firstLink.substring(firstLink.indexOf(">")+1, firstLink.indexOf(HTML_ANCHOR_TAG_CLOSE));
		return result;
		
	}

	private static String getFirstLink(String playToProcess) {
		String t = playToProcess.substring(playToProcess.indexOf(HTML_ANCHOR_TAG_START));
		String link = t.substring(0, t.indexOf(HTML_ANCHOR_TAG_CLOSE) + 4);
		return link;
	}

	/**
	 * Adds a closing bracket if the String had more opening than closing brackets.
	 * @param roleName
	 * @return String if closing bracket if needed.
	 */
	public static String addClosingBracket(String roleName) {
		long countOpeningBrackets = roleName.chars().filter(ch -> ch == '(').count();
		long countClosingBrackets = roleName.chars().filter(ch -> ch == ')').count();
		if(countOpeningBrackets > countClosingBrackets) {
			return roleName + ")";
		}
		return roleName;
	}

}
