import java.util.*;

// AuthorLink class
public class AuthorLink {
	String author;
	HashMap<String,Integer> authLinked; // the higher the link value the stronger the link
	
	// constructors of AuthorLink
		public AuthorLink() {
		this.author = "";
		authLinked = new HashMap<String,Integer>();
	}
	public AuthorLink(String author) {
		this.author = author;
		authLinked = new HashMap<String,Integer>();
	}
	
	// add linked author
	public int LinkAuthor(String linkauth, int distance) {
		// check if already linked
		if ( authLinked.containsKey(linkauth) ) {
			// linked: add distance points to existing value
			int currlinkval = (int)authLinked.get(linkauth);
			authLinked.put(linkauth, currlinkval + distance);
			//System.out.println("link increased");
			return 1;
		} else {
			// not linked yet: add link
			authLinked.put(linkauth, distance);
			//System.out.println("link added");
			return 2;
		}
	}
	
	// print linked authors
	public String PrintLinkedAuth() {
		String retStr = "";
		for(Map.Entry<String,Integer> entry : authLinked.entrySet()) {
			String key = entry.getKey();
			int value = entry.getValue();
			if ( value > 0 )
				//System.out.print(key + " - " + value + " ");
				retStr += " " + key + " - " + value + " ";
		}
		return retStr;
	}
}