package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import utils.Utils;

/**
 * @author Rafael Arizcorreta
 * 
 * Takes the manually merged file and creates a file with quickstatemens.
 * Only entries in the file containing content in the role name column are considered. 
 *
 */
public class QuickStatementCreator {
	
	private static final Logger LOGGER = Logger.getLogger(QuickStatementCreator.class.getName());

	private static final String TAB = "\t";
	private static final String PARENTHESIS = "\"";
	private static final String WIKIDATA_PROPERTY_IS_A = "P31";
	private static final String WIKIDATA_PROPERTY_PRESENT_IN_WORK = "P1441";
	
	private static final String WIKIDATA_LABEL = "L";
	private static final String WIKIDATA_DESCRIPTION = "D";

	// link from play to role
	private static final String WIKIDATA_PROPERTY_CHARACTERS = "P674";
	private static final String WIKIDATA_PROPERTY_OPENED_AT = "S813";
	private static final String WIKIDATA_PROPERTY_IMPORTED_FROM = "S143";
	

	private static final String QUICKSTATEMENT_SOURCE_WIKIPEDIA_P1 = WIKIDATA_PROPERTY_IMPORTED_FROM + TAB;
	private static final String QUICKSTATEMENT_SOURCE_WIKIPEDIA_P2 = TAB + WIKIDATA_PROPERTY_OPENED_AT + TAB + "+2019-05-12T00:00:00Z/11";
	private static final String QUICKSTATEMENT_LAST_CREATED_ID = "LAST";
	private static final String QUICKSTATEMENT_CREAT_COMMAND = "CREATE";

	public static void main(String[] args) throws IOException {
		String language = Utils.language;
		String wikipediaLanguageQID = Utils.wikipediaQid;
		String quickStatementSource = QUICKSTATEMENT_SOURCE_WIKIPEDIA_P1 + wikipediaLanguageQID + QUICKSTATEMENT_SOURCE_WIKIPEDIA_P2;
		String fileName = "data-merged.csv";
		String outputName = "roles_quickstatements.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName)));
		br.readLine();
		int newRoles = 0, mergedRoles = 0;
		while (br.ready()) {
			String[] role = br.readLine().split("\\|");
			String roleName = role[3];
			if(roleName == null || roleName.isBlank() || "-".equals(roleName)) {
				continue;
			}
			roleName = cleanUpRoleName(roleName);
			String roleQID;
			if (Utils.isQId(role[1])) {
				roleQID = role[1];
				mergedRoles ++;
			} else {
				roleQID = QUICKSTATEMENT_LAST_CREATED_ID;
				bw.write(QUICKSTATEMENT_CREAT_COMMAND);
				bw.newLine();
				for (String roleCharacteristic : Utils.roleType) {
					bw.write(roleQID + TAB + WIKIDATA_PROPERTY_IS_A + TAB + roleCharacteristic + TAB + quickStatementSource);
					bw.newLine();
				}
				newRoles ++;
			}
			
			bw.write(roleQID + TAB + WIKIDATA_LABEL + language +TAB + PARENTHESIS + roleName + PARENTHESIS);
			bw.newLine();
			String description = concatDescription(role);
			bw.write(roleQID + TAB + WIKIDATA_DESCRIPTION + language + TAB + PARENTHESIS + description + PARENTHESIS);
			bw.newLine();

			String playQID = role[0];
			bw.write(roleQID + TAB + WIKIDATA_PROPERTY_PRESENT_IN_WORK + TAB + playQID + TAB + quickStatementSource);
			bw.newLine();
			
			//add role as character to the play
			bw.write(playQID + TAB + WIKIDATA_PROPERTY_CHARACTERS + TAB + roleQID + TAB + quickStatementSource);
			bw.newLine();
			bw.newLine();
		}
		br.close();
		bw.close();
		LOGGER.info(newRoles + " create statements have been generated");
		LOGGER.info(mergedRoles + " statements with additional information have been generated");
	}

	private static String cleanUpRoleName(String roleName) {
		String cleanedUp = removeLastCharacter(roleName.trim(), ',');
		return cleanedUp;
	}

	/**
	 * Takes the default description and concatinates the optional description if available to it.
	 * 
	 * @param role
	 * @return description of the role
	 */
	private static String concatDescription(String[] role) {
		String description = role[5];
		if(role[4] != null && !role[4].isBlank() && !"-".equals(role[4])) {
			String additionalDescription = role[4].trim();
			for(String keyWords : Utils.comparisonKeyWordsList) {
				if(additionalDescription.split(" ")[0].toLowerCase().startsWith(keyWords)) {
					additionalDescription = Utils.toLowerCase(additionalDescription);
					break;
				}
			}
			additionalDescription = removeLastCharacter(additionalDescription, ',');
			if((description + "; " + additionalDescription).length()> 250) {
				additionalDescription = additionalDescription.substring(0, additionalDescription.indexOf("."));
			}
			description += "; " + additionalDescription;
		}
		return description;
	}

	private static String removeLastCharacter(String stringToProcess, char toRemove) {
		if(stringToProcess.charAt(stringToProcess.length() -1) == toRemove) {
			stringToProcess = stringToProcess.substring(0, stringToProcess.length() - 2);
		}
		return stringToProcess.trim();
	}
}
