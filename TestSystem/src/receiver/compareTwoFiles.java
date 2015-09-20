package receiver;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;


public class compareTwoFiles {
	private static String inname = "/Users/shenghaolu/Documents/testSphinx/offline/input.txt";
	private static String outname = "/Users/shenghaolu/Documents/testSphinx/offline/output.txt";
	private static String resultname = "/Users/shenghaolu/Documents/testSphinx/offline/result.txt";

	public static void main(String[] argv) throws Exception {
		String path = "/Users/shenghaolu/Documents/testSphinx/offline/";
		File f = new File(resultname);
		
		
		f.delete();
		File folder = new File(path);
		changeFilesOfFolder(folder);
		System.out.println("Done");
	}

	public static void changeFilesOfFolder(File folder) throws Exception{
		File tf = new File(resultname);
		File in = new File(inname);
		File out = new File(outname);

		int i = 0;
		int max = 0; 
		FileWriter fw = new FileWriter(tf,true);
		//BufferedWriter writer give better performance
		BufferedWriter bw = new BufferedWriter(fw);
		
		BufferedReader reader = new BufferedReader(new FileReader(inname));
		int lines = 0;
		while (reader.readLine() != null) lines++;
		reader.close();
		double finalAccuracy = 0;
		double finalTime = 0;
		
		while(i < lines){
			
			String inputline = Files.readAllLines(Paths.get(inname)).get(i);
			String outputline = Files.readAllLines(Paths.get(outname)).get(i);
			String[] inline = inputline.split(" ");
			String[] outline = outputline.split(" ");
			bw.write("Line " + (i+1) + ":\tInput: " + inputline + ";\tOutput: ");
			
			max = inline.length;
			if(inline.length != outline.length - 1){
				if (inline.length > outline.length){
					max = outline.length;
				} else {
					max = inline.length;
				}
			}
			double count = 0;
			
			for(int j = 0; j < max; j++){
				if (inline[j].equals(outline[j])){
					count++;
				}
				bw.write(outline[j] + " ");
			}
			double accuracy = count / (double)inline.length;
			double result = accuracy * 100;
					
			bw.write("; \t"+ String.valueOf(result) + "% \t" + outline[outline.length-1] + "ns\n");
			finalAccuracy += accuracy;
			finalTime += Double.parseDouble(outline[outline.length-1]);
			i++;
		}
		System.out.println(finalTime + "\n");
		System.out.println(lines + "\n");
		finalAccuracy = finalAccuracy / (double)lines;
		finalTime = finalTime / (double)lines;
		finalAccuracy = finalAccuracy * 100;
		
		String[] infilenamearray = inname.split("/");
		String infilename = infilenamearray[infilenamearray.length - 1];
		String[] outfilenamearray = outname.split("/");
		String outfilename = outfilenamearray[outfilenamearray.length - 1];
		bw.write("\n");
		bw.write("Input filename: "+ infilename + "\n");
		bw.write("Output filename: " + outfilename + "\n");
		
		
		bw.write("\n");
		bw.write("Overall Accuracy: " + String.valueOf(finalAccuracy) + "% \nAverage Execution Time: " + String.valueOf(finalTime) + "ns \n");
		bw.close();

	}

}
