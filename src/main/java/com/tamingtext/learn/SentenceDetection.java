package com.tamingtext.learn;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class SentenceDetection {

    public static void main(String[] args) {
        File modelFile = new File(System.getProperty("user.dir") + "/opennlp-models", "en-sent.bin");
        try {
            InputStream modelStream = new FileInputStream(modelFile);
            SentenceModel model = new SentenceModel(modelStream);
            SentenceDetector detector = new SentenceDetectorME(model);
            String testString = "This is a sentence. It has fruits, vegetables," +
                    " etc. but does not have meat. Mr. Smith went to Washington.";
            String[] result = detector.sentDetect(testString);
            for (int i = 0; i < result.length; i++) {
                System.out.println("Sentence: " + result[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
