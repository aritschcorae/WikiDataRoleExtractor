package logic.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class DescriptionCleanUp {

	public static void main(String[] args) throws IOException {
		Map<String, String> operas = loadOperas();

		String roles = "src\\main\\resources\\05_roles_in_excel.csv";
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("src\\main\\resources\\roles_with_standard_description.txt"), StandardCharsets.UTF_8));
		bw.write("﻿QID|Role QID|exists in eng|Role|standard description|description|Voice type|Premiere cast|rev or 2nd prem|3rd|4th");
		bw.newLine();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(roles), StandardCharsets.UTF_8));
		br.readLine();
		while (br.ready()) {
			String readLine = br.readLine();
			String[] splittedLine = readLine.split("\\|");
			String qid = splittedLine[0];
			String description = "operatic character in the opera " + operas.get(qid).trim();
			String newLine = splittedLine[0] + "|" + description + "|" + splittedLine[1] + "|" + splittedLine[2] + "|" + splittedLine[3] + "|";
			
			if(splittedLine.length > 4 && !splittedLine[4].equals("")) {
				newLine += capitalizeString(splittedLine[4].trim()) + "|";
			} else {
				newLine += "|";
			}
			for(int i = 5; i < splittedLine.length;i++) {
				newLine += splittedLine[i] + "|";
			}
			System.out.println(newLine);
			bw.write(newLine);
			bw.newLine();
		}
		br.close();
		bw.close();
	}
	
	private static String capitalizeString(String input) {
		if(input.length()>0) {
			String output = input.substring(0, 1).toUpperCase() + input.substring(1);
			return output;
		}
		return input;
	}
	
	private static Map<String, String> loadOperas() throws IOException {
		Map<String, String> result = new HashMap<String, String>();
		String roles = "src\\main\\resources\\wikipedia url_old.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(roles), StandardCharsets.UTF_8));
		br.readLine();
		while (br.ready()) {
			String readLine = br.readLine();
			String[] operaSplit = readLine.split("\\|");
			String qId = operaSplit[1];
			int indexOf = readLine.indexOf(" ");
			String title = readLine.substring(indexOf);
			result.put(qId, title);
		}
		br.close();
		return result;
	}

	private static String removeTag(String role, String a) {
		String tagLessRole = role;
		while (tagLessRole.contains("<" + a) && tagLessRole.contains("</" + a)) {
			String start = tagLessRole.substring(0, tagLessRole.indexOf("<" + a));
			String end = tagLessRole.substring(tagLessRole.indexOf("</" + a) + 3 + a.length());
			tagLessRole = start + end;
		}
		return tagLessRole;
	}
	
	private static String removeLinkTag(String role) {
		String linkLessRole = role;
		if(role.contains("<a href")) {
			while(linkLessRole.contains("<a href")) {
				linkLessRole = removeLink(linkLessRole);
			}
		} 
		return linkLessRole;
	}

	private static String removeLink(String operaToProcess) {
		String firstLink = getFirstLink(operaToProcess);
		String value = getLinkValue(firstLink);
		String linklessLine = operaToProcess.replace(firstLink, value);
		return linklessLine;
	}

	private static String getLinkValue(String firstLink) {

		String result = firstLink.substring(firstLink.indexOf(">")+1, firstLink.indexOf("</a>"));
		return result;
		
	}

	private static String getFirstLink(String operaToProcess) {
		String t = operaToProcess.substring(operaToProcess.indexOf("<a href"));
		String link = t.substring(0, t.indexOf("</a>") + 4);
		return link;
	}

}
