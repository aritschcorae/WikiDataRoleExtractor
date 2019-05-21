package logic;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import utils.StringCleanUp;
import utils.Utils;
import valueobject.Play;
import valueobject.Role;

/**
 * @author Rafael Arizcorreta
 * 
 * Class to be executed to extract data from wikidata and wikipedia and create a CSV with plays and their roles.
 * settings are defined in the default.properties file.
 * 
 */
public class DataExtractor {

	private static final String FILE_SEPARATOR_PIPE = "|";
	private static final Logger LOGGER = Logger.getLogger(DataExtractor.class.getName());

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		String language = Utils.language;
		List<Play> playFromWikidataList = WikidataConnector.getPlayInWikidataByLanguage(language);
		LOGGER.info("Plays in wikidata: " + playFromWikidataList.size());
		
		DataScraping.extractRoles(playFromWikidataList);
		List<Play> playsWithRoles = filterNonRolePlays(playFromWikidataList);
		processRoles(playsWithRoles);
		Map<String, List<Role>> playRolesWikidataByLanguage = WikidataConnector.getPlayRolesWikidataByLanguage(language);
		int wikidataRoles = countWikiDataRoles(playRolesWikidataByLanguage);
		LOGGER.info("Loaded " + wikidataRoles + " roles from " + playRolesWikidataByLanguage.size() + " wikidata plays");
		
