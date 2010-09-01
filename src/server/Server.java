package server;

import java.net.*;
import java.util.*;

import services.DbService;
import services.XmlService;

public class Server {
	private static DbService db;
	private static ServerSocket s = null;
	private static String torrentScannerHost;
	
	/** Handles the requests of one client. It handles one request at a time. 
	 * 
	 * @author ivaylo
	 *
	 */
	static class ClientThread extends Thread{
		DbService db;
		XmlService xml;
		
		public ClientThread(XmlService xml, DbService db){
			this.xml = xml;
			this.db = db;
		}
		
		public void run(){
			try{
				while(true){
					String name = (String) xml.readObject();
					if(xml.isClosed())break;
					System.out.println("Client sent search request");
					if(name.length() < 2)continue;
					List<Pair<String>> list = db.getFileList(name);
					xml.writeObject(list);
					list = null;
					name = null;
				}
			}
			catch (Exception e) {
				//e.printStackTrace();
				System.out.println("Closing connection to client");
			}
			finally{
				xml.serverClose();
			}
		}
	}
	
	/** Waits for one spider (Scanner) to give information about some torrents.
	 * 
	 * @author ivaylo
	 *
	 */
	static class ServerThread extends Thread{
		DbService db;
		XmlService xml;
		
		public ServerThread(XmlService xml, DbService db){
			this.xml = xml;
			this.db = db;
		}
		
		public void run(){
			try{
				while(true){
					Querry q = (Querry) xml.readObject();
					if(xml.isClosed())break;
					switch (q.getRequest()) {
					case ADDTORRENTS:
						addTorrentToDB((TorrentAddQuerry)q.getData());
						break;
					case CHECKTORRENTS:
						List<String> list = needsTorrents((String[])q.getData());
						xml.writeObject(list);
						list = null;
						break;
					}
					q = null;
				}
			} catch (Exception e) {
				System.out.println("Connection closed to scanner");
				return;
			}
			finally{
				xml.serverClose();
			}
		}
		
		private List<String> needsTorrents(String[] torrents){
			List<String> results = new ArrayList<String>();
			for(String s : torrents){
				if(db.getID(s) < 0)results.add(s);
			}
			return results;
		}
		
		private void addTorrentToDB(TorrentAddQuerry q){
			long id = db.getID(q.link);

			// check if the torrents is already indexed		
			if(id >= 0)return;
			
			// insert the link in the database and get it's id
			db.insertTorrentDB(q.link);
			id = db.getID(q.link);		
					
			// there is a problem with the insertion of this value
			if(id < 0){
				System.out.println("\n\n\nProblem arised while trying to insert " + q.link);
				return;
			}
			
			// add the files that correspond to the torrent 
			for(String file:q.files)
				db.insertFileDB(file, id);
		}
	}
	
	/** Connects to database according to the given parameters
	 * 
	 * @param host The host name where the database is
	 * @param port The port at which the database listens
	 * @param databaseName The name of the database
	 * @param username The username of the user
	 * @param databasePassword The password for that user
	 * @return True if we connected successfully to the database 
	 */
	public static boolean connectToDB(String host, String port, String databaseName, String username, String databasePassword){
		db = null;
		try {
			db = new DbService(host, port, databaseName, username, databasePassword);
		} catch (Exception ex) {
			System.out.println("Problem while connecting to " + host + " " + databaseName);
			return false;
		}
		return (db != null);
	}
	
	/** Starts to listening for incoming connections on the specified port. 
	 * @param port The port to which the server should bind
	 * @return True iff the successfully binded to the port
	 */
	public static boolean startListen(String port){
		try{
			s = new ServerSocket(Integer.parseInt(port));
		} catch (Exception e1) {
			System.out.println("Problem while binding to " + port);
			return false;
		}
		return true;
	}
	
	/** Reads the configuration from the conf.properties file and connects to the database and starts listeining
	 * on the specified port.
	 * If some of the operations fail, it will exit the program.
	 */
	public static void init(){
		ResourceBundle prop = ResourceBundle.getBundle("conf");
		if(!connectToDB(prop.getString("DatabaseHost"), prop.getString("DatabasePort"),
				prop.getString("DatabaseName"), prop.getString("DatabaseUsername"), 
				prop.getString("DatabasePassword")) || !startListen(prop.getString("ListenOnPort"))){
			
			System.out.println("Exitting");
			System.exit(1);
		}
		torrentScannerHost = prop.getString("TorrentScannerHost");
	}
	
	/** Starts to wait for connections and to handle them
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		
		while(true){
			try {
				Socket client = s.accept();
				XmlService xml = XmlService.createXMLHandler(client);
				
				int tmp = (Integer)xml.readObject();
				
				if(tmp==0){
					System.out.println("Client connected");
					ClientThread th = new ClientThread(xml, db);
					th.start();
				}
				else if(client.getInetAddress().getHostAddress().equals(torrentScannerHost)){
					System.out.println("Scanner connected");
					ServerThread scanner = new ServerThread(xml, db);
					scanner.start();
				}
				else {
					System.out.println("rejecting connection to: " + client.getInetAddress().getHostName());
				}
			} catch (Exception e) {
				System.out.println("Error" + e);
			}
		}
	}
}
