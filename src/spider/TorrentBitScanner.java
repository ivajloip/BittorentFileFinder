package spider;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;

/**
 * Scanner (spider) for torrentbit.net tracker.
 * @author ivaylo
 *
 */
public class TorrentBitScanner extends AbstractTrackerScanner{
	public TorrentBitScanner(String host, int port) {
		init(host, port);
	}
	
	/**
	 * Starts to go through the pages in the tracker and to add torrents from each of them.
	 * @throws IOException
	 */
	public void scan() throws IOException{
		boolean flag;
		int page;
		
		System.out.println("TorrentBitScanner starts to work");
		
		String[] genres = new String[]{"Action", "Alternative", "Ambient", "Anime", "Asian", "Balad", "Blues", "Chart%20Listings", "Christian", "Christmas", "Classic", "Classic%20Pop/Rock", "Classical", 
				"Comedy", "Compilation", "Compilation%20/%20Various%20Artists%20(VA)", "Concert%20Videos", "Country%20/%20Western", "Disco", "Drum%20N%20Bass", "Dubstep", "Easy%20Listening", 
				"Electronic", "Ethnic", "Folk", "Funk", "Game%20Music", "Gospel", "Gothic", "HardHouse/Old%20School%20Radio%20Mixes", "Hardcore", "Hardrock", "Hardstyle/Jump", 
				"Heavy/Death%20Metal", "Hip%20Hop", "Indie", "Indie%20/%20Britpop", "Industrial", "Instrumental", "International", "Jazz", "Karaoke", "Latin", "Linux", "Live/Concert", 
				"Lo-Fi", "Lounge", "MP3", "Melodic", "Metal", "Motown", "Music%20-%20Other", "Music%20Videos", "NewAge", "Non-English", "Now%20That&#039;s%20What%20I%20Call%20Music", "Other", "Pop", 
				"Psychedelic", "Punk", "R&amp;B", "Radio%20shows", "Rap", "Reggae", "Rock", "Rock%20&#039;n&#039;%20Roll", "Runk", "Singer%20Songwriter", "Ska", "Soul", "Soundtracks", 
				"Spanish", "Techno", "Trance%20/%20House%20/%20Dance", "Unsigned/Amateur", "Unsorted", "Video%20clips", "World", "lossless/FLAC"};
		for(int i=0;i<genres.length;i++){
			page = 1;
			flag = true;
			while(flag){
				flag = false;
				String result = UrlService.readURL("http://www.torrentbit.net/cat/Music/subcat/"+ genres[i] +"/?page=" + page);
				Pattern p = Pattern.compile("<td class=\"title\"><a href=\"/torrent/(\\d*).*?\" title=\".*?\">(.*?)</a></td>");
				Matcher m = p.matcher(result);
				while(m.find()){
					if(isInterrupted())return;
					handleTorrent(m.group(2), "http://www.torrentbit.net/get/" + m.group(1));
					flag = true;
					//break;
				}
				//flag = false;
	
				System.out.println("\n\n----" + page + "-------\n");
				page++;
			}
		}
		flushCache();
	}
	
	public static void main(String[] args) throws IOException {
		ResourceBundle prop = ResourceBundle.getBundle("spider");
		TorrentBitScanner s = new TorrentBitScanner(
				prop.getString("hostName"), Integer.parseInt(prop.getString("port")));
		s.scan();
	}
}
