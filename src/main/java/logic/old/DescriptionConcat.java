package logic.old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DescriptionConcat {

	public static void main(String[] args) throws IOException {
		Map<String, String> operaComponistMap = getOperaComponistMap();
		String fileName = "08_roles_mapped(in progress).csv";
		String outputName = "08_roles_mapped_final.csv";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName)));
		br.readLine();
		String header = "Role QID|Opera QID|Name|standard description|Description|Voice type|Voice Type QID|Voice TypeQID2|Voice Type QID3|Voice Type QID4";
		bw.write(header);
		bw.newLine();
		while (br.ready()) {
			String line = br.readLine();
			String[] opera = line.split("\\|");
			String composer = operaComponistMap.get(opera[1]);
			String firstPart = opera[0] + "|" + opera[1] + "|" + opera[2] + "|" + opera[3] + " by " + composer;
			if (opera.length >= 5) {
				String description = opera[5].trim();
				if (!"".equals(opera[4])) {
					description = Character.toLowerCase(description.charAt(0)) + description.substring(1);
				} else if (!description.isEmpty()) {
					firstPart = firstPart + "; " + description;
				}
				for (int i = 6; i < opera.length; i++) {
					firstPart += "|" + opera[i];
				}
			}
			bw.write(firstPart);
			bw.newLine();
		}
		br.close();
		bw.close();
	}

	public static Map<String, String> getOperaComponistMap() throws IOException{
		Map<String, String> operaComponistMap = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("opera-componist.csv"), StandardCharsets.UTF_8));
		br.readLine();//header
		while(br.ready()) {
			String readLine = br.readLine();
			String[] split = readLine.split(",");
			operaComponistMap.put(split[0].trim(), split[1].trim());
		}
		br.close();
		return operaComponistMap;
	}
	
}