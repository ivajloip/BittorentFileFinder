package services;

import java.util.Iterator;
import java.util.List;
import org.bson.types.ObjectId;

import server.Pair;

public interface DBService {
	
	/**
	 * Sends insert request to the database, torrent table.
	 * @param link The location of the torrent that is to be added.
	 * @return if the insertion was successful returns true.  
	 */
	public boolean insertTorrent2DB(String link);
	
	/**
	 * Inserts a file in the database. 
	 * @param name The name of the file that is to be added in the database.
	 * @param id The id of the torrent that contains the file.
	 * @return True if the file name was inserted successfully.
	 */
	public boolean insertFileDB(String name, ObjectId id);
	
	/**
	 * Gets the id of the torrent.
	 * @param link The location of the torrent.
	 * @return The id of the torrent if it is present in the database. Otherwise "" is returned.
	 */
	public ObjectId getID(String link);
	
	/**
	 * Retrieves a list of filenames and locations of all entries in the database that match the words in name.
	 * @param name The String that filenames are match against.
	 * @return A List of Pairs of files and locations that match the searched the words in name.
	 */
	public List<Pair<String>> getFileList(String name);
	
	/**
	 * Tries to remove some torrents. Doesn't worry if this is impossible. Should succeed if there are no files 
	 * connected to the torrent. It will also remove all files associated with that id.
	 * @param id The id of the torrent that is to be removed from the database.
	 */
	public boolean try2RemoveFromTorrents(ObjectId id);
	
	/**
	 * Retrieves the filename and link by string representation of object id
	 * @param object id
	 * @return a single value in an ArrayList or empty ArrayList if there's no such item.
	 */
	public List<Pair<String>> getFileAndLinkById(String _object_id);
	
	/**
	 * Gets the ObjectId String representation of filename with name name
	 * @param name
	 * @return ObjectId String representation
	 */
	
	public String getFileId(String name);
	
	/**
	 * Gets an Iterator<String> with links
	 */
	public Iterator<String> getLinks();
}