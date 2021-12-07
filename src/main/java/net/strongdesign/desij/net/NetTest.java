

package net.strongdesign.desij.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class NetTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ServerSocket socket = new ServerSocket(3456);
		
		
		Socket accept = socket.accept();
		
		
		InputStream inputStream = accept.getInputStream();
		
		InputStreamReader bis = new InputStreamReader(inputStream);
		
		BufferedReader br = new BufferedReader(bis);
		
		String line = null;
		
		
		
		do {
			line = br.readLine();
			System.out.println(line);
		} while (!line.equals("exit"));
		

		socket.close();
	}
	
	
	
	

}
