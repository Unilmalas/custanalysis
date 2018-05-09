import java.io.*;
//import java.net.URL;
//import java.net.HttpURLConnection;
import java.net.*;
import java.util.*;
 
public class FetchPubs {
	
	static boolean isNumeric(String s) {  
		return s != null && s.matches("[-+]?\\d*\\.?\\d+");  
	}

	// run with $ java FetchPubs "parkinson+motor" 5
	public static void main(String[] args) { // to combine terms add with +, e.g. "parkinson+motor"
		String searchTerm = "aspirin";
		int npages = 2;
		if (args.length > 0) searchTerm = args[0];
		if (args.length > 1) npages = Integer.parseInt(args[1]); // how many pages of results to process
		String cookieHeader = "";
		
		String strTemp = "";
		String strRslt = new String("rslt");
		int rsltPos = 0;
		String strPubLink = new String("a href=\"/pubmed"); // beginning of publication link
		// the full link to the publication will then be: https://www.ncbi.nlm.nih.gov/pubmed/29413803
		String strDocTitle = new String("docsum_title"); // title text after this
		int linkPos = 0;
		int titlePos = 0;
		String linkStr = new String("");
		String titleStr = new String("");
		String authStr = new String("");
		String jrnlStr = new String("");
		String yearStr = new String("");
		
		// publication master list as a tree map (unique and sorted)
		TreeMap<String,Publication> pubList = new TreeMap<String,Publication>();
			
		// author master list as HashMap (unique key but unsorted)
		HashMap<String,AuthorLink> authList = new HashMap<String,AuthorLink>();
			
		try {
			URL url = new URL("https://www.ncbi.nlm.nih.gov/pubmed/?term=" + searchTerm + "");
			//URL url = new URL("https://www.linkedin.com/in/xxxxx/");
			//URL url = new URL("https://www.xing.com/profile/xxxxx/");
			//curl -A "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3" -I --url https://www.linkedin.com/company/linkedin
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			
			// read cookies, see http://docs.oracle.com/javase/1.5.0/docs/guide/deployment/deployment-guide/cookie_support.html
			URLConnection connection = url.openConnection();
			List<String> cookies = connection.getHeaderFields().get("Set-Cookie");
			//System.out.println(cookies.toString());
			// temporary to build request cookie header
			StringBuilder sb = new StringBuilder();
			if (cookies != null) {
				for (String cookie : cookies) {
					if (sb.length() > 0) {
						sb.append("; ");
					}
					// only want the first part of the cookie header that has the value
					String value = cookie.split(";")[0];
					sb.append(value);
				}
			}
			// build request cookie header to send on all subsequent requests
			cookieHeader = sb.toString();
			
			// fill publications
			while (null != (strTemp = br.readLine())) {
				rsltPos++;
				//titlePos = strTemp.indexOf(strDocTitle);
				for (String resStr: strTemp.split(strRslt)) {
					linkPos = resStr.indexOf(strPubLink);
					if ( linkPos > -1 ) {
						linkStr = resStr.substring(linkPos+8, linkPos+23); // link
						//System.out.println(linkStr);
					}
					titlePos = resStr.indexOf(strDocTitle);
					if ( titlePos > -1 ) {
						titleStr = resStr.substring(titlePos+14, resStr.indexOf("</a>")); // title
						//System.out.println(titleStr);
						resStr = resStr.substring(resStr.indexOf("</a>")+5, resStr.length());
						authStr = resStr.substring(resStr.indexOf("desc")+6, resStr.indexOf("</p>")-1); // authors
						//System.out.println(authStr);
						resStr = resStr.substring(resStr.indexOf("</p>")+5, resStr.length());
						jrnlStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.indexOf(">", resStr.indexOf("jrnl")+13)-1); // journal
						//System.out.println(jrnlStr);
						resStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.length());
						yearStr = resStr.substring(resStr.indexOf("</span>")+9, resStr.indexOf("</span>")+13); // year
						//System.out.println(yearStr);
						
						if ( !isNumeric(yearStr) )
							yearStr = "0000";
						Publication cPub = new Publication(linkStr, titleStr, authStr, jrnlStr, Integer.parseInt(yearStr));
						pubList.put(titleStr, cPub);
					}
				}
			} // while... readLine
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		// nasty: 2+ result page only accessible via "next page" - finally got this to work: need these form fields filled plus the cookie
		// ( a lot of network-log reading in Chromes developer tools )
		
