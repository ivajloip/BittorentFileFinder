package server;

public class ClientQuerry extends BaseQuerry {
	private ClientRequest request;
	
	public ClientQuerry(ClientRequest request, Object data) {
		super(data);
		this.request = request;
	}
	
	/** Gets the request that is sent to the server.
	 * @return A ClientQuerry that is the request for the server. 
	 */
	public ClientRequest getRequest(){
		return request;
	}
}
