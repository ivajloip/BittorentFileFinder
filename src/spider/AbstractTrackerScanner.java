package spider;

import jBittorrentAPI.*;
import java.io.*;
import java.util.*;

import server.*;
import services.UrlService;
import services.XmlService;


/**
 * Handles the work that is common for all the spiders: connection to server, finding files in torrent and sending 
 * them to the server.
 * @author ivaylo
 *
 */
public abstract class AbstractTrackerScanner extends Thread{
	protected TorrentProcessor tp;
	protected XmlService xml;
	protected String[] torrentsCache;
	protected int current;
	private String tmpDir;
	private boolean stopScan;
	
	/**
	 * Initialize the connection to the server.
	 * @param host The hostname to which the spider should connect.
	 * @param port The port where the server should be listening.
	 */
	public void init(String host, int port){
		tp = new TorrentProcessor();
		connectToServer(host, port);
		setTmpDir();
		stopScan = false;
		torrentsCache = new String[20];
		current = 0;
	}
	
	/**
	 * Adds a torrent to be inserted in the database if it isn't already there. 
	 * @param name The name of the torrent (never really accessed, but added for future use).
	 * @param torrent
	 */
	public void handleTorrent(String name, String torrent){
		torrentsCache[current++] = torrent;
		if(current == torrentsCache.length)flushCache();
	}
	
	/**
	 * Sends all the torrents in the cache to the server (if they are not already indexed, they are indexed :) ). 
	 */
	@SuppressWarnings("unchecked")
	protected synchronized void flushCache(){
		xml.writeObject(new Querry(ServerRequest.CHECKTORRENTS,torrentsCache));
		List<String> needs = (List<String>) xml.readObject();
		List<String> files;
		for(String torrent : needs){
			files = getFilesInTorrent(torrent);
			if(files != null){
				System.out.println("Adding " + files.size() + " files in torrent "+ torrent );
				xml.writeObject(new Querry(ServerRequest.ADDTORRENTS, new TorrentAddQuerry(torrent, files)));
			}
		}
		current = 0;
	}
	
	/**
	 * Gets the files at the specified location.
	 * @param link The location of the torrent which files is to be returned.
	 * @return A List of String containing the names of the files that are in the torrent. If some error 
	 * occurs, null is returned.
	 */
	public List<String> getFilesInTorrent(String link){
		List<String> files = new ArrayList<String>();
		
		if(!UrlService.copyFile(link, tmpDir + "/__tmp123" + ".torrent"))return null;
		TorrentFile t = tp.getTorrentFile(tp.parseTorrent(tmpDir + "/__tmp123" + ".torrent"));
		
		if(t == null || stopScan){
			System.out.println("Error while parsing torrent file" + link);
			if(stopScan)interrupt();
			else stopScan = true;
			return null;
		}
		
		stopScan = false;
		
		for(Object o:t.name)
			files.add(o.toString());
		
		File f = new File(tmpDir + "/__tmp123"+".torrent");
		f.delete();
		
		// clears some data in the TorrentProcessor
		t.length.clear();
		t.name.clear();
		t.piece_hash_values_as_binary.clear();
		t.piece_hash_values_as_hex.clear();
		t.piece_hash_values_as_url.clear();
		
		return files;
	}
	
	/**
	 * Connects to the server at the specified host and port. Returns true iff the connection was successful.
	 * @param host The host name of the server.
	 * @param port The port at which the server should be listening.
	 * @return Whether the connection was successful.
	 */
	protected boolean connectToServer(String host, int port){
		if(xml != null) return true;
		try{
			//Socket s = new Socket (host, port);
			xml = XmlService.createXMLHandler(host, port);
			xml.writeObject(new Integer(1));
		}
		catch (Exception e) {
			xml = null;
			return false;
		}
		return true;
	}
	
	/**
	 * Sets the system tmp dir.
	 */
	protected void setTmpDir(){
		tmpDir = System.getProperty("java.io.tmpdir");
	}
		
	/**
	 * Starts to go through the pages in the tracker and to add torrents from each of them.
	 * @throws IOException 
	 */
	public abstract void scan() throws IOException;
	
	/**
	 * Starts the scan of the tracker.
	 */
	@Override
	public void run(){
		try {
			scan();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
