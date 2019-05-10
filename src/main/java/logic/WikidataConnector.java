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

import valueobject.Opera;
import valueobject.Role;

public class WikidataConnector {

	
	/**
	 * Executes a SPARQL query to retrieve all operas in wikidata with the name and
	 * url to wikipedia by the language requested.
	 * 
	 * Language in short (i.e. en, fr, de)
	 * 
	 * 
	 * @param language
	 */
	public static List<Opera> getOperasInWikidataByLanguage(String language) {
		List<Opera> queryResults = new LinkedList<Opera>();
		String queryString = getOperasSPARQLQuery(language);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("https://query.wikidata.org/sparql", queryString);
		try {
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultSetRewindable = ResultSetFactory.makeRewindable(results);
			List<String> header = new LinkedList<String>();
			for (int col = 0; col < resultSetRewindable.getResultVars().size(); col++) {
				header.add(results.getResultVars().get(col));
			}
			
			
			while (resultSetRewindable.hasNext()) {
				Opera opera = extractOpera(language, resultSetRewindable, header);
				addOpera(queryResults, opera);
			}
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		} finally {
			qexec.close();
		}
		
		for (Opera opera : queryResults) {
			opera.getqID();
		}
		return queryResults;
	}


	private static void addOpera(List<Opera> queryResults, Opera opera) {
		if(opera != null) {
			boolean load = addComponistIfOperaAlreadyExists(queryResults, opera);
			if(load) {
				queryResults.add(opera);
			}
		}
	}


	/**
	 * @param queryResults
	 * @param opera
	 * @return false if opera already exists
	 */
	private static boolean addComponistIfOperaAlreadyExists(List<Opera> queryResults, Opera opera) {
		boolean load = true;
		for (Opera operaLoaded : queryResults) {
			if(operaLoaded.getqID().equals(opera.getqID())) {
				operaLoaded.getComponist().addAll(opera.getComponist());
				load = false;
			}
		}
		return load;
	}

	private static Opera extractOpera(String language, ResultSetRewindable resultSetRewindable, List<String> header) {
		QuerySolution rBind = resultSetRewindable.nextSolution();
		Opera opera = new Opera();
		for (String key : header) {
			String var = getVarValueAsString(rBind, key);
			if("item".equals(key)) {
				String qid = var.replace("<http://www.wikidata.org/entity/", "").replace(">", "");
				opera.setqID(qid);
			} else if("itemLabel".equals(key)) {
				String name = var.replace("\"", "").replace("@" + language, "");
				opera.setName(name);
			}  else if("componistLabel".equals(key)) {
				String name = var.replace("\"", "").replace("@" + language, "");
				opera.setComponist(Lists.newArrayList(name));
			} else if("article".equals(key)) {
				String url = var.replace("<", "").replace(">", "");
				opera.setUrl(url);
			}
		}
		//we only want the operas which have a url
		if("".equals(opera.getUrl())) {
			return null;
		}
		return opera;
	}
	
	/**
	 * @param language
	 * @return SPARQL query to retreive all operas in wikidata with the qid, componist and link to the wikipedia page in the respective language
	 */
	private static String getOperasSPARQLQuery(String language) {
		String queryString = "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n" 
				+ "PREFIX wd: <http://www.wikidata.org/entity/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#> \n" 
				+ "PREFIX schema: <http://schema.org/>\n" 
				+ "PREFIX bd: <http://www.bigdata.com/rdf#>\r\n"
				+ "SELECT ?item ?itemLabel "
				+ "?article ?componistLabel \r\n" 
				+ "WHERE {\r\n" 
				+ "  ?item wdt:P31 wd:Q1344.\r\n" 
				+ "  ?item wdt:P86 ?componist. \r\n"
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
	 * @return a map with all roles corresponding to an opera. key is the opera  qid
	 */
	public static Map<String, List<Role>> getOperaRolesWikidataByLanguage(String language) {
		Map<String, List<Role>> queryResults = new HashMap<String, List<Role>>();
		String queryString = getOperaRolesQuery(language);
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
				String operaQid = null;
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
					} else if("opera".equals(key)) {
						operaQid = var.replace("<http://www.wikidata.org/entity/", "").replace(">", "");
					}
				}
				if(operaQid != null) {
					if(!queryResults.containsKey(operaQid)) {
						queryResults.put(operaQid, new LinkedList<Role>());
					}
					queryResults.get(operaQid).add(role);
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
	 *         name, description and corresponding opera qid in the respective
	 *         language if available otherwise english
	 */
	private static String getOperaRolesQuery(String language) {
		String queryString = "PREFIX wdt: <http://www.wikidata.org/prop/direct/>\n"
				+ "PREFIX wd: <http://www.wikidata.org/entity/>\n"
				+ "PREFIX wikibase: <http://wikiba.se/ontology#> \n"
				+ "PREFIX schema: <http://schema.org/>\n"
				+ "PREFIX bd: <http://www.bigdata.com/rdf#>\r\n"
				+ "SELECT ?item ?itemLabel ?itemDescription ?opera \r\n"
				+ "WHERE \r\n" + "{\r\n"
				+ "  ?item wdt:P31 wd:Q50386450.\r\n"
				+ "  ?item wdt:P1441 ?opera.\r\n"
				+ "  SERVICE wikibase:label { bd:serviceParam wikibase:language \"" + language + ",en,[AUTO_LANGUAGE]\". }\r\n"
				+ "}\r\n";
//				+ "}\r\n"		
//				+ "limit 100";
		return queryString;
	}

	/**
	 * Helper method for testing purposes.
	 * @return list of operas based on file passed.
	 */
	public static List<Opera> createOperaFromWikipediaPages() throws FileNotFoundException, IOException {
		List<Opera> operaList = new LinkedList<Opera>();
		String wikiURLPath = "src\\main\\resources\\wikipedia url.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(wikiURLPath)));
		br.readLine();//skip header
		while(br.ready()) {
			String operaToProcess = br.readLine();
			operaList.add(new Opera(operaToProcess));
		}
		br.close();
		return operaList;
	}
	
}
