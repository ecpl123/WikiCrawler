package wikiCrawl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Hashtable;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

public class JankyWikipediaCrawler {

	public static void main(String[] args) throws IOException{
		Document doc;
		String startLinkArg = args[0];
		Pair<String, Integer> startLink = new Pair<String, Integer>(startLinkArg, 0);
		String domainPrefix = args[1];
		int depth = Integer.parseInt(args[2]);
		int iterations = 0;
		Hashtable<Integer, String> visitedLink = new Hashtable<Integer, String>();
		String urlTitle = "";
		Queue<Pair<String, Integer>> linkQueue = new LinkedList<Pair<String, Integer>>();
		linkQueue.add(startLink);

		outerloop:
			while(linkQueue.isEmpty() != true) {
				String urlString = "";
				doc = Jsoup.connect("https://www.google.com").get();
				//NEED TO TRY CONNECTING TO NEXT LINK WHILE THE CURRENT LINK GIVES AN ERROR

				boolean done = false;
				while(!done) {
					String fileTitle = "";
					try {
						//System.out.println("Trying! ");
						String currLink = linkQueue.remove().getFirst();
						urlString = currLink;
						doc = Jsoup.connect(currLink).get();
						doc.setBaseUri(domainPrefix);
						int hash = currLink.hashCode();
						visitedLink.put(hash, currLink);
						urlTitle = doc.location();
						done = true;
					}
					catch(HttpStatusException e) {
						//System.out.println("Caught! ");
						linkQueue.remove();
						if(linkQueue.isEmpty() == true && iterations <= 0) {
							break outerloop;
						}
					}
				}
				//System.out.println("\nTHIS IS THE CURRENT QUEUE SIZE: " + linkQueue.size());
				if(linkQueue.isEmpty()== true) {
					System.out.println(
							  "================================================\n"
										+ "================================================\n"
										+ "==================END ITERATION==================\n"
										+ "================================================\n"
										+ "================================================\n");



					iterations++;
				}

				Elements tables = doc.getElementsByTag("table");
				Elements links = doc.getElementsByTag("a");
				
				continuetable:
				for(Element table: tables) {
						boolean documentThis = true;
						Elements captions = table.select("caption");
						if(captions.isEmpty() == true) {
							documentThis = false;
							continue continuetable;
						}
						
						int rowCount = 0;
						boolean headersFound = false;
						boolean cellsFound = false;
						Elements tableRows = table.select("tr");
						
						String fileTitle = "";
						
						String captionString = "";
						List<String> headersToFile = new ArrayList<String>();
						ArrayList<List<String>> cellsToFile = new ArrayList<List<String>>();
						
						
						
						
						for(Element tableRow: tableRows) {

							//System.out.println("rowCount = " + rowCount);
							List<String> header = tableRow.select("th").eachText();
							List<String> cell = tableRow.select("td").eachText();
							String tr = "\n\"\"";
							if(header.isEmpty() == false && rowCount == 0) {

								System.out.print("\n" + urlTitle);
								for(Element caption: captions) {
									String c = caption.ownText();
									String cforfile = c.replaceAll(" ", "");
									captionString = "\"Table\",\"" + c + "\"";
									fileTitle = urlString.substring(domainPrefix.length()) + "_" + cforfile +".txt";
									String tableTag = "\nTable, ";
									System.out.println(tableTag + c);
								}

								header.stream()
								.map(r -> r.replace("\"", ""))
								.collect(Collectors.toList());
								System.out.print("\nHeadings");
								for(int i = 0; i < header.size(); i++) {
									System.out.print(", " + header.get(i));
								}
								headersToFile = header;
								headersFound = true;
								rowCount++;
							}
							else if(headersFound == true && header.isEmpty() == false) {
								documentThis = false;
								continue continuetable;
							}
							if(cell.isEmpty() != true && headersFound == true) {
									System.out.print(tr);
									cell.stream()
									.map(r -> r.replace("\"", ""))
									.collect(Collectors.toList());
									for(int i = 0; i < cell.size(); i++) {
										System.out.print(", " + cell.get(i));
								}
									cellsFound = true;
									cellsToFile.add(cell);
								rowCount++;
							}


						}
						if(headersFound == true) {
							System.out.println("\n==================================");

						}
						if(headersFound == true && cellsFound == true && documentThis == true) {
							try {
								String currDir = System.getProperty("user.dir");
								FileWriter writer = new FileWriter(currDir + fileTitle);
								writer.write("\"" + urlString + "\"");
								writer.write("\n");
								writer.write(captionString);
								writer.write("\n");
								writer.write("\n");
								writer.write("\"Heading,\"");
								for(String heading: headersToFile) {
									writer.write(",\""+ heading + "\"");
								}
								writer.write("\n");
								for(List<String> cells: cellsToFile) {
									writer.write("\"\"");
									for(String cell: cells) {
										writer.write(",\""+ cell + "\"");
									}
									writer.write("\n");
								}
								writer.close();
								System.out.println("Successfully wrote to the file.");
								System.out.println(currDir + fileTitle);
							} catch (IOException e) {
								System.out.println("An error occurred.");
								e.printStackTrace();
							}
						}
					}





				if(iterations <= depth) {
				for(Element link: links){
					
					Pair<String, Integer> l = new Pair<String, Integer>(link.absUrl("href"), iterations);
					//System.out.println("\n\nORIGINAL LINK: " + l);

					if(l.getSecond() == iterations && l.getFirst().length() >= domainPrefix.length() && l.getFirst().substring(0, domainPrefix.length()).equals(domainPrefix)) {
						//System.out.println("Final link: " + l);
						if(visitedLink.contains(l) == false) {
							if(linkQueue.contains(l) == false) {
								linkQueue.add(l);
								//System.out.println("SUCCESS");
							}
						}
					}


				}
			}
				
		}
		
	}

}

