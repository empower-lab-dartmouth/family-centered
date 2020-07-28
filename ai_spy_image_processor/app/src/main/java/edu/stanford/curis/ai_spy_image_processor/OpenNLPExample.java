package edu.stanford.curis.ai_spy_image_processor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

//extract noun phrases from a single sentence using OpenNLP
public class OpenNLPExample {

    static String sentence = "Who is the author of The Call of the Wild?";

    static Set<String> nounPhrases = new HashSet<>();

    public static void main(String[] args) {
        InputStream test = null;
        InputStream modelInParse = null;
        try {
            //load chunking model
            System.out.println(new File(".").getAbsolutePath());
            modelInParse = new FileInputStream("/Users/cmoffitt/Desktop/test.txt");
            modelInParse = new FileInputStream("/Users/cmoffitt/Desktop/en-parser-chunking.bin"); //from http://opennlp.sourceforge.net/models-1.5/
            ParserModel model = new ParserModel(modelInParse);


            //create parse tree
            Parser parser = ParserFactory.create(model);
            Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);

            //call subroutine to extract noun phrases
            for (Parse p : topParses)
                getNounPhrases(p);

            //print noun phrases
            for (String s : nounPhrases)
                System.out.println(s);

            //The Call
            //the Wild?
            //The Call of the Wild? //punctuation remains on the end of sentence
            //the author of The Call of the Wild?
            //the author
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelInParse != null) {
                try {
                    modelInParse.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

    //recursively loop through tree, extracting noun phrases
    public static void getNounPhrases(Parse p) {

        if (p.getType().equals("NP")) { //NP=noun phrase
            nounPhrases.add(p.getCoveredText());
        }
        for (Parse child : p.getChildren())
            getNounPhrases(child);
    }
}