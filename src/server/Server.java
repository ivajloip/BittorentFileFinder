package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;

import services.DBService;
import services.DBServiceImpl;
import services.SigningService;
import services.XmlService;
import utility.Utilities;

public class Server {
	private static DBService db;
	private static ServerSocket s = null;
	private static String certificateFile;
	private static String certificatePassword;
	private static String[] blacklist;
	private static Logger logger;
	
	/** Handles the requests of one client. It handles one request at a time. 
	 * 
	 * @author ivaylo
	 *
	 */
	static class ClientThread extends Thread {
		private DBService db;
		private XmlService xml;
		private Logger log;
		
		public ClientThread(XmlService xml, DBService db2) {
			this.xml = xml;
			this.db = db2;
			log = Logger.getLogger(getClass());
		}
		
		public void run() {
			try{
				while(true) {
					ServerQuerry q = (ServerQuerry) xml.readObject();
					if(xml.isClosed()) {
						break;
					}
					String name = (String) q.getData();
					switch(q.getRequest()) {
						case SEARCH:
							if(name.length() < 2) {
								continue;
							}
							log.debug("Client sent search request " + name);
							
							List<Pair<String>> list;
							list = db.getFileList(name);
							xml.writeObject(list);
							list = null;
							name = null;
							
							break;
						case GETID:
							log.debug("-- id searched for" + name);
							String res = db.getFileId(name);
							log.debug(res);
							xml.writeObject(res);
							break;
						case SEARCHBYID:
							list = db.getFileAndLinkById(name);
							if(list != null && list.size() != 0) { 
								log.debug( list.get(0).getFirst() + " " + list.get(0).getSecond() );
							}
							xml.writeObject(list);
							break;
						default:
							log.error("Unallowed operation send. Ignoring " + q.getRequest());
					}						
				}
			}
			catch (Exception e) {
				log.error("Closing connection to client", e);
			}
			finally {
				xml.serverClose();
			}
		}
	}
	
	/** Waits for one spider (Scanner) to give information about some torrents.
	 * 
	 * @author ivaylo
	 *
	 */
	static class ServerThread extends Thread {
		private DBService db;
		private XmlService xml;
		private Logger log;
		
		public ServerThread(XmlService xml, DBService db2) {
			this.xml = xml;
			this.db = db2;
			log = Logger.getLogger(getClass());
		}
		
		public void run() {
			try {
				// Check that the host is really a valid spider
				String text = "" + new Random().nextDouble();
				xml.writeObject(text);
				String signed = (String)xml.readObject();
				SigningService xmlService = SigningService.createSigningService(certificateFile, certificatePassword);
				if(!xmlService.validate(signed)) {
					log.error("Incorrect signature");
					return;
				}
				
				while(true) {
					ServerQuerry q = (ServerQuerry) xml.readObject();
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
				log.info("Connection closed to scanner");
				return;
			}
			finally {
				xml.serverClose();
			}
		}
		
		private List<String> needsTorrents(String[] torrents) {
			List<String> results = new ArrayList<String>();
			for(String s : torrents) {
				if(db.getID(s) == null)results.add(s);
			}
			return results;
		}
		
		private void addTorrentToDB(TorrentAddQuerry q) {
			ObjectId id = db.getID(q.link);

			// check if the torrents is already indexed		
			if( id != null ) {
				return;
			}
			
			// insert the link in the database and get it's id
			db.insertTorrent2DB(q.link);
			id = db.getID(q.link);		
					
			// there is a problem with the insertion of this value
			if( id == null ) {
				log.error("\n\n\nProblem arised while trying to insert " + q.link);
				return;
			}
			
			// add the files that correspond to the torrent 
			for(String file:q.files) {
				db.insertFileDB(file, id);
			}
		}
	}
	
	/**
	 * Checks if some torrents were deleted
	 * @author ivaylo
	 *
	 */
	static class TorrentChecker extends Thread {
		DBService db;
		Logger log;
		
		public TorrentChecker(DBService db2) {
			this.db = db2;
			log = Logger.getLogger(getClass());
		}
		
