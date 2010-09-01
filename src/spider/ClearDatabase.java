package spider;

import java.util.List;
import java.util.ResourceBundle;

import services.DbService;

/**
 * Class used when we had some inconsistency in the database - should be removed in the future.
 * @author ivaylo
 *
 */
public class ClearDatabase {
	public static DbService db;
	public static void main(String[] args) {
		ResourceBundle prop = ResourceBundle.getBundle("conf");
		try {
			db = new DbService(prop.getString("DatabaseHost"), prop.getString("DatabasePort"),
					prop.getString("DatabaseName"), prop.getString("DatabaseUsername"), 
					prop.getString("DatabasePassword"));
			List<Integer> t = db.getTorrentsThatMatch("torrentbit.net");
			System.out.println(t.size());
			int counter = 0;
			for (int a : t){
				db.tryRemoveFromTorrents(a);
				counter++;
				if(counter == 20){
					counter = 0;
					db.executeBatch();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
