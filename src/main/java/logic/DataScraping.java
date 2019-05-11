package logic;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import valueobject.Play;

/**
 * @author Rafael Arizcorreta
 *
 * Scrapes {@link Play} by their wikipedia url and returns a list with the roles as List of strings.
 *
 */
public class DataScraping {
	
	
	public static void main(String[] args) throws IOException {
		List<List<String>> playList = extractRoles("https://en.wikipedia.org/wiki/Miss_Saigon");
		for (List<String> list : playList) {
			for (String item : list) {
				System.out.print(StringCleanUp.removeHTMLTagAndPlaceholders(item));
			}
			System.out.println();
		}
	}

	/**
	 * Scrapes wikipedia pages based on the {@link Play} list containing links to wikipedia.
	 * The scraping tool checks for the list of keywords for and checks if a
	 * unsorted list or a table is available and scrapes the data out of there.
	 * tables are higher priority
	 * 
	 * @param playList
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static List<Play> extractRoles(List<Play> playList) throws FileNotFoundException, IOException {
		for (Play play : playList) {
			try {
				List<List<String>> roles = extractRoles(play.getUrl());
				play.setRoles(roles);
			} catch (StringIndexOutOfBoundsException s) {
			}
		}
		
		for (Play play : playList) {
			if(play.getRoles() != null && !play.getRoles().isEmpty()) {
				if(play.getHeaderRow().contains("role|Voice type|Premiere Cast|")) {
				} 
			}
		}
		return playList;
	}


	/**
	 * @param url
	 * @return A list of list wtih roles as String
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static List<List<String>> extractRoles(String url) throws FileNotFoundException, IOException {
		String urlSource = Utils.getURLSource(url);
		List<String> spanIds = getSpanIds(urlSource);
		List<List<String>> rolesAsList = extractRoles(urlSource, spanIds);
		return rolesAsList;
	}

	/**
	 * @param urlSource
	 * @return A list with all ids which are part of a span tag.
	 */
	private static List<String> getSpanIds(String urlSource) {
		List<String> spanIds = new LinkedList<String>();
		String[] split = urlSource.split("<span");
		for (String spanItem : split) {
			if(!spanItem.startsWith(">")) {
				String getSpanId = spanItem.split(">")[0];
				if (getSpanId.contains("id=\"")) {
					String spanId = getSpanId.split("id=\"")[1].replace("\"", "");
					spanIds.add(spanId);
				}
			}
		}
		return spanIds;
	}
	
	/**
	 * Scrapes the wikipage for a table which is in a span of the keyword list.
	 * If matched returns the whole table content.
	 * 
	 * @param urlSource Source code of the wikipage
	 * @param spanIds all ids beloning to a span
	 * @return A the table containing all roles. 
	 * @throws IOException
	 */
	private static List<List<String>> extractRoles(String urlSource, List<String> spanIds) throws IOException {
		String roles = null;
		List<List<String>> a;
		for (String header : Utils.wikipediaSectionHeader) {
			roles = extractRolesAsHTMLById(urlSource, header, spanIds);
			if(roles != null) {
				return extractRolesFromTable(roles);
			}
			a = extractRolesOfUnsortedList(urlSource, spanIds, header);
			if(!a.isEmpty()) {
				return a;
			}
		}
		return Collections.emptyList();
	}

	/**
	 * @param urlSource
	 * @param id
	 * @param spanIds
	 * @return A html source of the table in the span
	 */
	private static String extractRolesAsHTMLById(String urlSource, String id, List<String> spanIds) {
		for(int i = 0; i < spanIds.size();i++) {
			String spanId = spanIds.get(i);
			String nextSpanId = i +1 == spanIds.size() ? null: spanIds.get(i+1);
			if(spanId.startsWith(id)) {
				String roleBlock = urlSource.substring(urlSource.indexOf("id=\""+spanId+"\">"));
				if(nextSpanId != null) {
					roleBlock = roleBlock.substring(0, roleBlock.indexOf(nextSpanId));
				}
				if(roleBlock.contains("</tbody></table>")) {
					return roleBlock.substring(0, roleBlock.indexOf("</tbody></table>"));
				}
			}
			
		}
		return null;
	}

