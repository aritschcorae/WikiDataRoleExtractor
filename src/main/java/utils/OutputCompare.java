package utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import logic.Utils;

public class OutputCompare {

	private static final Logger LOGGER = Logger.getLogger(OutputCompare.class.getName());
	
	public static void main(String args[]) throws IOException {
		Map<String, List<String>> germanExtract = readExtractFile("extract de.csv");
		Map<String, List<String>> englishExtract = readExtractFile("extract en.csv");
		logMap(germanExtract);
		logMap(englishExtract);
		int matches = 0;
		for (String key : germanExtract.keySet()) {
			if(englishExtract.containsKey(key)) {
				List<String> germanRoles = germanExtract.get(key);
				List<String> englishRoles = englishExtract.get(key);
				for (String germanRole : germanRoles) {
					for (String englishRole : englishRoles) {
						if(Utils.compareStringsIngoringAccent(englishRole, germanRole)) {
//							LOGGER.info(englishRole + " match " + germanRole);
							matches ++;
							break;
						}
					}
				}
			}
		}
		LOGGER.info("matched in both extracts: " + matches);
	}

	private static void logMap(Map<String, List<String>> extractMap) {
		int amountOfRoles = 0;
		for (String roles : extractMap.keySet()) {
			amountOfRoles += extractMap.get(roles).size();
		}
		LOGGER.info("#Play: " + extractMap.keySet().size());
		LOGGER.info("#Roles: " + amountOfRoles);
	}

	private static Map<String, List<String>> readExtractFile(String filename) throws FileNotFoundException, IOException {
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8));
		br.readLine();
		while (br.ready()) {
			String[] role = br.readLine().split("\\|");
			if("-".equals(role[1])) {
				String playid = role[0];
				if(!result.containsKey(playid)) {
					result.put(playid, new LinkedList<String>());
				}
				String name = role[3];
				result.get(playid).add(name);
			}
		}
		br.close();
		return result;
	}

}