		List<String> rolesAsPrintableStringList = matchRoles(playsWithRoles, playRolesWikidataByLanguage);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("extract_ " + language + "_" + Utils.playType + ".csv"), StandardCharsets.UTF_8));
		for (String roleLine : rolesAsPrintableStringList) {
			bw.write(roleLine);
			bw.newLine();
		}
		bw.close();
		
		LOGGER.info("Finished in : " + ((System.currentTimeMillis() - startTime) /1000) + " seconds");
	}

	private static int countWikiDataRoles(Map<String, List<Role>> playRolesWikidataByLanguage) {
		int wikidataRoles = 0;
		for (String key : playRolesWikidataByLanguage.keySet()) {
			wikidataRoles += playRolesWikidataByLanguage.get(key).size();
		}
		return wikidataRoles;
	}

	/**
	 * @param playWithRoles
	 * @param playRolesWikidataByLanguage
	 * @return A List of Strings with play roles including a header. If the roles
	 *         names in wikipedia and wikidata match according to the
	 *         Utils.compareStringsIngoringAccent method one row is returned.
	 *         Otherwise one row per role is returned.
	 * 
	 */
	private static List<String> matchRoles(List<Play> playWithRoles, Map<String, List<Role>> playRolesWikidataByLanguage) {
		int newRoles = 0, mergedRoles = 0, rolesinWiki = 0;
		List<String> rolesAsPrintableStringList = new LinkedList<String>();
		String header = "PlayQID|RoleQID|wikidataName|name|description|defaultdescription|wikidataDescription";
		rolesAsPrintableStringList.add(header);
		for (Play play : playWithRoles) {
			String playQid = play.getqID();
			if(playRolesWikidataByLanguage.containsKey(playQid)) {
				List<Role> wikidataRoles = playRolesWikidataByLanguage.get(playQid);
				List<Role> matchedRoles = new LinkedList<Role>();
				for (Role role : play.getRoleNames()) {
					boolean matched = false;
					for (Role wikidataRole : wikidataRoles) {
						if (!matchedRoles.contains(wikidataRole) &&
								Utils.compareStringsIngoringAccent(role.getName(), wikidataRole.getName())) {
							matchedRoles.add(wikidataRole);
							rolesAsPrintableStringList.add(getMatchedRoleLine(playQid, role, wikidataRole));
							matched = true;
							mergedRoles++;
							break;
						}
					}
					//if no match found just print simple line
					if(!matched) {
						rolesAsPrintableStringList.add(getSimpeRoleAsPrintableString(playQid, role));
					}
				}
				wikidataRoles.removeAll(matchedRoles);
				for (Role wikidataRole : wikidataRoles) {
					rolesAsPrintableStringList.add(getSimpeWikidataRoleLine(playQid, wikidataRole));
					rolesinWiki ++;
				}
			} else {
				// no roles  found for that play
				List<String> rolesAsPrintableString = getRolesAsPrintableString(play);
				rolesAsPrintableStringList.addAll(rolesAsPrintableString);
				newRoles += rolesAsPrintableString.size();
			}
		}
		LOGGER.info("New Roles: " + newRoles);
		LOGGER.info("Roles in Wikidata: " + rolesinWiki);
		LOGGER.info("Roles merged: " + mergedRoles);
		return rolesAsPrintableStringList;
	}

	/**
	 * @param play
	 * @return all roles of the play as String 
	 */
	private static List<String> getRolesAsPrintableString(Play play) {
		List<String> roles = new LinkedList<String>();
		for (Role role : play.getRoleNames()) {
			roles.add(getSimpeRoleAsPrintableString(play.getqID(), role));
		}
		return roles;
	}

	/**
	 * @param playQid
	 * @param role
	 * @return the provided role from wikipedia as string
	 */
	private static String getSimpeRoleAsPrintableString(String playQid, Role role) {
		return playQid + "|-|-|" + role.getName() + FILE_SEPARATOR_PIPE + role.getDescription() + FILE_SEPARATOR_PIPE + role.getDefaultDescription()
				+ FILE_SEPARATOR_PIPE;
	}

	/**
	 * @param playlQid
	 * @param role
	 * @return the provided role from wikidata as string
	 */
	private static String getSimpeWikidataRoleLine(String playlQid, Role role) {
		String printout = playlQid + FILE_SEPARATOR_PIPE + role.getqID() + FILE_SEPARATOR_PIPE + role.getName() + "|-|-|-|" + role.getDescription();
		return printout;
	}

	/**
	 * @param playQid
	 * @param role
	 * @param wikidataRole
	 * @return the provided roles from wikipedia and wikidata merged
	 */
	private static String getMatchedRoleLine(String playQid, Role role, Role wikidataRole) {
		String printout = playQid + FILE_SEPARATOR_PIPE + wikidataRole.getqID() + FILE_SEPARATOR_PIPE + wikidataRole.getName() + FILE_SEPARATOR_PIPE
				+ role.getName() + FILE_SEPARATOR_PIPE + role.getDescription() + FILE_SEPARATOR_PIPE + role.getDefaultDescription() + FILE_SEPARATOR_PIPE
				+ wikidataRole.getDescription();
		return printout;
	}

	/**
	 * @param playWithRoles
	 * Cleans up the roles in all the plays. Clean up contains:
	 * splitting of first column in the role string (either by ',', ';' or tab)
	 * cleaning up the role names
	 * creating a default description
	 */
	private static void processRoles(List<Play> playWithRoles) {
		for (Play play : playWithRoles) {
			List<Role> rolesNameList = new LinkedList<Role>();
			List<List<String>> roles = play.getRoles();
			for (List<String> roleString : roles) {
				if(!roleString.isEmpty()) {
					Role role = new Role();
					String firstPart = StringCleanUp.removeHTMLTagAndPlaceholders(roleString.get(0)).trim();
					
					String splitSign = getSplitSign(firstPart);
					String roleName;
					if(splitSign == null) {
						roleName = firstPart;
					} else {
						roleName = firstPart.split(splitSign)[0];
						String description = firstPart.substring(firstPart.indexOf(splitSign) + splitSign.length()).trim();
						if (roleString.size() > 1) {
							for (int i = 1; i < roleString.size(); i++) {
								description += " " + StringCleanUp.removeHTMLTagAndPlaceholders(roleString.get(i));
							}
						}
						description = Utils.toLowerCase(StringCleanUp.removeAfterKeyWords(description));
						role.setDescription(description);
					}
					roleName = StringCleanUp.removeAfterKeyWords(roleName);
					roleName = StringCleanUp.addClosingBracket(roleName);
					role.setName(roleName.trim());
					String defaultDescription = Utils.EMPTY_STRING;
					if(play.getComposerList() != null && !play.getComposerList().isEmpty()) {
						defaultDescription = Utils.defaultDescriptionBy + " " + play.getComposersAsString();
					}
					role.setDefaultDescription(Utils.defaultDescriptionStart + play.getName() + defaultDescription);
					rolesNameList.add(role);
				}
			}
			play.setRoleNames(rolesNameList);
		}
	}

	private static String getSplitSign(String toExtract) {
		String[] hardIndicator = {":","\t", "--", "–", "-", "—"};
		String[] softIndicator = {";",","};
		
		for (String splitIndicator : hardIndicator) {
			if(toExtract.contains(splitIndicator)) {
				return getSplitSignWithList(toExtract, hardIndicator);
			}
		}
		return getSplitSignWithList(toExtract, softIndicator);
	}

	private static String getSplitSignWithList(String toExtract, String[] indicatorList) {
		int firstSign = Integer.MAX_VALUE;
		String splitSign = null;
		for (String indicator : indicatorList) {
			if (toExtract.contains(indicator)) {

				int indexOf = toExtract.indexOf(indicator);
				if (indexOf < firstSign) {
					splitSign = indicator;
					firstSign = indexOf;
				}
			}
		}
		return splitSign;
	}

	private static List<Play> filterNonRolePlays(List<Play> playList) throws FileNotFoundException, IOException {
		List<Play> playWithRoles = new LinkedList<Play>();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plays_no_roles_extracted.csv")));
		for (Play play : playList) {
			if (play.getRoles() != null && !play.getRoles().isEmpty()) {
				playWithRoles.add(play);
			} else {
				bw.write(play.getUrl() + "," + play.getqID() + "," + play.getName());
				bw.newLine();
			}
		}
		bw.close();
		LOGGER.info("Plays with roles: " + playWithRoles.size());
		return playWithRoles;
	}


}