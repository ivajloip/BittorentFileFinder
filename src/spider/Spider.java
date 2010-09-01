package spider;

import java.util.ResourceBundle;

/**
 * Takes care that the spiders won't die and stay this way for a long time. 
 * @author ivaylo
 *
 */
public class Spider extends Thread{
	private AbstractTrackerScanner[] threads;
	private String host;
	private int port;
	
	public Spider(String host, int port){
		this.host = host;
		this.port = port;
	}
	
	/**
	 * Unleashed the spiders to index the trackers.
	 */
	@Override
	public void run(){
		
//		String host = "localhost";
//		int port = 6543; 
		
		while(true){
			if(interrupted()){
				return;
			}
			
			threads = new AbstractTrackerScanner[]{
					//new ThePirateBayScanner(host, port),
					new BtJunkieScanner(host, port),
					//new TorrentBitScanner(host, port)
					};
			
			for(Thread t : threads){
				t.start();
			}
			System.out.println("Threads started successfully");
			try {
				Thread.sleep(21600000);
				//Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			halt();			
		}
	}
	
	/**
	 * Makes all the threads to stop.
	 */
	@SuppressWarnings("deprecation")
	public void halt(){
		for(AbstractTrackerScanner t : threads){
			if(t.isAlive()){
				synchronized (t) {
						t.stop();
				}
			}				
		}
	}
	
	public static void main(String[] args){
		ResourceBundle prop = ResourceBundle.getBundle("spider");
		Thread curr = new Spider(prop.getString("hostName"), Integer.parseInt(prop.getString("port")));
		curr.run();
	}
}

