package logic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.util.FmtUtils;

import com.github.jsonldjava.shaded.com.google.common.collect.Lists;

import utils.Utils;
import valueobject.Play;
import valueobject.Role;

public class WikidataConnector {

	
	/**
	 * Executes a SPARQL query to retrieve all plays in wikidata with the name and
	 * url to wikipedia by the language requested.
	 * 
	 * Language in short (i.e. en, fr, de)
	 * 
	 * 
	 * @param language
	 */
	public static List<Play> getPlayInWikidataByLanguage(String language) {
		List<Play> queryResults = new LinkedList<Play>();
		String queryString = getPlaysSPARQLQuery(language);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", queryString);
		try {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultSetRewindable = ResultSetFactory.makeRewindable(results);
			List<String> header = new LinkedList<String>();
			for (int col = 0; col < resultSetRewindable.getResultVars().size(); col++) {
				header.add(results.getResultVars().get(col));
			}
			
			while (resultSetRewindable.hasNext()) {
				Play play = extractPlay(language, resultSetRewindable, header);
				addPlay(queryResults, play);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			qexec.close();
		}
		
		for (Play play : queryResults) {
			play.getqID();
		}
		return queryResults;
	}


	private static void addPlay(List<Play> queryResults, Play play) {
		if(play != null) {
			boolean load = addComponistIfPlayAlreadyExists(queryResults, play);
			if(load) {
				queryResults.add(play);
			}
		}
	}


	/**
	 * @param queryResults
	 * @param play
	 * @return false if play already exists
	 */
	private static boolean addComponistIfPlayAlreadyExists(List<Play> queryResults, Play play) {
		boolean load = true;
		for (Play playLoaded : queryResults) {
			if(playLoaded.getqID().equals(play.getqID())) {
				playLoaded.getComposerList().addAll(play.getComposerList());
				load = false;
			}
		}
		return load;
	}

	private static Play extractPlay(String language, ResultSetRewindable resultSetRewindable, List<String> header) {
		QuerySolution rBind = resultSetRewindable.nextSolution();
		Play play = new Play();
		for (String key : header) {
			String var = getVarValueAsString(rBind, key);
			if("item".equals(key)) {
				String qid = var.replace("<http://www.wikidata.org/entity/", "").replace(">", "");
				play.setqID(qid);
			} else if("itemLabel".equals(key)) {
				String name = var.replace("\"", "").replace("@" + language, "");
				play.setName(name);
			}  else if("componistLabel".equals(key)) {
				String name = var.replace("\"", "").replace("@" + language, "");
				play.setComposerList(Lists.newArrayList(name));
			} else if("article".equals(key)) {
				String url = var.replace("<", "").replace(">", "");
				play.setUrl(url);
			}
		}
		//we only want the plays which have a url
		if("".equals(play.getUrl())) {
			return null;
		}
		return play;
	}
	
	/**
	 * @param language
	 * @return SPARQL query to retreive all plays in wikidata with the qid, componist and link to the wikipedia page in the respective language
	 */
	public static String getPlaysSPARQLQuery(String language) {
		String queryString = "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" 
				+ "PREFIX wd: <http://www.wikidata.org/entity/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#> \n" 
				+ "PREFIX schema: <http://schema.org/>\n" 
				+ "PREFIX bd: <http://www.bigdata.com/rdf#>\r\n"
				+ "SELECT ?item ?itemLabel "
				+ "?article ?componistLabel \r\n" 
				+ "WHERE {\r\n" 
				+ "  ?item wdt:P31 wd:" + Utils.playType + ".\r\n" 
				+ "  OPTIONAL { ?item wdt:P86 ?componist. } \r\n"
				+ "    OPTIONAL {\r\n"
				+ "      ?article schema:about ?item .\r\n" 
				+ "      ?article schema:inLanguage \"" + language + "\" .\r\n"
				+ "      ?article schema:isPartOf <https://" + language + ".wikipedia.org/> .\r\n" 
				+ "    }\r\n"
				+ "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"" + language + "\". }\r\n"
			+ "}";
//		+ "} LIMIT 10";
		return queryString;
	}


	/**
	 * @param rBind result of the sparql
	 * @param varName name of the column
	 * @return value as String
	 */
	private static String getVarValueAsString(QuerySolution rBind, String varName) {
		RDFNode obj = rBind.get(varName);
		if (obj == null)
			return Utils.EMPTY_STRING;
		return FmtUtils.stringForRDFNode(obj, null);
	}

	/**
	 * @param language
	 * @return a map with all roles corresponding to a performance. key is the performance qid
	 */
	public static Map<String, List<Role>> getPlayRolesWikidataByLanguage(String language) {
		Map<String, List<Role>> queryResults = new HashMap<String, List<Role>>();
		String queryString = getPlayRolesQuery(language);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", queryString);
		try {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultSetRewindable = ResultSetFactory.makeRewindable(results);
			List<String> header = new LinkedList<String>();
			for (int col = 0; col < resultSetRewindable.getResultVars().size(); col++) {
				header.add(results.getResultVars().get(col));
			}
			while (resultSetRewindable.hasNext()) {
				QuerySolution rBind = resultSetRewindable.nextSolution();
				String playQid = null;
				Role role = new Role();
				for (String key : header) {
					String var = getVarValueAsString(rBind, key);
					if("item".equals(key)) {
						String qid = var.replace("<http://www.wikidata.org/entity/", "").replace(">", "");
						role.setqID(qid);
					} else if("itemLabel".equals(key)) {
						String name;
						if(var.contains(language)) {
							name = var.replace("\"", "").replace("@" + language, "");
						} else {
							name = var.replace("\"", "").split("@")[0];
						}
						if(!Utils.isQId(name)) {
							role.setName(name);
						} else {
							break;
						}
					}  else if("itemDescription".equals(key)) {
						String name = var.replace("\"", "").replace("@" + language, "");
						role.setDescription(name);
					} else if("play".equals(key)) {
						playQid = var.replace("<http://www.wikidata.org/entity/", "").replace(">", "");
					}
				}
				if(playQid != null) {
					if(!queryResults.containsKey(playQid)) {
						queryResults.put(playQid, new LinkedList<Role>());
					}
					queryResults.get(playQid).add(role);
				}
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			qexec.close();
		}
		return queryResults;
	}

	/**
	 * @param language
	 * @return SPARQL query to retrieve all roles in wikidata with the qid, role
	 *         name, description and corresponding play qid in the respective
	 *         language if available otherwise english
	 */
	public static String getPlayRolesQuery(String language) {
		String queryString = "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wd: <http://www.wikidata.org/entity/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#> \n"
				+ "PREFIX schema: <http://schema.org/>\n"
				+ "PREFIX bd: <http://www.bigdata.com/rdf#>\r\n"
				+ "SELECT ?item ?itemLabel ?itemDescription ?play \r\n"
				+ "WHERE \r\n" + "{\r\n"
				+ "  ?item wdt:P31 wd:Q15632617.\r\n"
				+ "  ?item wdt:P1441 ?play.\r\n"
				+ "  ?play wdt:P31 wd:" + Utils.playType
				+ "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"" + language + ",en,[AUTO_LANGUAGE]\". }\r\n"
				+ "}\r\n";
//				+ "} limit 100 \r\n";		
		return queryString;
	}

	/**
	 * Helper method for testing purposes.
	 * @return list of plays based on file passed.
	 */
	public static List<Play> createPlaysFromWikipediaPages() throws FileNotFoundException, IOException {
		List<Play> playList = new LinkedList<Play>();
		String wikiURLPath = "src\\main\\resources\\wikipedia url.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(wikiURLPath)));
		br.readLine();//skip header
		while(br.ready()) {
			String playToProcess = br.readLine();
			playList.add(new Play(playToProcess));
		}
		br.close();
		return playList;
	}
	
}
