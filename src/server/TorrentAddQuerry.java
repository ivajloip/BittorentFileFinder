package server;

import java.util.*;

/**
 * Gives the server some data that should be added to the database.
 * Immutable by design.
 * @author ivaylo
 *
 */
public final class TorrentAddQuerry {
	final String link;
	final List<String> files;

	/**
	 * Creates a new TorrentAddQuerry object that will give information to the server what is to be added to the
	 * database.
	 * @param link The location of the torrent.
	 * @param files A List of String representing the names of the files in the torrent. Can not be null.
	 */
	public TorrentAddQuerry(String link, List<String> files) {
		this.link = link;
		this.files = files;
	}

	/**
	 * Retrieves the location of the torrent.
	 * @return String representing the location of the torrent in the internet.
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Retrieves the names of the files.
	 * @return A List of Strings that are the names of the files in the torrent.
	 */
	public List<String> getFiles() {
		return files;
	}
}
