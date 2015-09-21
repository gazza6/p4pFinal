package server;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

// server-side
public class MiniServer extends Thread{

	private Socket socket = null;

	public MiniServer(Socket socket) {

		super("MiniServer");
		this.socket = socket;

	}

	public void run(){
		try {
			DataInputStream dIn = new DataInputStream(socket.getInputStream());
			int length = dIn.readInt(); 
			byte[] message = new byte[length];// read length of incoming message
			if(length>0) {
				
				dIn.readFully(message, 0, message.length); // read the message
			}
			
			String address = "";
			File f = null;
			System.out.println("Receive data from port: 7000");
			for (int i = 0; i < 50; i++){
				address = "C:\\Users\\gary\\Documents\\InputVoice\\testing"+String.valueOf(i)+".wav";
				f = new File(address);
				if(f.exists() && !f.isDirectory()) {
					continue;
				}
				f.createNewFile();
				break;


			}
			byteArrayToWav(message, address, f);
			
			System.out.println("Transcribing voice...");
			
			recogniseVoice rv = new recogniseVoice();
			String recognisedText = rv.recognise(address);
			System.out.println("The text is: " + recognisedText);

			DataOutputStream outToClient = new DataOutputStream(socket.getOutputStream());
			outToClient.writeUTF(recognisedText);
			outToClient.flush();
			outToClient.close();
			dIn.close();
			f.delete();
			System.out.println("Send text to client");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Server received unknown source.");
		}
	}
	private static void byteArrayToWav(byte[] clipData, String address, File f){
	    try {
	        long mySubChunk1Size = 16;
	        int myBitsPerSample= 16;
	        int myFormat = 1;
	        long myChannels = 1;
	        long mySampleRate = 16000;
	        long myByteRate = mySampleRate * myChannels * myBitsPerSample/8;
	        int myBlockAlign = (int) (myChannels * myBitsPerSample/8);

	        long myDataSize = clipData.length;
	        long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/8;
	        long myChunkSize = 36 + myChunk2Size;

	        OutputStream os;   
	        os = new FileOutputStream(f);
	        BufferedOutputStream bos = new BufferedOutputStream(os);
	        DataOutputStream outFile = new DataOutputStream(bos);

	        outFile.writeBytes("RIFF");                                 // 00 - RIFF
	        outFile.write(intToByteArray((int)myChunkSize), 0, 4);      // 04 - how big is the rest of this file?
	        outFile.writeBytes("WAVE");                                 // 08 - WAVE
	        outFile.writeBytes("fmt ");                                 // 12 - fmt 
	        outFile.write(intToByteArray((int)mySubChunk1Size), 0, 4);  // 16 - size of this chunk
	        outFile.write(shortToByteArray((short)myFormat), 0, 2);     // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
	        outFile.write(shortToByteArray((short)myChannels), 0, 2);   // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
	        outFile.write(intToByteArray((int)mySampleRate), 0, 4);     // 24 - samples per second (numbers per second)
	        outFile.write(intToByteArray((int)myByteRate), 0, 4);       // 28 - bytes per second
	        outFile.write(shortToByteArray((short)myBlockAlign), 0, 2); // 32 - # of bytes in one sample, for all channels
	        outFile.write(shortToByteArray((short)myBitsPerSample), 0, 2);  // 34 - how many bits in a sample(number)?  usually 16 or 24
	        outFile.writeBytes("data");                                 // 36 - data
	        outFile.write(intToByteArray((int)myDataSize), 0, 4);       // 40 - how big is this data chunk
	        outFile.write(clipData);                                    // 44 - the actual data itself - just a long string of numbers

	        outFile.flush();
	        outFile.close();

	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	}


	private static byte[] intToByteArray(int i)
	    {
	        byte[] b = new byte[4];
	        b[0] = (byte) (i & 0x00FF);
	        b[1] = (byte) ((i >> 8) & 0x000000FF);
	        b[2] = (byte) ((i >> 16) & 0x000000FF);
	        b[3] = (byte) ((i >> 24) & 0x000000FF);
	        return b;
	    }

	    // convert a short to a byte array
	    public static byte[] shortToByteArray(short data)
	    {
	        /*
	         * NB have also tried:
	         * return new byte[]{(byte)(data & 0xff),(byte)((data >> 8) & 0xff)};
	         * 
	         */

	        return new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};
	    }
}