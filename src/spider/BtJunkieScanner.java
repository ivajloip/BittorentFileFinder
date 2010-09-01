package spider;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;

/**
 * Scanner (spider) for btjunk.org tracker.
 * @author ivaylo
 *
 */
public class BtJunkieScanner extends AbstractTrackerScanner{
	public BtJunkieScanner(String host, int port){
		init(host, port);
	}
	
	/**
	 * Starts to go through the pages in the tracker and to add torrents from each of them.
	 * @throws IOException
	 */
	@Override
	public void scan() throws IOException {
		boolean flag;
		int page;
		
		String genres[] = new String[]{"Audio", "Anime", "Games"};
		System.out.println("BtJunk starts to work");
		for(int i=0;i<genres.length;i++){
				page = 1;
				flag = true;
				while(flag){
					flag = false;
					
					// waits some time so that the server won't decide that it's DOS attack
					try {
						Thread.sleep(120000);
					} catch (InterruptedException e) {
						System.out.println("Interrupted while waiting for next");
					}
					
					String result = UrlService.readURL("http://btjunkie.org/browse/"+genres[i] + "/page" + page + "/?o=52&t=0&s=1");
					Pattern p = Pattern.compile(" class=\"label\"><a href=\"(.*?)\"(.*?\\s*){0,4}.*?class=\"BlckUnd\">(.*?)</a></th>");
					Matcher m = p.matcher(result);
					
					while(m.find()){
						if(isInterrupted()){
							System.out.println("closing");
							xml.close(); 
							return;
						}
						handleTorrent(m.group(3), m.group(1));
						flag = true;
					}
					
					System.out.println("\n----" + page + "-------");
					page++;
				}
				System.out.println("\n\n\nfinished " + genres[i] + "\n\n");
		}
		flushCache();
		xml.close();
	}
}
