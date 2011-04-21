package spider;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import services.UrlService;

/**
 * Scanner (spider) for thepiratebay.org tracker.
 * @author ivaylo
 *
 */
public class ThePirateBayScanner extends AbstractTrackerScanner{
	public ThePirateBayScanner() {
		super();
	}
	
	public ThePirateBayScanner(String host, int port, String certificateFile, String certificatePassword){
		super(host, port, certificateFile, certificatePassword);
	}
	
	/**
	 * Starts to go through the pages in the tracker and to add torrents from each of them.
	 * @throws IOException
	 */
	public void scan() throws IOException{
		boolean flag;
		int page;
		int orderBy;
		int genres[] = new int[]{100, 101, 102, 103, 104, 200, 300, 400, 600};
		System.out.println("ThePirateBayScanner starts to work");
		for(int i=0;i<genres.length;i++){
			for(orderBy = 1; orderBy < 15; orderBy++){
				if(orderBy == 10)continue;
				page = 0;
				flag = true;
				while(flag){
					flag = false;
					String result = UrlService.readURL("http://thepiratebay.org/browse/"+genres[i] + "/" + page + "/" + orderBy);
					Pattern p = Pattern.compile("title=\"Details for (.*?)\".*?</a></div>\\s*<a href=\"(.*?)\" title=\"Download this torrent\">");
					Matcher m = p.matcher(result);
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
					
					System.out.println("\nThe piratebay scanner:\n----" + page + "-------\n");
					page++;
				}
				System.out.println("\n\n\nfinished " + genres[i] + " " + orderBy + "\n\n");
			}
		}
		flushCache();
		xml.close();
	}
	
	public static void main(String[] args) throws IOException {
		ThePirateBayScanner s = new ThePirateBayScanner();
		s.scan();
	}
}

