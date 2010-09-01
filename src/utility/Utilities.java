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
	 * @return The number of seeders for the specified torrent it is possible to be get. Otherwise -1 is returned. 
	 */
	public int getSeed(String link){
		TorrentSeeders tmp = null;
		if(link.matches(".*thepiratebay\\.org/.*") ){
			tmp = new ThePirateBayTorrent();
			
		}
		if(link.matches(".*torrentbit\\.net/.*")){
			tmp = new TorrentbitTorrent();
		}
		if(link.matches(".*btjunk\\.org/.*")){
			tmp = new BtJunkTorrent();
		}
		else return -1;
		return tmp.getSeeders(link);
	}
	
	public static boolean isEmpty(String input) {
		return "".equals(input) || input == null;
	}
}
