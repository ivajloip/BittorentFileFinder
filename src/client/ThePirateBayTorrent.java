package client;

import java.util.regex.*;

import services.UrlService;
import utility.Utilities;

/**
 * Used to get the number of seeders for the specified torrent. In thepiratebay.org.
 * @author ivaylo
 *
 */
public class ThePirateBayTorrent implements TorrentSeeders {
	/**
	 * Checks how much seed is there for a torrent
	 * @param torrent Link to the torrent that we are interested in
	 * @return The number of seeders available. If the number of seeders can not be determined, however the torrent is 
	 * available, Integer.MAX_VALUE is returned. If the torrent is not a valid torrent location or some other error 
	 * occurs, -1 is returned.
	 */
	public int getSeeders(String torrent) {
		Pattern p = Pattern.compile("org/(\\d*)/");
		Matcher m = p.matcher(torrent);
		if(m.find()) {
			String result = UrlService.readURL("http://thepiratebay.org/torrent/"+m.group(1));
			if(Utilities.isEmpty(result)) {
				return -1;
			}
			
			Pattern seed = Pattern.compile("<dt>Seeders:</dt>\\s*<dd>(\\d+)</dd>");
			Matcher seedMatcher = seed.matcher(result);
			if(!seedMatcher.find()) {
				return Integer.MAX_VALUE;			
			}
			return Integer.parseInt(seedMatcher.group(1));
		}
			
		return -1;
	}

}
