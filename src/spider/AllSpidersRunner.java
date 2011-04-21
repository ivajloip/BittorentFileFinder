package spider;


/**
 * Takes care that the spiders won't die and stay this way for a long time. 
 * @author ivaylo
 *
 */
public class AllSpidersRunner extends Thread{
	private AbstractTrackerScanner[] threads;
	private String host;
	private int port;
	private String cerfiticateFile;
	private String certificatePassword;
	
	public AllSpidersRunner(String host, int port, String certificateFile, String certificatePassword){
		this.host = host;
		this.port = port;
		this.cerfiticateFile = certificateFile;
		this.certificatePassword = certificatePassword;
	}
	
	/**
	 * Unleashed the spiders to index the trackers.
	 */
	@Override
	public void run(){
		while(true){
			if(interrupted()){
				return;
			}
			
			threads = new AbstractTrackerScanner[]{
					new ThePirateBayScanner(host, port, cerfiticateFile, certificatePassword), 
//					new BtJunkieScanner(host, port, cerfiticateFile, certificatePassword),
//					new TorrentBitScanner(host, port, cerfiticateFile, certificatePassword)
			};
			
			for(Thread t : threads){
				t.start();
			}
			System.out.println("Threads started successfully");
			try {
				Thread.sleep(21600000);
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
		Object[] conf = AbstractTrackerScanner.getConfiguration();
		Thread curr = new AllSpidersRunner((String) conf[0], (Integer) conf[1], (String) conf[2], (String) conf[3]);
		curr.run();
	}
}

