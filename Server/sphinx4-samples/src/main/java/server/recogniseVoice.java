package server;

import java.io.FileInputStream;
import java.io.InputStream;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class recogniseVoice {

	public String recognise(String address) throws Exception{
		System.out.println("Loading models...");
		
		Configuration configuration = new Configuration();
		configuration
		.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		configuration
		.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		configuration
		.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

		StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(
				configuration);
		
		InputStream stream = new FileInputStream(address);
		SpeechResult result;
		recognizer.startRecognition(stream);
        result = recognizer.getResult();
        recognizer.stopRecognition();
        return result.getHypothesis();
	}

}
