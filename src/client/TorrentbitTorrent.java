package client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;

/**
 * Used to get the number of seeders for the specified torrent. In torrentbit.net.
 * @author ivaylo
 *
 */
public class TorrentbitTorrent implements TorrentSeeders {
	public int getSeeders(String torrent){
		Pattern p = Pattern.compile("(get|torrent)/(\\d*)");
		Matcher m = p.matcher(torrent);
		if(m.find()){
			String result = UrlService.readURL("http://torrentbit.net/torrent/"+m.group(2));
			Pattern seed = Pattern.compile("<th>Seeds:</th>\\s*<td id=\"s\"><span class=\"seeds\">(\\d+)</span></td>");
			Matcher seedMatcher = seed.matcher(result);
			if(!seedMatcher.find())return 1;			
			return Integer.parseInt(seedMatcher.group(1));
		}
			
		return 1;
	}

}
