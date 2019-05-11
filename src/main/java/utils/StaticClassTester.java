package utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import logic.Utils;
import logic.WikidataConnector;
import valueobject.Role;

public class StaticClassTester {
	
	public static void main(String args[]) throws IOException {
		Utils.loadProperties();
		System.out.println(WikidataConnector.getPlayRolesQuery(Utils.language));
		Map<String, List<Role>> playRolesWikidataByLanguage = WikidataConnector.getPlayRolesWikidataByLanguage("en");
		for (String key : playRolesWikidataByLanguage.keySet()) {
			System.out.println(key);
			for (Role roles : playRolesWikidataByLanguage.get(key)) {
				System.out.println(roles.getName());
			}
		}
	}

}
