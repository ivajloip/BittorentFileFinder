package client;

import java.util.regex.*;

import services.UrlService;

/**
 * Used to get the number of seeders for the specified torrent. In thepiratebay.org.
 * @author ivaylo
 *
 */
public class ThePirateBayTorrent implements TorrentSeeders {
	public int getSeeders(String torrent){
		Pattern p = Pattern.compile("org/(\\d*)/");
		Matcher m = p.matcher(torrent);
		if(m.find()){
			String result = UrlService.readURL("http://thepiratebay.org/torrent/"+m.group(1));
			Pattern seed = Pattern.compile("<dt>Seeders:</dt>\\s*<dd>(\\d+)</dd>");
			Matcher seedMatcher = seed.matcher(result);
			if(!seedMatcher.find())return 0;			
			return Integer.parseInt(seedMatcher.group(1));
		}
			
		return 0;
	}

}
