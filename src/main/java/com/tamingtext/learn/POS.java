package com.tamingtext.learn;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.SimpleTokenizer;

import java.io.File;
import java.io.FileInputStream;

public class POS {

    public static void main(String[] args) {
        String text = "I am Jacob";
        File posModelFile = new File(System.getProperty("user.dir") + "/opennlp-models", "en-pos-maxent.bin");
        try {
            FileInputStream posModelStream = new FileInputStream(posModelFile);
            POSModel model = new POSModel(posModelStream);
            POSTaggerME tagger = new POSTaggerME(model);
            String[] words = SimpleTokenizer.INSTANCE.tokenize(text);
            String[] result = tagger.tag(words);
            for (int i = 0; i < words.length; i++) {
                System.out.println(words[i] + "/" + result[i] + " ");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