		// add loop here: process next pages ++++++++++++++++++++++++++++
		int cPage = 1; // have already processed first page
		while ( cPage < npages ) {
			try {
				URL purl = new URL("https://www.ncbi.nlm.nih.gov/pubmed");
				// fill data of POST request
				Map<String,Object> params = new LinkedHashMap<>(); // Map interface maps unique keys to values.
				// form data: EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.CurrPage=3
				params.put("term", searchTerm);
				params.put("EntrezSystem2.PEntrez.PubMed.Pubmed_PageController.PreviousPageName", "results");
				params.put("EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.cPage", "" + (cPage - 1));
				params.put("EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.CurrPage", "" + cPage);
				params.put("EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.cPage", "" + (cPage - 1));
				params.put("EntrezSystem2.PEntrez.DbConnector.Db", "pubmed");
				params.put("EntrezSystem2.PEntrez.DbConnector.Term", searchTerm);
				params.put("EntrezSystem2.PEntrez.DbConnector.LastQueryKey", "1");
				params.put("EntrezSystem2.PEntrez.DbConnector.Cmd", "PageChanged");
				params.put("p$a", "EntrezSystem2.PEntrez.PubMed.Pubmed_ResultsPanel.Pubmed_Pager.Page");
				params.put("p$l", "EntrezSystem2");
				params.put("p$st", "pubmed");
				
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
					conn.setRequestProperty("Cookie", cookieHeader);
					conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
					conn.setDoOutput(true);
					conn.getOutputStream().write(postDataBytes); // write POST request to stream

					// read response
					BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

					while (null != (strTemp = br.readLine())) {
						rsltPos++;
						//titlePos = strTemp.indexOf(strDocTitle);
						for (String resStr: strTemp.split(strRslt)) {
							linkPos = resStr.indexOf(strPubLink);
							if ( linkPos > -1 ) {
								linkStr = resStr.substring(linkPos+8, linkPos+24); // link
								//System.out.println(linkStr);
							}
							titlePos = resStr.indexOf(strDocTitle);
							if ( titlePos > -1 ) {
								titleStr = resStr.substring(titlePos+14, resStr.indexOf("</a>")); // title
								//System.out.println(titleStr);
								resStr = resStr.substring(resStr.indexOf("</a>")+5, resStr.length());
								authStr = resStr.substring(resStr.indexOf("desc")+6, resStr.indexOf("</p>")-1); // authors
								//System.out.println(authStr);
								resStr = resStr.substring(resStr.indexOf("</p>")+5, resStr.length());
								jrnlStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.indexOf(">", resStr.indexOf("jrnl")+13)-1); // journal
								//System.out.println(jrnlStr);
								resStr = resStr.substring(resStr.indexOf("jrnl")+13, resStr.length());
								yearStr = resStr.substring(resStr.indexOf("</span>")+9, resStr.indexOf("</span>")+13); // year
								//System.out.println(yearStr);
								
								// store data in publication treemap
								if ( !isNumeric(yearStr) )
									yearStr = "0000";
								Publication cPub = new Publication(linkStr, titleStr, authStr, jrnlStr, Integer.parseInt(yearStr));
								pubList.put(titleStr, cPub);
							}
						}
					} // while... readLine
					
				} catch (IOException ex) {
					//...
				}
				
			} catch (MalformedURLException ex) {
				//do exception handling here
			}
			cPage++;
		} // while... npages
		// close loop over next pages here ++++++++++++++++++++++++++++++++++++++++++
		
		// process publications and fill author distance matrix (sparse - we attach everything to an author and only fill existing relationships)
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
		} // while... hasNext
		
		// print publist (raw)
		//PrintWriter pw = new PrintWriter(new FileOutputStream(new File("persons.txt"), true /* append = true */));
		try(PrintWriter writer = new PrintWriter("FetchAbstractsOut.txt", "UTF-8");) {
			for (Map.Entry<String, Publication> entry : pubList.entrySet()) {
				//System.out.println("Key: " + entry.getKey() + ". Value: " + entry.getValue().link);
				//System.out.println(entry.getKey() + ";" + entry.getValue().link);
				writer.println(entry.getKey() + ";" + entry.getValue().link + ";" + entry.getValue().year);
			}
			writer.close();
		} catch (IOException e) {
			System.err.println(e);
		}
	} // main
}