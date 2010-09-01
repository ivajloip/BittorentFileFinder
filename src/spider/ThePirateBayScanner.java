package spider;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.regex.*;

import services.UrlService;

/**
 * Scanner (spider) for thepiratebay.org tracker.
 * @author ivaylo
 *
 */
public class ThePirateBayScanner extends AbstractTrackerScanner{
	public ThePirateBayScanner(String host, int port){
		init(host, port);
	}
	
	/**
	 * Starts to go through the pages in the tracker and to add torrents from each of them.
	 * @throws IOException
	 */
	public void scan() throws IOException{
		boolean flag;
		int page;
		int orderBy;
		int genres[] = new int[]{600, 400, 100, 101, 102, 103, 104, 200, 300};
		System.out.println("ThePirateBayScanner starts to work");
		for(int i=0;i<genres.length;i++){
			for(orderBy = 1; orderBy < 15; orderBy++){
				if(orderBy == 10)continue;
				page = 0;
				flag = true;
				while(flag){
					flag = false;
					//System.out.println("next");
					// za janrovete 100 moje da se zamesti s 200, 300, 400, 600
					String result = UrlService.readURL("http://thepiratebay.org/browse/"+genres[i] + "/" + page + "/" + orderBy);
					//System.out.println("read");
					Pattern p = Pattern.compile("title=\"Details for (.*?)\".*?</a></div>\\s*<a href=\"(.*?)\" title=\"Download this torrent\">");
					Matcher m = p.matcher(result);
					//System.out.println("staring");
					while(m.find()){
						//System.out.println(m.group(2));
						if(isInterrupted()){
							System.out.println("closing");
							xml.close(); 
							return;
						}
						handleTorrent(m.group(1), m.group(2));
						flag = true;
					}
					
					System.out.println("\n----" + page + "-------");
					page++;
				}
				System.out.println("\n\n\nfinished " + genres[i] + " " + orderBy + "\n\n");
			}
		}
		flushCache();
		xml.close();
	}
	
	public static void main(String[] args) throws IOException {
		ResourceBundle prop = ResourceBundle.getBundle("spider");
		
		ThePirateBayScanner s = new ThePirateBayScanner(
				prop.getString("hostName"), Integer.parseInt(prop.getString("port")));
		s.scan();
	}
}

