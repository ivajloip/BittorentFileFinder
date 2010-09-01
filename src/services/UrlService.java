package services;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Handles http communications.
 * @author ivaylo
 *
 */
public class UrlService{
	/**
	 * Reads a web page and gives it in the form of a String.
	 * @param address The location of the page.
	 * @return The contents of the web page at the address.
	 */
	public static String readURL(String address){
		URL url;
		String result = "";
		BufferedReader in = null;
		try {
			url = new URL(address);
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			//System.out.print("buffer opened "+ address + " ");
			String tmp;
			while((tmp = in.readLine()) != null){
				result = result + tmp + "\n";
				//System.out.println(tmp);
			}
			//System.out.println("out");
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("Error getting " + address);
		}
		finally{
			//System.out.println("closing");
			try{
				if(in != null)in.close();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves a file from the Internet.
	 * @param address The location of the file in the Internet.
	 * @param path The location where the file is to be saved.
	 * @return Whether the download was successful.
	 */
	public static boolean copyFile(String address, String path){
		URL url = null;
		FileOutputStream out = null;
		BufferedInputStream in = null;
		boolean tryAgain = true;
		int attempts = 3;
		while (tryAgain && attempts > 0){
			tryAgain = false;
			try{
				url = new URL(address);
				in = new BufferedInputStream(url.openStream());
				out = new FileOutputStream(path);
				byte[] data = new byte[1024];
				int cnt = 0;
				while((cnt = in.read(data)) != -1){
					out.write(data, 0, cnt);
				}
			}
			catch(IOException ex){
				//ex.printStackTrace();
				tryAgain = true;
				attempts--;
				System.out.println(address + " attempts left " + attempts);
			}
			finally{
				try{
					if(in != null)in.close();
					if(out != null)out.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return attempts!=0;
	}
}

