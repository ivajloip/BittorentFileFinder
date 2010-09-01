package client.gui;

import jBittorrentAPI.TorrentDownloader;

public class TorrentTask {

	private TorrentDownloader thread;
	private Elem name;
	
	private String pathToTorrent;
	private String savePath, file;
	
	private double status;
//	private int end;
	private boolean paused;
	
	protected double getStatus () {
		return status; 
	}
	
	protected void updateStatus () {
		status = thread.getCompleted();
	}
	
	protected boolean isFinished () {
		return thread.getCompleted() == 100.0;
	} 
	
	public TorrentTask(String pathToTorrent, String savePath, String file, Elem e) {
		this.pathToTorrent = pathToTorrent;
		this.savePath = savePath;
		this.file = file;
		status = 0.0;
		paused = false;
		name = e;
		thread = new TorrentDownloader(pathToTorrent, savePath, file);
		thread.start();
	}
		
	@SuppressWarnings("deprecation")
	public void pause () {
		System.out.println("supposed to stop!");
		paused = true;
		//thread.suspend();
		thread.interrupt();
		thread.stop();
		System.out.println(thread.getState());
	}
	
	public void resume () {
		System.out.println("supposed to resume!");
		paused = false;
		thread = new TorrentDownloader(pathToTorrent, savePath, file);
		thread.start();
	}
	
	public String toString () {
		updateStatus();		
		return "" + name + " completed: " + status;
	}
	
	public boolean isPaused () { return paused; }
	
}
