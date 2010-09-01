package client;

import jBittorrentAPI.TorrentDownloader;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;

import server.Pair;
import services.UrlService;
import services.XmlService;


public class DumbClient {
	public static void main(String[] args) throws UnknownHostException, IOException {		
		ResourceBundle prop = ResourceBundle.getBundle("client");
		
		XmlService xml = XmlService.createXMLHandler(
				prop.getString("server_host"), Integer.parseInt(prop.getString("server_port")));
		
		xml.writeObject(new Integer(0));
		Scanner sc = new Scanner(System.in);
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		
		System.out.println("\nPlease enter filename to search for: ");
		while(sc.hasNext()){
			String request = sc.nextLine();
			if(request.length() < 2){
				continue;
			}
			xml.writeObject(request);
			
			System.out.println("We found: ");
			
			@SuppressWarnings("unchecked")
			List<Pair<String>> l = (List<Pair<String>>) xml.readObject();
			int i=0;
			
			@SuppressWarnings("unchecked")
			Pair<String>[] results = new Pair[l.size()];
			String prevTorrentName = "";
			boolean prevTorrent = true;
			for(Pair<String> t:l){
				if(t.getSecond().matches(".*thepiratebay\\.org/.*") ){
					ThePirateBayTorrent tmp = new ThePirateBayTorrent();
					if(prevTorrentName == t.getSecond()){
						if(prevTorrent){
							results[i] = new Pair<String>(t.getFirst(), t.getSecond());
							System.out.println(i + ") " + t.getFirst() + " " + t.getSecond());
							i++;
						}
					}
					else if(tmp.getSeeders(t.getSecond()) > 0){
						prevTorrent = true;
						results[i] = new Pair<String>(t.getFirst(), t.getSecond());
						System.out.println(i + ") " + t.getFirst() + " " + t.getSecond());
						i++;						
					}
					else{
						prevTorrent = false;
					}
					prevTorrentName = t.getSecond();
				}
			}
			System.out.println("Please enter the nubmer of the torrent that you want: ");
			int num=sc.nextInt();
			if(num >= i){
				System.out.println("Incorrect input\nPlease enter filename to search for: ");
				continue;
			}
			String torrent = results[num].getSecond();
			String file = results[num].getFirst();
			results = null;
			l = null;
			if(!UrlService.copyFile(torrent, tmpDir + "/__tmp123" + ".torrent"))continue;;
			System.out.println("name " + file + " torrent" + torrent);
			TorrentDownloader f = new TorrentDownloader(tmpDir + "/__tmp123" + ".torrent", tmpDir+"/", file);
			f.start();
			
			System.out.println("\nPlease enter filename to search for: ");
		}
		xml.close();
//		s.close();
	}
}