	/**
	 * Scrapes the wikipedia page for spans in the span id list. If match is found
	 * processed the following unsorted list and creates roles based on that.
	 * 
	 * @param urlSource
	 * @param spanIds
	 * @return List of all roles in an unsorted list.
	 */
	private static List<List<String>> extractRolesOfUnsortedList(String urlSource, List<String> spanIds, String roleId) {
		List<List<String>> result = new LinkedList<List<String>>();
		for (int i = 0; i < spanIds.size(); i++) {
			String spanId = spanIds.get(i);
			String nextSpanId = i + 1 == spanIds.size() ? null : spanIds.get(i + 1);
			if (spanId.startsWith(roleId)) {
				String start;
				if (nextSpanId == null) {
					start = urlSource.substring(urlSource.indexOf("id=\"" + spanId + "\">"));
				} else {
					start = urlSource.substring(urlSource.indexOf("id=\"" + spanId + "\">"), urlSource.indexOf("id=\"" + nextSpanId + "\">"));
				}
				if (start.contains("<ul>")) {
					String list = start.substring(start.indexOf("<ul>"), start.indexOf("</ul>"));
					String[] rolesSplit = list.split(Utils.WINDOWS_NEW_LINE);
					for (String string : rolesSplit) {
						result.add(Arrays.asList(string));
					}
					break;
				}
			}

		}
		return result;
	}

	/**
	 * Parses the html table and extracts the roles in their.
	 * @param roleAsHTML
	 * @return List with all roles in the html table
	 */
	private static List<List<String>> extractRolesFromTable(String roleAsHTML) {
		boolean isHeaderSet = false, headerSetting = false;
		List<String> tempRow = new LinkedList<String>();
		List<List<String>> roles = new LinkedList<List<String>>();

		String[] rolesSplit = roleAsHTML.split(Utils.WINDOWS_NEW_LINE);
		for (String stringa : rolesSplit) {
			String string = stringa.trim();
			if (!isHeaderSet) {
				if ("</th></tr>".equals(string)) {
					isHeaderSet = true;
//					roles.add(tempRow);
					tempRow = new LinkedList<String>();
				} else if ("<tbody><tr>".equals(string)) {
					headerSetting = true;
				} else if (headerSetting && string.startsWith("<th>")) {
					tempRow.add(cleanUpHeader(string));
				}
			} else {
				if ("</td></tr>".equals(string) || "</th></tr>".equals(string)) {
					roles.add(tempRow);
					tempRow = new LinkedList<String>();
				} else if (headerSetting) {
					if(string.startsWith("<td")) {
						tempRow.add(string.replaceAll("<td>", "").replaceAll("</td>", ""));
					} else if (string.startsWith("<th")) {
						tempRow.add(string.replaceAll("<th>", "").replaceAll("</th>", ""));
					}
				}
			}
		}
		return roles;
	}
	
	/**
	 * Used for testing
	 * @param string
	 * @return Table column header in an uniform way.
	 */
	private static String cleanUpHeader(String string) {
		String cleanHeader = string.replaceAll("<th>", "").replaceAll("</th>", "");
		String lowerCaseHeader = cleanHeader.toLowerCase();
		if(lowerCaseHeader.contains("Role")) {
			return "Role";
		} else if(lowerCaseHeader.contains("voice type")) {
			return "Voice type";
		} else if(lowerCaseHeader.contains("premier past")) {
			return "Premiere Cast";
		} else if(lowerCaseHeader.contains("premiere cast")) {
			return "Premiere Cast";
		}
		return lowerCaseHeader;
	}

}
