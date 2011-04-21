package server;

public class BaseQuerry {
	private final Object data;
	
	/**
	 * Constructs new Querry with the provided request and data.
	 * @param r The request to be send to the server.
	 * @param data The information that should be provided.
	 */
	public BaseQuerry(Object data) {
		this.data = data;
	}

	/**
	 * Gets the data that is sent to the server.
	 * @return The containing data.
	 */
	public Object getData(){
		return data;
	}
}
