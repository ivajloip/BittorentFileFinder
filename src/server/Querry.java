package server;

/** Class Querry is disigned to be used for comunication between the server and the scanners. 
 * It has data and type of request.
 * It is immutable by design. 
 * @author ivaylo
 */
public final class Querry {
	private final ServerRequest r;
	private final Object data;
	
	/**
	 * Constructs new Querry with the provided request and data.
	 * @param r The request to be send to the server.
	 * @param data The information that should be provided.
	 */
	public Querry(ServerRequest r, Object data){
		this.r = r;
		this.data = data;
	}

	/** Gets the request that is sent to the server.
	 * @return A ServerRequest that is the reguest for the server. 
	 */
	public ServerRequest getRequest(){
		return r;
	}

	/**
	 * Gets the data that is sent to the server.
	 * @return The containing data.
	 */
	public Object getData(){
		return data;
	}
}
