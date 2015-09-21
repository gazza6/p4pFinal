package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class TestServer {
	public static void main(String[] argv) throws Exception {
		String path = "/Users/shenghaolu/Documents/testSphinx/online/";
		File folder = new File(path);
		changeFilesOfFolder(folder);
		System.out.println("Done");
	}

	public static void changeFilesOfFolder(File folder) throws Exception{
		File tf = new File("/Users/shenghaolu/Documents/testSphinx/online/output.txt");
		File[] listOfFiles = folder.listFiles();
		FileWriter fw = new FileWriter(tf,true);
		//BufferedWriter writer give better performance
		BufferedWriter bw = new BufferedWriter(fw);

		for (int i = 0; i < listOfFiles.length - 3; i++){
			
			recogniseVoice rv = new recogniseVoice();
			String address = "/Users/shenghaolu/Documents/testSphinx/online/file_" + String.valueOf(i)+ ".wav";
			long startTime = System.nanoTime();
			String recognisedText = rv.recognise(address);
			
			long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            System.out.println("The text is: " + recognisedText);
			bw.write(recognisedText + " " + duration + "\n");
			
		}
		bw.close();

	} 
	

}
