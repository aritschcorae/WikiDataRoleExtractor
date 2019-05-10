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

import valueobject.Opera;
import valueobject.Role;

/**
 * @author Rafael Arizcorreta
 * 
 * Class to be executed to extract data from wikidata and wikipedia and create a CSV with operas and their roles.
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
		List<Opera> operaFromWikidataList = WikidataConnector.getOperasInWikidataByLanguage(language);
		System.out.println("operas in wikidata: " + operaFromWikidataList.size());
		
		DataScrapper.extractRoles(operaFromWikidataList);
		List<Opera> operaWithRoles = filterNonRoleOperas(operaFromWikidataList);
		processRoles(operaWithRoles);
		
		Map<String, List<Role>> operaRolesWikidataByLanguage = WikidataConnector.getOperaRolesWikidataByLanguage(language);
		System.out.println("loaded roles from " + operaRolesWikidataByLanguage.size() + " operas");
		
		List<String> rolesAsPrintableStringList = matchRoles(operaWithRoles, operaRolesWikidataByLanguage);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("extract " + language + ".csv"), StandardCharsets.UTF_8));
		for (String roleLine : rolesAsPrintableStringList) {
			bw.write(roleLine);
			bw.newLine();
		}
		bw.close();
		
		
	}

	/**
	 * @param operaWithRoles
	 * @param operaRolesWikidataByLanguage
	 * @return A List of Strings with opera roles including a header. If the roles
	 *         names in wikipedia and wikidata match according to the
	 *         Utils.compareStringsIngoringAccent method one row is returned.
	 *         Otherwise one row per role is returned.
	 * 
	 */
	private static List<String> matchRoles(List<Opera> operaWithRoles, Map<String, List<Role>> operaRolesWikidataByLanguage) {
		List<String> rolesAsPrintableStringList = new LinkedList<String>();
		String header = "OperaQID|RoleQID|wikidataName|name|description|defaultdescription|wikidataDescription";
		rolesAsPrintableStringList.add(header);
		for (Opera opera : operaWithRoles) {
			String operaQid = opera.getqID();
			if(operaRolesWikidataByLanguage.containsKey(operaQid)) {
				List<Role> wikidataRoles = operaRolesWikidataByLanguage.get(operaQid);
				List<Role> matchedRoles = new LinkedList<Role>();
				for (Role role : opera.getRoleNames()) {
					boolean matched = false;
					for (Role wikidataRole : wikidataRoles) {
						if (!matchedRoles.contains(wikidataRole) &&
								Utils.compareStringsIngoringAccent(role.getName(), wikidataRole.getName())) {
							matchedRoles.add(wikidataRole);
							rolesAsPrintableStringList.add(getMatchedRoleLine(operaQid, role, wikidataRole));
							matched = true;
							break;
						}
					}
					//if no match found just print simple line
					if(!matched) {
						rolesAsPrintableStringList.add(getSimpeRoleAsPrintableString(operaQid, role));
					}
				}
				wikidataRoles.removeAll(matchedRoles);
				for (Role wikidataRole : wikidataRoles) {
					rolesAsPrintableStringList.add(getSimpeWikidataRoleLine(operaQid, wikidataRole));
				}
			} else {
				// no roles  found for that opera
				rolesAsPrintableStringList.addAll(getRolesAsPrintableString(opera));
			}
		}
		return rolesAsPrintableStringList;
	}

	/**
	 * @param opera
	 * @return all roles of the opera as String 
	 */
	private static List<String> getRolesAsPrintableString(Opera opera) {
		List<String> roles = new LinkedList<String>();
		for (Role role : opera.getRoleNames()) {
			getSimpeRoleAsPrintableString(opera.getqID(), role);
		}
		return roles;
	}

	/**
	 * @param operaQid
	 * @param role
	 * @return the provided role from wikipedia as string
	 */
	private static String getSimpeRoleAsPrintableString(String operaQid, Role role) {
		return operaQid + "|-|-|" + role.getName() + FILE_SEPARATOR_PIPE + role.getDescription() + FILE_SEPARATOR_PIPE + role.getDefaultDescription()
				+ FILE_SEPARATOR_PIPE;
	}

	/**
	 * @param operaQid
	 * @param role
	 * @return the provided role from wikidata as string
	 */
	private static String getSimpeWikidataRoleLine(String operaQid, Role role) {
		String printout = operaQid + FILE_SEPARATOR_PIPE + role.getqID() + FILE_SEPARATOR_PIPE + role.getName() + "|-|-|-|" + role.getDescription();
		return printout;
	}

	/**
	 * @param operaQid
	 * @param role
	 * @param wikidataRole
	 * @return the provided roles from wikipedia and wikidata merged
	 */
	private static String getMatchedRoleLine(String operaQid, Role role, Role wikidataRole) {
		String printout = operaQid + FILE_SEPARATOR_PIPE + wikidataRole.getqID() + FILE_SEPARATOR_PIPE + wikidataRole.getName() + FILE_SEPARATOR_PIPE
				+ role.getName() + FILE_SEPARATOR_PIPE + role.getDescription() + FILE_SEPARATOR_PIPE + role.getDefaultDescription() + FILE_SEPARATOR_PIPE
				+ wikidataRole.getDescription();
		return printout;
	}

	/**
	 * @param operaWithRoles
	 * Cleans up the roles in all the operas. Clean up contains:
	 * splitting of first column in the role string (either by ',', ';' or tab)
	 * cleaning up the role names
	 * creating a default description
	 */
	private static void processRoles(List<Opera> operaWithRoles) {
		for (Opera opera : operaWithRoles) {
			List<Role> rolesNameList = new LinkedList<Role>();
			List<List<String>> roles = opera.getRoles();
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
					role.setDefaultDescription(Utils.defaultDescriptionStart + opera.getName() + Utils.defaultDescriptionBy + opera.getComponist());
					rolesNameList.add(role);
				}
			}
			opera.setRoleNames(rolesNameList);
		}
	}

	private static List<Opera> filterNonRoleOperas(List<Opera> operaList) throws FileNotFoundException, IOException {
		List<Opera> operaWithRoles = new LinkedList<Opera>();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("opera_no_roles_extracted.csv")));
		for (Opera opera : operaList) {
			if (opera.getRoles() != null && !opera.getRoles().isEmpty()) {
				operaWithRoles.add(opera);
			} else {
				bw.write(opera.getqID() + "," + opera.getUrl() + "," + opera.getName());
				bw.newLine();
			}
		}
		bw.close();
		System.out.println("opears with roles: " + operaWithRoles.size());
		return operaWithRoles;
	}


}