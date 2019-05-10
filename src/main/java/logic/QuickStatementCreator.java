package logic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

/**
 * @author Rafael Arizcorreta
 * 
 * Takes the manually merged file and creates a file with quickstatemens.
 * Only entries in the file containing content in the role name column are considered. 
 *
 */
public class QuickStatementCreator {

	private static final String TAB = "\t";
	private static final String PARENTHESIS = "\"";
	private static final String WIKIDATA_PROPERTY_IS_A = "P31";
	private static final String WIKIDATA_PROPERTY_PRESENT_IN_WORK = "P1441";
	
	private static final String WIKIDATA_LABEL = "L";
	private static final String WIKIDATA_DESCRIPTION = "D";

	// link from opera to role
	private static final String WIKIDATA_PROPERTY_CHARACTERS = "P674";
	private static final String WIKIDATA_PROPERTY_OPENED_AT = "S813";
	private static final String WIKIDATA_PROPERTY_IMPORTED_FROM = "S143";
	

	private static final String WIKIDATA_ITEM_OPERA_FIGURE = "Q50386450";
	private static final String WIKIDATA_ITEM_FICTIONAL_HUMAN = "Q15632617";
	
	
	private static final String QUICKSTATEMENT_SOURCE_WIKIPEDIA_P1 = WIKIDATA_PROPERTY_IMPORTED_FROM + TAB;
	private static final String QUICKSTATEMENT_SOURCE_WIKIPEDIA_P2 = TAB + WIKIDATA_PROPERTY_OPENED_AT + TAB + "+2019-05-09T00:00:00Z/11";
	private static final String QUICKSTATEMENT_LAST_CREATED_ID = "LAST";
	private static final String QUICKSTATEMENT_CREAT_COMMAND = "CREATE";

	public static void main(String[] args) throws IOException {
		String language = Utils.language;
		String wikipediaLanguageQID = "Q8447";
		String quickStatementSource = QUICKSTATEMENT_SOURCE_WIKIPEDIA_P1 + wikipediaLanguageQID + QUICKSTATEMENT_SOURCE_WIKIPEDIA_P2;
		String fileName = "data-merged.csv";
		String outputName = "roles_quickstatements.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName)));
		br.readLine();
		while (br.ready()) {
			String[] role = br.readLine().split("\\|");
			String roleName = role[3];
			if(roleName == null || roleName.isBlank() || "-".equals(roleName)) {
				continue;
			}
			String roleQID;
			if (Utils.isQId(role[1])) {
				roleQID = role[1];
			} else {
				roleQID = QUICKSTATEMENT_LAST_CREATED_ID;
				bw.write(QUICKSTATEMENT_CREAT_COMMAND);
				bw.newLine();
				bw.write(roleQID + TAB + WIKIDATA_PROPERTY_IS_A + TAB + WIKIDATA_ITEM_OPERA_FIGURE + TAB + quickStatementSource);
				bw.newLine();
				bw.write(roleQID + TAB + WIKIDATA_PROPERTY_IS_A + TAB + WIKIDATA_ITEM_FICTIONAL_HUMAN + TAB + quickStatementSource);
				bw.newLine();
			}
			
			bw.write(roleQID + TAB + WIKIDATA_LABEL + language +TAB + PARENTHESIS + roleName + PARENTHESIS);
			bw.newLine();
			String description = concatDescription(role);
			bw.write(roleQID + TAB + WIKIDATA_DESCRIPTION + language + TAB + PARENTHESIS + description + PARENTHESIS);
			bw.newLine();

			String operaQID = role[0];
			bw.write(roleQID + TAB + WIKIDATA_PROPERTY_PRESENT_IN_WORK + TAB + operaQID + TAB + quickStatementSource);
			bw.newLine();
			
			//add role as character to the opera
			bw.write(operaQID + TAB + WIKIDATA_PROPERTY_CHARACTERS + TAB + roleQID + TAB + quickStatementSource);
			bw.newLine();
			bw.newLine();
		}
		br.close();
		bw.close();
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
			if(additionalDescription.charAt(additionalDescription.length() -1) == ',') {
				additionalDescription = additionalDescription.substring(0, additionalDescription.length() - 2);
			}
			description += "; " + additionalDescription;
		}
		return description;
	}
}
