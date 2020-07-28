package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;

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
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

//https://gist.github.com/johnmiedema/e12e7359bcb17b03b8a0
//extract noun phrases from a single sentence using OpenNLP
public class OpenNLPExample {

    static String sentence = "Who is the author of The Call of the Wild?";

    static Set<String> nounPhrases = new HashSet<>();

    public static void main(Context thisContext) {
        AssetManager assetManager = thisContext.getAssets();


        //TODO: Fix this attempt to initialize a part of speech identifier model
        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
        try {
            InputStream modelStream = assetManager.open("en-pos-maxent.bin");
            POSModel model;

            if(modelStream != null) {
                model = new POSModel(modelStream); //TODO: program stalls here
            } else {
                return;
            }

            POSTaggerME tagger = new POSTaggerME(model);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //TODO: Fix this attempt to initialize a chunking model
        InputStream modelInParse = null;
        try {
            //load chunking model
            modelInParse = assetManager.open("en-parser-chunking.bin");//from http://opennlp.sourceforge.net/models-1.5/

            ParserModel model;

            if (modelInParse != null){
                model = new ParserModel(modelInParse); //TODO: program stalls here
            } else {
                return;
            }

            //create parse tree
            Parser parser = ParserFactory.create(model);
            Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);

            //call subroutine to extract noun phrases
            for (Parse p : topParses)
                getNounPhrases(p);

            //print noun phrases
            for (String s : nounPhrases)
                System.out.println("***************" + s);

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