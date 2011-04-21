package client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;
import utility.Utilities;

/**
 * Used to get the number of seeders for the specified torrent. In torrentbit.net.
 * @author ivaylo
 *
 */
public class TorrentbitTorrent implements TorrentSeeders {
	/**
	 * Checks how much seed is there for a torrent
	 * @param torrent Link to the torrent that we are interested in
	 * @return The number of seeders available. If the number of seeders can not be determined, however the torrent is 
	 * available, Integer.MAX_VALUE is returned. If the torrent is not a valid torrent location or some other error 
	 * occurs, -1 is returned.
	 */
	public int getSeeders(String torrent){
		Pattern p = Pattern.compile("(get|torrent)/(\\d*)");
		Matcher m = p.matcher(torrent);
		if(m.find()){
			String result = UrlService.readURL("http://torrentbit.net/torrent/"+m.group(2));
			if(Utilities.isEmpty(result)) {
				return -1;
			}
			
			Pattern seed = Pattern.compile("<th>Seeds:</th>\\s*<td id=\"s\"><span class=\"seeds\">(\\d+)</span></td>");
			Matcher seedMatcher = seed.matcher(result);
			if(!seedMatcher.find()) {
				return Integer.MAX_VALUE;			
			}
			return Integer.parseInt(seedMatcher.group(1));
		}
			
		return -1;
	}

}
