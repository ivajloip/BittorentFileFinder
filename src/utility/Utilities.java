package utility;

import client.BtJunkTorrent;
import client.ThePirateBayTorrent;
import client.TorrentSeeders;
import client.TorrentbitTorrent;

/**
 * @author ivaylo
 *
 */
public class Utilities {
	/**
	 * Tries to get the number of seeders for the specified torrent.
	 * @param link The location of the torrent whose seeders are to be get.
	 * @return The number of seeders available. If the number of seeders can not be determined, however the torrent is 
	 * available, Integer.MAX_VALUE is returned. If the torrent is not a valid torrent location or some other error 
	 * occurs, -1 is returned.
	 */
	public static int getSeed(String link){
		TorrentSeeders tmp = null;
		if(link.matches(".*thepiratebay\\.org/.*") ){
			tmp = new ThePirateBayTorrent();
			
		}
		else if(link.matches(".*torrentbit\\.net/.*")){
			tmp = new TorrentbitTorrent();
		}
		else if(link.matches(".*btjunkie\\.org/.*")){
			tmp = new BtJunkTorrent();
		}
		else return -1;
		return tmp.getSeeders(link);
	}
	
	/**
	 * Checks if a string is an empty string
	 * @param input Some sting
	 * @return Whether a String is an empty string.
	 */
	public static boolean isEmpty(String input) {
		return "".equals(input) || input == null;
	}
}
