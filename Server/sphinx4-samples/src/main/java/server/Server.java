package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

// server-side
public class Server{

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;

		boolean listeningSocket = true;
		try {
			serverSocket = new ServerSocket(7000);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 7000");
		}
		System.out.println("Server is running...");
		
		try{
		while(listeningSocket){
			Socket clientSocket = serverSocket.accept();
			MiniServer mini = new MiniServer(clientSocket);
			mini.start();
		}
		serverSocket.close();  
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Server received unknown source.");
			serverSocket.close();
			Server.main(args);
		}
	}
}

