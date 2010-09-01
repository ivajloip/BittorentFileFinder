package services;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import server.Pair;


/**
 * A class that handles all the request to the database.
 * @author ivaylo
 *
 */
public class DbService {
	Connection       db;
	Statement        sql;
	DatabaseMetaData dbmd;
	
	/**
	 * If able to create new connection to the database it gives a new DBHandler. Otherwise throws Exception.
	 * @param host The host name where the database is.
	 * @param port The port at which the database is listening.
	 * @param dbName The name of the database.
	 * @param username The username that should be used to connect to the database.
	 * @param passwd The password for the user in the database.
	 * @throws Exception If we are unable to connect to the database and the production of the instance is pointless.
	 */
	public DbService(String host, String port, String dbName, String username, String passwd) throws Exception{
		Class.forName("org.postgresql.Driver"); //load the driver
	    db = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + dbName,
	                                     username,
	                                     passwd); //connect to the db
	    dbmd = db.getMetaData(); //get MetaData to confirm connection
	    System.out.println("Connection to "+dbmd.getDatabaseProductName()+" "+
	                       dbmd.getDatabaseProductVersion()+" successful.\n");
	    sql = db.createStatement();
	}
	
	/**
	 * Sends insert request to the database, torrent table.
	 * @param link The location of the torrent that is to be added.
	 * @return Iff the insertion was successful returns true.  
	 */
	public synchronized boolean insertTorrentDB(String link){
		String sqlText = "insert into torrents values(default, E'" + link +  "');";
	    try {
			sql.executeUpdate(sqlText);
			sql.clearWarnings();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Inserts a file in the database. 
	 * @param name The name of the file that is to be added in the database.
	 * @param id The id of the torrent that contains the file.
	 * @return True iff the file name was inserted successfully.
	 */
	public synchronized boolean insertFileDB(String name, long id){
		name = name.replace("'", "\\'");
	    String sqlText = "insert into files values(E'" + name +  "', " + id + ");";
	    try {
			sql.executeUpdate(sqlText);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the id of the torrent.
	 * @param link The location of the torrent.
	 * @return The id of the torrent if it is present in the database. Otherwise -1 is returned.
	 */
	public long getID(String link){
		long result = -1;
		try {
			if(link == null || link == "")return -1;
			ResultSet results = sql.executeQuery("select id from torrents where address = '" + link + "';");
		    if (results != null)
		    {
		    	if(results.next()) 	result = results.getLong("id");
		    }
		    results.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * Retrieves a list of filenames and locations of all entries in the database that match the words in name.
	 * @param name The String that filenames are match against.
	 * @return A List of Pairs of files and locations that match the searched the words in name.
	 */
	public List<Pair<String>> getFileList(String name){
		List<Pair<String>> result = new ArrayList<Pair<String>>();
		name = name.replace("'", "\\'");
		try {
			//ResultSet results = sql.executeQuery("select name,address from files, torrents " + formWhereClause(name) + "limit 1000;");
			//ResultSet results = sql.executeQuery("select name,address from files, torrents " + formWhereClause(name) + " and address ~* 'torrentbit.net' limit 1000;");
			ResultSet results = sql.executeQuery("select name,address from files, torrents " + formWhereClause(name) + " and address ~* 'btjunk' limit 1000;");
		    if (results != null)
		    {
		      while (results.next())
		      {
		    	  result.add(new Pair<String>(results.getString("name"), ""+results.getString("address")));
		      }
		    }
		    results.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Gets a list of ids of torrents that fulfill some criteria.
	 * @param str The String that should be contained in the link of the torrents in order to be added to the result.
	 * @return A list of ids of torrent locations in the database.
	 */
	public List<Integer> getTorrentsThatMatch(String str){
		List<Integer> result = new ArrayList<Integer>();
		str = str.replace("'", "\\'");
		try {
			ResultSet results = sql.executeQuery("select id from torrents where address ~* '" + str + "';");
		    if (results != null)
		    {
		      while (results.next())
		      {
		    	  result.add(results.getInt("id"));
		      }
		    }
		    results.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Tries to remove some torrents. Doesn't worry if this is impossible. Should succeed if there are no files 
	 * connected to the torrent.
	 * @param id The id of the torrent that is to be removed from the database.
	 */
	public void tryRemoveFromTorrents(int id){
		try{
			sql.addBatch("delete from torrents where id = '" + id + "';");
		}
		catch (Exception e) {
			System.out.println(id + " is needed");
		}
	}
	
	/**
	 * Executes a batch of commands. This should be fasted then executing single commands.
	 */
	public void executeBatch(){
		try {
			sql.executeBatch();
		} catch (SQLException e) {
			System.out.println("some needed :D");
			//e.printStackTrace();
		}
	}
	
	/**
	 * Makes the where clause for searching in the database.
	 * @param name The words that should be contained.
	 * @return A String that is the where clause.
	 */
	private String formWhereClause(String name){
		if(name == null) return "";
		String[] words = name.split(" ");
		String result = "where files.id = torrents.id and files.name ~* '" + words[0] + "' ";
		for(int i=1;i<words.length;i++)
			result += "and name ~* '" + words[i] + "'";
		
		return result;
	}
}
