package services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Handles the communication between server, clients and spiders. 
 * @author ivaylo
 *
 */
public class XmlService {
	private XStream read;
	private XStream write;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isClosed;
	
	/**
	 * Constructs XMLHandler
	 * @param in The stream from which the data should be read
	 * @param out The stream where the data should be send
	 */
	private XmlService(ObjectInputStream in, ObjectOutputStream out){
		read = new XStream(new DomDriver());
		write = new XStream();
		this.in = in;
		this.out = out;
		isClosed = false;
	}
	
	/**
	 * Sends obj to the other party in the communication. The other application should know what kind of Object is
	 * expected.
	 * @param obj The Object that should be written
	 */
	public synchronized void writeObject(Object obj){
		try{
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			write.toXML(obj, output);
			out.writeInt(output.size());
			out.write(output.toByteArray(), 0, output.size());
			out.flush();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/**
	 * Reads an Object from the other party in the communication. Blocks until data is present.
	 * @return Object that the other party has sent.
	 */
	public synchronized Object readObject(){
		Object result = null;
		try{
			int len = in.readInt();
			if(len == -1){
				isClosed = true;
				return null;
			}
			else if (len == 0){
				return null;
			}
			DataInputStream dataInput = new DataInputStream(in);
			byte buffer[] = new byte[len];
			dataInput.readFully(buffer, 0, len);
			ByteArrayInputStream input = new ByteArrayInputStream(buffer);
			result = read.fromXML(input);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return result; 
	}
	
	/**
	 * Checks if the connunication can still be used.
	 * @return whether the communication was closed.
	 */
	public boolean isClosed(){
		return isClosed;
	}
	
	/**
	 * Initialized a close of the connection between the two applications.
	 */
	public synchronized void close(){
		try {
			out.writeInt(-1);
			out.flush();
		} catch (IOException e) {
				serverClose();
			System.out.println("Problem arised while trying to closing connection");
		}
	}
	
	/**
	 * Only closes the connection the server side of the connection when the other side is already closed.
	 */
	public synchronized void serverClose(){
		try {
			out.close();
		} catch (IOException e1) {
			System.out.println("Problem arised while trying to close server stream");
		}
	}
	
	/**
	 * Creates new XML Object that will serve for communication with the other application.
	 * @param hostName The host that we want to connect to.
	 * @param port The port that we want to connect to.
	 * @return New XMLHandler object or null if the communication failed. 
	 */
	public static XmlService createXMLHandler(String hostName, int port){
		Socket s;
		try {
			s = new Socket(hostName, port);
		} catch (Exception ex) {
			System.out.println("Unable to connect to " + hostName);
			return null;
		}
		return createXMLHandler(s);
	}
	
	/**
	 * Creates new XML Object that will serve for communication with the other application.
	 * @param s The socket that should be used for communication.
	 * @return New XMLHandler object or null if the communication failed.
	 */
	public static XmlService createXMLHandler(Socket s){
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			out = new ObjectOutputStream (s.getOutputStream());
			out.flush();
			in = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			System.out.println("Couldn't create object for server/client communication");
			return null;
		}
		
		return new XmlService(in, out);
	}
}
