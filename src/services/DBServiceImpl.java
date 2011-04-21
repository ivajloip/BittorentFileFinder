package services;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;

import server.Pair;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * A class that handles all the request to the database.
 * @author marii
 *
 */
public class DBServiceImpl implements DBService {
	
	private DBCollection torrents;
	private DBCollection files;
	
	/**
	 * If able to create new connection to the database it gives a new DBHandler. Otherwise throws Exception.
	 * @throws Exception if we are unable to connect to the database and the production of the instance is pointless.
	 */
	public DBServiceImpl() throws Exception{
		Mongo m = new Mongo();
		DB db = m.getDB("torrentsdb");
		torrents = db.getCollection("torrents");
		files = db.getCollection("files");
	}
	
	public synchronized boolean insertTorrent2DB(String link){
		try {
			BasicDBObject obj = new BasicDBObject("link", link);
			torrents.insert(obj);
		} catch(Exception e) {
			return false;
		}
		return true;
	}
	
	public synchronized boolean insertFileDB(String name, ObjectId id){
		try {
			BasicDBObject obj = new BasicDBObject();
			obj.put("name", name);
			obj.put("torrent_id", id);
			files.insert(obj);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	public ObjectId getID(String link) {		
		DBObject t = torrents.findOne( new BasicDBObject("link", link) );
		if ( t == null ) return null;
		return (ObjectId) t.get("_id");
	}
	
	public List<Pair<String>> getFileList(String name) {
		List<Pair<String>> result = new ArrayList<Pair<String>>();
		DBCursor c = files.find(query(name)).limit(1000);
		
		for (DBObject d : c) {
			DBObject o = torrents.findOne(new BasicDBObject("_id", d.get("torrent_id")));			
			result.add( new Pair<String>((String) d.get("name"), (String) o.get("link")) );			
		}
		return result;
	}

	/**
	 * 
	 * @param q a String query which will be searched
	 * @return BasicDBObject which is the actual query
	 */
	public static BasicDBObject query(String q) {
		ArrayList<BasicDBObject> arr = new ArrayList<BasicDBObject>();
		String[] data = q.split("\\s+");
		for (String d : data)
			arr.add( new BasicDBObject("name", Pattern.compile( ".*" + d + ".*",
						Pattern.DOTALL | Pattern.CASE_INSENSITIVE)));
		return new BasicDBObject("$or", arr);
	}

	public boolean try2RemoveFromTorrents(ObjectId id) {
		try {
			synchronized(this) {
				files.remove( new BasicDBObject("torrent_id", id) );
				torrents.remove( new BasicDBObject("_id", id) );
			}			
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public List<Pair<String>> getFileAndLinkById(String _object_id) {
		List<Pair<String>> arr = new ArrayList<Pair<String>>();
		try {
			DBObject o = files.findOne( new BasicDBObject( "_id", new ObjectId(_object_id)) );
			if ( o != null ) {
				DBObject t = torrents.findOne(new BasicDBObject("_id", o.get("torrent_id")) );	
				arr.add( new Pair<String> ((String) o.get("name"), (String) t.get("link")) );
			}
		} catch(Exception e) {
			return new ArrayList<Pair<String>> ();
		}
		
		return arr;
	}

	public String getFileId(String name) {
		DBObject o = files.findOne( new BasicDBObject("name", name) );
		return ((ObjectId) o.get("_id")).toString();
	}

	public Iterator<String> getLinks () {
		return new DummyIterator();
	}
	
	private class DummyIterator implements Iterator<String> {
		private DBCursor cursor;
		public DummyIterator() {
			cursor = torrents.find();
		}

		public boolean hasNext() {
			return cursor.hasNext();
		}

		public String next() {
			return (String) cursor.next().get("link");
		}

		public void remove() {}		
	}
		
}
