package com.tamingtext.learn;

import org.tartarus.snowball.ext.EnglishStemmer;

public class Stem {

    public static void main(String[] args) {
        EnglishStemmer english = new EnglishStemmer();
        String[] test = {"bank", "banks", "banking", "banker", "banked", "bankers"};
        String[] gold = {"bank", "bank", "bank", "banker", "bank", "banker"};
        for (int i = 0; i < test.length; i++) {
            english.setCurrent(test[i]);
            english.stem();
            System.out.println("English: " + english.getCurrent());
//            assertTrue(english.getCurrent() + " is not equal to " + gold[i], english.getCurrent().equals(gold[i]) == true);
            if (english.getCurrent().equals(gold[i]) == false) {
                System.out.println( english.getCurrent() + " is not equal to " + gold[i]);
            }
        }
    }
}
