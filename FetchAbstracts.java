
import java.io.*;
//import java.net.URL;
//import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
//import java.util.concurrent;
 
public class FetchAbstracts {
	
	static boolean isNumeric(String s) {  
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}

	// use: first: $java FetchPubs "xxxx+xxx+xxx..." 20 (or any number of results pages), then run this
	// run with $ java FetchAbstracts /pubmed/29407464
	// OR run with $ java FetchAbstracts 2018
	public static void main(String[] args) { // /pubmed/29407464 ***OR*** read from file FetchAbstractsOut.txt ***OR*** choose year and read from file
		String cLink = "";
		String cYear = "";
		if (args.length > 0) { // either a specific link or year of the publications from the text file
			if ( isNumeric(args[0]) ) {
				cYear = args[0];
			} else {
				cLink = args[0];
			}
		}
		String strAbsID = "<div class=\"abstr\">";
		String strKeywrdID = "<div class=\"keywords\">";
		String strTemp = "";
		String abstrStr = "";
		String keyStr = "";
		String workStr = "";
		int absPos = 0;
		int keyPos = 0;
		
		List<Abstract> aStorage = new ArrayList<Abstract>(); // array list for abstracts raw storage
		Abstract tempA = new Abstract("","","","","");
		
		//System.out.println(cYear + " : " + args.length);

		try {
			if ( args.length == 0 || cYear != "" ) {
				// read file with paper title and link
				BufferedReader pubbr = new BufferedReader(new FileReader("FetchAbstractsOut.txt"));
				try {
					StringBuilder sb = new StringBuilder();
					String line = pubbr.readLine();
					while (line != null) {
						sb.append(line);
						sb.append(System.lineSeparator());
						line = pubbr.readLine();
						if ( line != null ) {
							int sepidx = line.indexOf(";/pubmed"); // publication pubmed-id
							if ( sepidx > -1 ) {
								tempA.title = line.substring(0, sepidx);
								tempA.link = line.substring(sepidx+1, line.length()-5); // line length minus year
								tempA.year = line.substring(line.length()-4, line.length());
							}
							//System.out.println(tempA.title);
							aStorage.add(new Abstract(tempA.link, tempA.title ,"" ,tempA.year, "")); // Integer.parseInt(yearStr) ???
						}
					}
					String everything = sb.toString();
					//System.out.println(everything);
				} finally {
					pubbr.close();
				}
			}
			
			Random rand = new Random();
			
			//for (Abstract thisA : aStorage) { // process all pubs just read
			for ( int i=0; i<aStorage.size(); i++) {
				//System.out.println("xxxxxx " + aStorage.get(i).title);
				try {
					tempA.title = aStorage.get(i).title;
					tempA.link = aStorage.get(i).link;
					tempA.year = aStorage.get(i).year;

					if ( cYear.equals("") || cYear.equals(tempA.year) ) { // either process all pubs or the ones for the year chosen
						// access URL and read out abstract and keywords
						URL url = new URL("https://www.ncbi.nlm.nih.gov" + tempA.link);
						System.out.println("accessing " + url);
						BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
						URLConnection connection = url.openConnection();
						// might need to wait a random time to hide robot
						//java.util.concurrent.TimeUnit.SECONDS.sleep(rand.nextInt(2) + 1); // java.util.concurrent.TimeUnit
						java.util.concurrent.TimeUnit.SECONDS.sleep(1);
						// read abstract
						while (null != (strTemp = br.readLine())) {
							// abstracts come in different flavors
							// identifier: <div class="abstr">
							absPos = strTemp.indexOf(strAbsID);
							keyPos = strTemp.indexOf(strKeywrdID);
							if ( absPos > -1 && keyPos > -1 ) {
								abstrStr = strTemp.substring(absPos+20, keyPos-1); // link
								//System.out.println(abstrStr);
								tempA.text = abstrStr;
							}										
							// identifier: <div class="keywords">
							if ( keyPos > -1 ) {
								workStr = strTemp.substring(keyPos+23, strTemp.length()-1);
								int dividx = workStr.indexOf("</div>");
								if (dividx > 0)
									keyStr = strTemp.substring(keyPos+23, dividx+keyPos+23); // link
								//System.out.println(keyStr);
								tempA.keywords = keyStr;
							}
						} // while... readLine
						aStorage.set(i, new Abstract(tempA.link, tempA.title, tempA.text, tempA.year, tempA.keywords));
						//System.out.println(aStorage.get(i).year);
						//System.out.println(aStorage.get(i));
					}
				} catch (IOException e) {
					System.err.println(e);
				}
			} // for... Abstract

		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// print abstracts
		//for (Abstract thisA : aStorage) {
		//	System.out.println(thisA.title + thisA.text + thisA.keywords);
		//}
		
		// print publist (raw)
		//PrintWriter pw = new PrintWriter(new FileOutputStream(new File("persons.txt"), true /* append = true */));
		try(PrintWriter writer = new PrintWriter("AbstractsFull.txt", "UTF-8");) {
			for (Abstract thisA : aStorage) {
				writer.println(thisA.title + thisA.text + thisA.keywords);
			}
			writer.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	  
	} // main
}