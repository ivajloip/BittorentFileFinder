/*
 * Java Bittorrent API as its name indicates is a JAVA API that implements the Bittorrent Protocol
 * This project contains two packages:
 * 1. jBittorrentAPI is the "client" part, i.e. it implements all classes needed to publish
 *    files, share them and download them.
 *    This package also contains example classes on how a developer could create new applications.
 * 2. trackerBT is the "tracker" part, i.e. it implements a all classes needed to run
 *    a Bittorrent tracker that coordinates peers exchanges. *
 *
 * Copyright (C) 2007 Baptiste Dubuis, Artificial Intelligence Laboratory, EPFL
 *
 * This file is part of jbittorrentapi-v1.0.zip
 *
 * Java Bittorrent API is free software and a free user study set-up;
 * you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Java Bittorrent API is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Java Bittorrent API; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * @version 1.0
 * @author Baptiste Dubuis
 * To contact the author:
 * email: baptiste.dubuis@gmail.com
 *
 * More information about Java Bittorrent API:
 *    http://sourceforge.net/projects/bitext/
 */

package jBittorrentAPI;

import java.util.ArrayList;

/**
 * Downloads requested files from a torrent.
 */
public class TorrentDownloader extends Thread{
	String pathToTorrent;
	String savePath;
	String[] files;
	DownloadManager dm;
	
	/**
	 * 
	 * @param pathToTorrent The local absolute path to the torrent file.
	 * @param savePath The directory where the torrent should be downloaded.
	 * @param files The files from the torrent that should be downloaded.
	 */
    public TorrentDownloader(String pathToTorrent, String savePath, String ... files){
    	this.pathToTorrent = pathToTorrent;
    	this.savePath = savePath;
    	this.files = files;
    }
    
    /**
     * Interrupts the download. If the data isn't removed the process can be continued later with another thread.
     */
    @Override
    public void interrupt(){
    	if(dm == null) return;
    	super.interrupt();
    	dm.interrupt();
        //dm.stopTrackerUpdate();
        //dm.closeTempFiles();
    }
    
    /**
     * Returns how much of the job is already done. The result should be a number between 0 and 100.
     * @return In percents how much of the torrents is downloaded.
     */
    public double getCompleted(){
    	if(dm == null) return 0;
    	return dm.getPercentsCompleted();
    }
    
    /**
     * Gets the download rate for the last few seconds in kb/s.
     * @return The rate at which the torrents is downloaded.
     */
    public double getDownloadRate(){
    	if(dm == null) return 0;
    	return dm.getDownloadRate();
    }
    
    /**
     * Starts to download the torrent.
     */
    @Override
    public void run(){
    	System.out.println("Starting to download");
    	try {
            TorrentProcessor tp = new TorrentProcessor();

            if(pathToTorrent == null){
                System.err.println(
                        "Incorrect use, please provide the path of the torrent file...\r\n" +
                        "\r\nCorrect use of ExampleDownloadFiles:\r\n"+
                        "ExampleDownloadFiles torrentPath");

                return;
            }
            TorrentFile t = tp.getTorrentFile(tp.parseTorrent(pathToTorrent));
            if(savePath != null)
                Constants.SAVEPATH = savePath;
            if (t != null) {
            	ArrayList<String> lightList = new ArrayList<String>();
            	for(String file : files)
            		lightList.add(file);
                dm = new DownloadManager(t, Utils.generateID(), (files.length > 0)?lightList:null);
                dm.startListening(6881, 6889);
                dm.startTrackerUpdate();
                dm.blockUntilCompletion();
                dm.stopTrackerUpdate();
                dm.closeTempFiles();
            } else {
                System.err.println(
                        "Provided file is not a valid torrent file");
                System.err.flush();
               	return;
            }
        } catch (Exception e) {
            System.out.println("Error while processing torrent file. Please restart the client");
            return;
        }
    }
}
