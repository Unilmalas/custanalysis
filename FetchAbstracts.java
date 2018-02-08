
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

		try {
			
			/*
			// read file with paper title and link
			BufferedReader br = new BufferedReader(new FileReader("file.txt"));
			try {
				StringBuilder sb = new StringBuilder();
				String line = br.readLine();

				while (line != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
				String everything = sb.toString();
			} finally {
				br.close();
			}*/
			
			// might need to wait a random time to hide robot
			//TimeUnit.SECONDS.sleep(1); // java.util.concurrent.TimeUnit
			
			// access URL and read out abstract and keywords
			URL url = new URL("https://www.ncbi.nlm.nih.gov" + cLink);

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
					System.out.println(abstrStr);
				}										
				// identifier: <div class="keywords">
				if ( keyPos > -1 ) {
					workStr = strTemp.substring(keyPos+23, strTemp.length()-1);
					keyStr = strTemp.substring(keyPos+23, workStr.indexOf("</div>")+keyPos+23); // link
					System.out.println(keyStr);
				}		
			} // while... readLine
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// print publist (raw)
		//PrintWriter pw = new PrintWriter(new FileOutputStream(new File("persons.txt"), true /* append = true */));
		/*try(PrintWriter writer = new PrintWriter("FetchAbstractsOut.txt", "UTF-8");) {
			for (Map.Entry<String, Publication> entry : pubList.entrySet()) {
				//System.out.println("Key: " + entry.getKey() + ". Value: " + entry.getValue().link);
				//System.out.println(entry.getKey() + ";" + entry.getValue().link);
				writer.println(entry.getKey() + ";" + entry.getValue().link);
			}
			writer.close();
		} catch (IOException e) {
			System.err.println(e);
		}*/
	} // main
}