
import java.io.*;
//import java.net.URL;
//import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
 
public class FetchAbstracts {
	
	static boolean isNumeric(String s) {  
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}

	// run with $ java FetchAbstractsFT /pubmed/29407464
	public static void main(String[] args) { // /pubmed/29407464
		String cLink = "";
		if (args.length > 0) cLink = args[0];
		String strAbsID = "<div class=\"abstr\">";
		String strKeywrdID = "<div class=\"keywords\">";
		String strTemp = "";
		String abstrStr = "";
		String keyStr = "";
		String workStr = "";
		int absPos = 0;
		int keyPos = 0;
		
		List<Abstract> aStorage = new ArrayList<Abstract>(); // array list for abstracts raw storage
		Abstract tempA = new Abstract("","","","");

		try {
			
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
						int sepidx = line.indexOf(";");
						if ( sepidx > -1 ) {
							tempA.title = line.substring(0, sepidx);
							tempA.link = line.substring(sepidx+1, line.length());
						}
						//System.out.println(tempA.title);
						aStorage.add(new Abstract(tempA.link, tempA.title,"",""));
					}
				}
				String everything = sb.toString();
				//System.out.println(everything);
			} finally {
				pubbr.close();
			}
			
			//System.out.println("++++++++++++++++++++++++++");
			
			// might need to wait a random time to hide robot
			//TimeUnit.SECONDS.sleep(1); // java.util.concurrent.TimeUnit
			
			//for (Abstract thisA : aStorage) { // process all pubs just read
			for ( int i=0; i<aStorage.size(); i++) {
				//System.out.println("xxxxxx " + aStorage.get(i).title);
				tempA.title = aStorage.get(i).title;
				tempA.link = aStorage.get(i).link;
				// access URL and read out abstract and keywords
				URL url = new URL("https://www.ncbi.nlm.nih.gov" + tempA.link);
				BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
				URLConnection connection = url.openConnection();
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
						keyStr = strTemp.substring(keyPos+23, workStr.indexOf("</div>")+keyPos+23); // link
						//System.out.println(keyStr);
						tempA.keywords = keyStr;
					}
				} // while... readLine
				aStorage.set(i, new Abstract(tempA.link, tempA.title, tempA.text, tempA.keywords));
				//System.out.println(aStorage.get(i));
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