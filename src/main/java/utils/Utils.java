package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.LinkedList;
import java.util.List;

public class Utils {

	public static final String WINDOWS_NEW_LINE = "\n";
    public static final String EMPTY_STRING = "" ;
    
    public static String language;
    public static String wikipediaQid;
    public static String defaultDescriptionStart;
    public static String defaultDescriptionBy;
    public static String playType;
    public static Boolean autoTrim = Boolean.FALSE;
    public static List<String> comparisonKeyWordsList = new LinkedList<String>();
    public static List<String> stringsToRemoveList = new LinkedList<String>();
    public static List<String> trimKeywordList = new LinkedList<String>();
    public static List<String> roleType = new LinkedList<String>();

	public static List<String> wikipediaSectionHeader = new LinkedList<String>();
	
	static {
		try {
			loadProperties();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads the properties from the default.properties file.
	 * 
	 * @throws IOException
	 */
	public static void loadProperties() throws IOException {
		String readFile = readFile("src/main/resources/default.properties");
		boolean loadKeywords = false, loadComparisonWords = false, loadRemovalStrings = false, trimKeywords = false, roleTypes = false;
		for (String propertyLine : readFile.split(WINDOWS_NEW_LINE)) {
			if(propertyLine.startsWith("autoTrim=")) {
				autoTrim = Boolean.valueOf(propertyLine.split("=")[1]);
			} else if(propertyLine.startsWith("language=")) {
				language = propertyLine.split("=")[1];
			} else if(propertyLine.startsWith("wikipediaLanguage=")) {
				wikipediaQid = propertyLine.split("=")[1];
			} else if(propertyLine.startsWith("defaultDescription=")) {
				String temp = propertyLine.split("=")[1];
				String[] defaultDesc = temp.split("PLAY_NAME");
				defaultDescriptionStart = defaultDesc[0];
				defaultDescriptionBy = defaultDesc[1];
			} else if(propertyLine.startsWith("playType=")) {
				playType = propertyLine.split("=")[1];
			} else if(propertyLine.startsWith("#")) {
				loadKeywords = propertyLine.startsWith("#wikipedia");
				loadComparisonWords = propertyLine.startsWith("#comparison");
				loadRemovalStrings = propertyLine.startsWith("#remove");
				trimKeywords =propertyLine.startsWith("#trim"); 
				roleTypes =propertyLine.startsWith("#role type"); 
				continue;
			}
			if(loadKeywords) {
				wikipediaSectionHeader.add(propertyLine);
			} else if(loadComparisonWords) {
				comparisonKeyWordsList.add(propertyLine);
			}  else if(loadRemovalStrings) {
				stringsToRemoveList.add(propertyLine);
			} else if(trimKeywords) {
				trimKeywordList.add(propertyLine);
			} else if(roleTypes) {
				roleType.add(propertyLine);
			}
		}
	}
	
	/**
	 * @param pathname
	 * @return The file in String format (separed with newline)
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String readFile(String pathname) throws FileNotFoundException, IOException {
		StringBuffer roleAsHTML = new StringBuffer();
		BufferedReader br = new BufferedReader(new FileReader(new File(pathname), StandardCharsets.UTF_8));
		while (br.ready()) {
			roleAsHTML.append(br.readLine());
			roleAsHTML.append(WINDOWS_NEW_LINE);
		}
		br.close();
		return roleAsHTML.toString();
	}

	/**
	 * From StackOverflow by BullyWiiPlaza
	 * 
	 */
	public static String getURLSource(String url) throws IOException {
		URL urlObject = new URL(url);
		URLConnection urlConnection = urlObject.openConnection();
		urlConnection.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		return toString(urlConnection.getInputStream());
	}

	/**
	 * From StackOverflow by BullyWiiPlaza
	 */
	private static String toString(InputStream inputStream) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
			String inputLine;
			StringBuilder stringBuilder = new StringBuilder();
			while ((inputLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(inputLine);
				stringBuilder.append(Utils.WINDOWS_NEW_LINE);
			}

			return stringBuilder.toString();
		}
	}


	/**
	 * @param a
	 * @param b
	 * @return returns the bigger int;
	 */
	public static int getLargerInt(int a, int b) {
		return a < b ? b : a;
	}

	public static boolean isQId(String name) {
		if(name != null && name.startsWith("Q")) {
			return isNumber(name.substring(1));
		}
		return false;
	}

	private static boolean isNumber(String substring) {
		try {
			Integer.parseInt(substring);
			return true;
		}catch(Exception e) { }
		return false;
	}
	
	public static String toLowerCase(String description) {
		if(description == null || description.isEmpty()) {
			return EMPTY_STRING;
		}
		return Character.toLowerCase(description.charAt(0)) + description.substring(1);
	}
	
	/**
	 * Escapes the string from special character like é to e and compares the
	 * strings ignoring upper and lower case. If no match was done and the strings
	 * are not in the comparison key word list a substring of the string is created
	 * (split with a space character) and compared again.
	 * 
	 * @param a String to compare
	 * @param b String to compare
	 * @return true if the Strings are identical.
	 */
	public static boolean compareStringsIngoringAccent(String a, String b) {
		String compareA = excapeString(a);
		String compareB = excapeString(b);
		boolean simpleCompare = compareA.equalsIgnoreCase(compareB);
		if(!simpleCompare) {
			for (String keyWords : comparisonKeyWordsList) {
				if(a.startsWith(keyWords)) {
					return false;
				}
			}
			return a.split(" ")[0].equalsIgnoreCase(b.split(" ")[0]); 
		} 
		return true;
	}

	private static String excapeString(String a) {
		String test = Normalizer.normalize(a, Normalizer.Form.NFKD);
		test = test.replaceAll("[^\\p{ASCII}]", "");
		return test.trim();
	}
}
