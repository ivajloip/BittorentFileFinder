package client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;

/**
 * Used to get the number of seeders for the specified torrent in btjunk.ort.
 * @author ivaylo
 *
 */
public class BtJunkTorrent implements TorrentSeeders{

	@Override
	public int getSeeders(String torrent) {
		String link = torrent.substring(0, torrent.length() - 16);
		String result = UrlService.readURL(link);
		Pattern seed = Pattern.compile("(\\d+)\\s*seeds \\| (\\d+) peers");
		Matcher seedMatcher = seed.matcher(result);
		if(!seedMatcher.find())return 0;			
		return Integer.parseInt(seedMatcher.group(1));
	}

}
