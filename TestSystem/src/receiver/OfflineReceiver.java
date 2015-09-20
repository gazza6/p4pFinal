package receiver;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class OfflineReceiver {

	public static void main(String[] args) {
		System.out.println("Offline test receiver is running...");
		try{
			ServerSocket Ssocket = new ServerSocket(7000);
			while(true){
				Socket socket = Ssocket.accept();
				DataInputStream dIn = new DataInputStream(socket.getInputStream());

				String result = dIn.readUTF();
				System.out.println("Received one result from smartphone");
				System.out.println(result);

				File tf = new File("/Users/shenghaolu/Documents/testSphinx/offline/output.txt");
				FileWriter fw = new FileWriter(tf,true);
				//BufferedWriter writer give better performance
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(result + "\n");
				bw.close();

			}
		} catch (Exception e){
			e.printStackTrace();
		}

	}

}
