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

import valueobject.Play;
import valueobject.Role;

/**
 * @author Rafael Arizcorreta
 * 
 * Class to be executed to extract data from wikidata and wikipedia and create a CSV with plays and their roles.
 * settings are defined in the default.properties file.
 * 
 */
public class MainClass {

	private static final String FILE_SEPARATOR_PIPE = "|";

	/**
	 * @param args
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		String language = Utils.language;
		List<Play> playFromWikidataList = WikidataConnector.getPlayInWikidataByLanguage(language);
		System.out.println("Plays in wikidata: " + playFromWikidataList.size());
		
		DataScrapper.extractRoles(playFromWikidataList);
		List<Play> playsWithRoles = filterNonRolePlays(playFromWikidataList);
		processRoles(playsWithRoles);
		
		Map<String, List<Role>> playRolesWikidataByLanguage = WikidataConnector.getPlayRolesWikidataByLanguage(language);
		System.out.println("loaded roles from " + playRolesWikidataByLanguage.size() + " plays");
		
		List<String> rolesAsPrintableStringList = matchRoles(playsWithRoles, playRolesWikidataByLanguage);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("extract " + language + ".csv"), StandardCharsets.UTF_8));
		for (String roleLine : rolesAsPrintableStringList) {
			bw.write(roleLine);
			bw.newLine();
		}
		bw.close();
		
		
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
				}
			} else {
				// no roles  found for that play
				rolesAsPrintableStringList.addAll(getRolesAsPrintableString(play));
			}
		}
		return rolesAsPrintableStringList;
	}

	/**
	 * @param play
	 * @return all roles of the play as String 
	 */
	private static List<String> getRolesAsPrintableString(Play play) {
		List<String> roles = new LinkedList<String>();
		for (Role role : play.getRoleNames()) {
			getSimpeRoleAsPrintableString(play.getqID(), role);
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
					String firstPart = StringCleanUp.removeHTMLTagAndPlaceholders(roleString.get(0));
					
					String splitSign = null;
					if(firstPart.contains("\t")) {
						splitSign = "\t";
					} else if(firstPart.contains(";")) {
						splitSign = ";";
					} else if(firstPart.contains(",")) {
						splitSign = ",";
					}
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
						description = StringCleanUp.removeAfterKeyWords(description);
						role.setDescription(description);
					}
					roleName = StringCleanUp.removeAfterKeyWords(roleName);
					roleName = StringCleanUp.addClosingBracket(roleName);
					role.setName(roleName);
					role.setDefaultDescription(Utils.defaultDescriptionStart + play.getName() + Utils.defaultDescriptionBy + play.getComposerList());
					rolesNameList.add(role);
				}
			}
			play.setRoleNames(rolesNameList);
		}
	}

	private static List<Play> filterNonRolePlays(List<Play> playList) throws FileNotFoundException, IOException {
		List<Play> playWithRoles = new LinkedList<Play>();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("plays_no_roles_extracted.csv")));
		for (Play play : playList) {
			if (play.getRoles() != null && !play.getRoles().isEmpty()) {
				playWithRoles.add(play);
			} else {
				bw.write(play.getqID() + "," + play.getUrl() + "," + play.getName());
				bw.newLine();
			}
		}
		bw.close();
		System.out.println("opears with roles: " + playWithRoles.size());
		return playWithRoles;
	}


}