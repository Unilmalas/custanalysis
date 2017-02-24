
import java.io.*;
//import java.net.URL;
//import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
 
public class FetchURLData {
 
	public static void main(String[] args) {
		String searchTerm = "aspirin";
		if (args.length > 0) searchTerm = args[0];
			
		try {
			URL url = new URL("https://www.ncbi.nlm.nih.gov/pubmed/?term=" + searchTerm + "");
			//URL url = new URL("https://www.linkedin.com/in/xxxxx/");
			//URL url = new URL("https://www.xing.com/profile/xxxxx/");
			//curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3" -I --url https://www.linkedin.com/company/linkedin
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String strTemp = "";
			String strRslt = new String("rslt");
			int rsltPos = 0;
			String strDocTitle = new String("docsum_title");
			int titlePos = 0;
			String titleStr = new String("");
			String authStr = new String("");
			String jrnlStr = new String("");
			String yearStr = new String("");
			
			// publication master list as a tree map (unique and sorted)
			TreeMap<String,Publication> pubList = new TreeMap<String,Publication>();
			
			// author master list as HashMap (unique key but unsorted)
			HashMap<String,AuthorLink> authList = new HashMap<String,AuthorLink>();
			
			// fill publications
			while (null != (strTemp = br.readLine())) {
				rsltPos++;
				//titlePos = strTemp.indexOf(strDocTitle);
				for (String resStr: strTemp.split(strRslt)) {
					titlePos = resStr.indexOf(strDocTitle);
					if ( titlePos > -1 ) {
						titleStr = resStr.substring(titlePos+14, resStr.indexOf("</a>")); // title
						System.out.println(titleStr);
						resStr = resStr.substring(resStr.indexOf("</a>")+5, resStr.length());
						authStr = resStr.substring(resStr.indexOf("desc")+6, resStr.indexOf("</p>")-1); // authors
						System.out.println(authStr);
						resStr = resStr.substring(resStr.indexOf("</p>")+5, resStr.length());
						jrnlStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.indexOf(">", resStr.indexOf("jrnl")+13)-1); // journal
						System.out.println(jrnlStr);
						resStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.length());
						yearStr = resStr.substring(resStr.indexOf("</span>")+9, resStr.indexOf("</span>")+13); // year
						System.out.println(yearStr);
						System.out.println("***********************");
						
						// store data in publication treemap
						Publication cPub = new Publication(titleStr, authStr, jrnlStr, Integer.parseInt(yearStr));
						pubList.put(titleStr, cPub);
					}
				}
			}
			
			// process publications and fill author distance matrix
			// Get a set of the entries
			Set pubSet = pubList.entrySet(); // A Set is a Collection that cannot contain duplicate elements
			// Get an iterator
			Iterator pubIt = pubSet.iterator();
			// Display elements
			while(pubIt.hasNext()) {
				Map.Entry<String,Publication> me = (Map.Entry<String,Publication>)pubIt.next(); // The Map interface maps unique keys to values, Map.Entry interface enables you to work with a map entry
				//System.out.println(me.getKey() + ": ");
				Publication value = me.getValue();
				//System.out.println(value.authors);
				// get authors from current publication
				String[] pubAuthors = value.authors.split(",");
				for ( int i=0; i<pubAuthors.length; i++ ) { // loop over 1st author
					//System.out.println("Author " + i + " : " + pubAuthors[i].trim());
					AuthorLink cAuthor = new AuthorLink();
					// author in author HashMap? add if not
					if ( authList.containsKey(pubAuthors[i].trim()) ) {
						cAuthor = authList.get(pubAuthors[i].trim());
						//System.out.println("Author found! " + cAuthor.author + " :: " + cAuthor.authLinked.toString());
					} else {
						cAuthor = new AuthorLink(pubAuthors[i].trim());
						authList.put(pubAuthors[i].trim(), cAuthor);
						//System.out.println("Adding author! " + cAuthor.author + " :: " + cAuthor.authLinked.toString());
					}
					for ( int j=i+1; j<pubAuthors.length; j++ ) { // inner loop over coauthors
						// process coauthors: add to list if needed and add link-points
						// coauthor in author HashMap? - this will be handled by the AuthorLink-class
						cAuthor.LinkAuthor(pubAuthors[j].trim(), 1);
					}
				}
			}
			
			// test output
			for(Map.Entry<String,AuthorLink> entry : authList.entrySet()) {
				String key = entry.getKey();
				AuthorLink value = entry.getValue();
				//System.out.println(key + " : " + value.PrintLinkedAuth());
				System.out.println(value.author + " : " + value.PrintLinkedAuth());
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		/*
		
		// nasty: 2+ result page only accessible via "next page" - did not get this to work via POST...
		
		try {
			URL purl = new URL("https://www.ncbi.nlm.nih.gov/pubmed/?term=Ruxolitinib");
			
			// fill data of POST request
			Map<String,Object> params = new LinkedHashMap<>(); // Map interface maps unique keys to values.
			//params.put("name", "Freddie the Fish");
			//params.put("email", "fishie@seamail.example.com");
			//params.put("reply_to_thread", 10394);
			//params.put("message", "somemessage xxxxxxxxxxx.");
			// form data: EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.CurrPage=3
			params.put("EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.Page", "2");
			
			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String,Object> param : params.entrySet()) { // loops over arrays: int[] nums = ...; for (int num : nums) ...
				if (postData.length() != 0) postData.append('&'); // appends the char argument as string to the StringBuilder
				try {
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				} catch (UnsupportedEncodingException ex) {
					// ...
				}
			}

			// build POST request from data and execute
			try {
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");
				HttpURLConnection conn = (HttpURLConnection)purl.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				conn.setDoOutput(true);
				conn.getOutputStream().write(postDataBytes); // write POST request to stream

				// read response
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				for (int c; (c = br.read()) >= 0;)
					System.out.print((char)c);
				String strTemp = "";
				String strRslt = new String("rslt");
				int rsltPos = 0;
				String strDocTitle = new String("docsum_title");
				int titlePos = 0;
				String outStr = new String("");
				while (null != (strTemp = br.readLine())) {
					rsltPos++;
					//titlePos = strTemp.indexOf(strDocTitle);
					for (String resStr: strTemp.split(strRslt)) {
						titlePos = resStr.indexOf(strDocTitle);
						if ( titlePos > -1 ) {
							outStr = resStr.substring(titlePos+14, resStr.indexOf("</a>")); // title
							System.out.println(outStr);
							resStr = resStr.substring(resStr.indexOf("</a>")+5, resStr.length());
							outStr = resStr.substring(resStr.indexOf("desc")+6, resStr.indexOf("</p>")); // authors
							System.out.println(outStr);
							System.out.println("***********************");
						}
					}
				}
				
			} catch (IOException ex) {
				//...
			}
			
		} catch (MalformedURLException ex) {
			//do exception handling here
		} */
	}
}