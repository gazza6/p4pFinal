package receiver;

import java.io.File;
import java.io.PrintWriter;

public class ResetFiles {
	public static void main(String[] argv) throws Exception {

		File f1 = new File("/Users/shenghaolu/Documents/testSphinx/online/output.txt");
		PrintWriter writer1 = new PrintWriter(f1);
		writer1.print("");
		writer1.close();

		File f2 = new File("/Users/shenghaolu/Documents/testSphinx/online/result.txt");
		PrintWriter writer2 = new PrintWriter(f2);
		writer2.print("");
		writer2.close();

		File f3 = new File("/Users/shenghaolu/Documents/testSphinx/offline/output.txt");
		PrintWriter writer3 = new PrintWriter(f3);
		writer3.print("");
		writer3.close();

		File f4 = new File("/Users/shenghaolu/Documents/testSphinx/offline/result.txt");
		PrintWriter writer4 = new PrintWriter(f4);
		writer4.print("");
		writer4.close();

		File folder = new File("/Users/shenghaolu/Documents/testSphinx/online/");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				if(!listOfFiles[i].getName().equals("input.txt") &&!listOfFiles[i].getName().equals("output.txt") && !listOfFiles[i].getName().equals("result.txt")){
					listOfFiles[i].delete();
				}
			} 
		}

		System.out.println("Done");
	}

}
