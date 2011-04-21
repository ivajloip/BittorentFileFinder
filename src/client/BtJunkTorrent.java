package client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;
import utility.Utilities;

/**
 * Used to get the number of seeders for the specified torrent in btjunk.ort.
 * @author ivaylo
 *
 */
public class BtJunkTorrent implements TorrentSeeders{
	/**
	 * Checks how much seed is there for a torrent
	 * @param torrent Link to the torrent that we are interested in
	 * @return The number of seeders available. If the number of seeders can not be determined, however the torrent is 
	 * available, Integer.MAX_VALUE is returned. If the torrent is not a valid torrent location or some other error 
	 * occurs, -1 is returned.
	 */
	public int getSeeders(String torrent) {
		String link = torrent.substring(0, torrent.length() - 16);
		
		String result = UrlService.readURL(link);
		if(Utilities.isEmpty(result)) {
			return -1;
		}
		
		Pattern seed = Pattern.compile("(\\d+)\\s*seeds \\| (\\d+) peers");
		Matcher seedMatcher = seed.matcher(result);
		if(!seedMatcher.find()) {
			return Integer.MAX_VALUE;			
		}
		
		return Integer.parseInt(seedMatcher.group(1));
	}

}