		@Override
		public void run() {
			while(true) {
				Iterator<String> iter = db.getLinks();
				if(isInterrupted()) {
					log.debug("Interrupted");
					return;
				}
				
				try {
					while(iter.hasNext()) {
						String toCheck = iter.next();
						if(!checkAvailable(toCheck)) {
							ObjectId id = db.getID(toCheck);
							db.try2RemoveFromTorrents(id);
							logger.debug("Removing: " + toCheck);
						}
					}
					
					log.info("Sleeping");
					Thread.sleep(86400000);
				} catch (InterruptedException e) {
					log.error("Interrupted");
					return;
				} catch (Exception e) {
					iter = db.getLinks();
				}
			}
		}

		/**
		 * Returns whether the torrent file is in the tracker, or it isn't
		 * @param toCheck 
		 * @return whether the torrent file is in the tracker, or it isn't
		 */
		private boolean checkAvailable(String toCheck) {
			return Utilities.getSeed(toCheck) >= 0;
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
	public static boolean connectToDB(String host, String port, String databaseName, String username, String databasePassword) {
		db = null;
		try {
			db = new DBServiceImpl();
		} catch (Exception ex) {
			logger.error("Problem while connecting to " + host + " " + databaseName, ex);
			return false;
		}
		return (db != null);
	}
	
	/** Starts to listening for incoming connections on the specified port. 
	 * @param port The port to which the server should bind
	 * @return True iff the successfully binded to the port
	 */
	public static boolean startListen(String port) {
		try {
			s = new ServerSocket(Integer.parseInt(port));
		} catch (Exception e1) {
			logger.error("Problem while binding to " + port, e1);
			return false;
		}
		return true;
	}
	
	/** Reads the configuration from the conf.properties file and connects to the database and starts listening
	 * on the specified port.
	 * If some of the operations fail, it will exit the program.
	 */
	public static void init() {
		logger = Logger.getLogger("Server");
		logger.info("Server started");
		ResourceBundle prop = ResourceBundle.getBundle("conf");
		
		if(!connectToDB(prop.getString("DatabaseHost"), prop.getString("DatabasePort"),
				prop.getString("DatabaseName"), prop.getString("DatabaseUsername"), 
				prop.getString("DatabasePassword")) || !startListen(prop.getString("ListenOnPort"))) {
			
			logger.error("Exitting");
			System.exit(1);
		}
		
		certificateFile = prop.getString("certificateFile");
		certificatePassword = prop.getString("certificatePassword");
		generateBlackList(prop);
		Thread torrentChecker = new TorrentChecker(db);
		torrentChecker.start();
	}
	
	private static void generateBlackList(ResourceBundle prop) {
		String blockAll = prop.getString("backlist");
		blacklist = blockAll.split(";");
		
		for(int i = 0; i < blacklist.length; i++) {
			String[] tmp = blacklist[i].split("\\.");
			int j;
			
			for(j = 0; j < 4; j++) {
				if(tmp[j].equals("0")) {
					break;
				}
			}
			
			if(j != 4) {
				StringBuilder newStr = new StringBuilder();
				int k;
				
				for(k = 0; k < j; k++) {
					newStr.append(tmp[k]);
					newStr.append(".");
				}
				for(k = j; k < 4; k ++) {
					newStr.append("\\d{1,3}");
					if(k != 3) {
						newStr.append(".");
					}
				}
				blacklist[i] = newStr.toString();
			}
		}
	}
	
	private static boolean isInBlackList(String hostAddress) {
		for(String blocked : blacklist) {
			if(hostAddress.matches(blocked)) {
				return true;
			}
		}
		return false;
	}

	/** Starts to wait for connections and to handle them
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		init();
		
		while(true) {
			try {
				Socket client = s.accept();
				XmlService xml = XmlService.createXMLHandler(client);
				
				int tmp = (Integer)xml.readObject();
				
				String hostAddress = client.getInetAddress().getHostAddress(); 
				if(isInBlackList(hostAddress)) {
					logger.error(hostAddress + " tried to connect but is in the blacklist");
					xml.serverClose();
					continue;
				}
				
				if(tmp == 0) {
					logger.info("Client connected " + hostAddress);
					ClientThread th = new ClientThread(xml, db);
					th.start();
				}
				else if(tmp == 1) {
					logger.info("Scanner connected" + client.getRemoteSocketAddress());
					ServerThread scanner = new ServerThread(xml, db);
					scanner.start();
				}
				else {
					logger.error("Rejecting connection to: " + client.getInetAddress().getHostName());
					xml.close();
				}
			} catch (Exception e) {
				logger.error("Error", e);
			}
		}
	}
}
