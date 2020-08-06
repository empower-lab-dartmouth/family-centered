package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class ConceptNetAPI {

    private final static String[] RELATIONS = new String[]{"IsA", "HasA", "UsedFor", "CapableOf", "SimilarTo", "MadeOf"};
    private final static String[] RELATIONS_NATURAL = new String[]{"is a ", "has ", "is used for ", "is capable of ", "is similar to ", "is made of "};
    private static HashMap<String, String> relationToLanguage;
    private static HashMap<String, ArrayList<String>> knowledgeGraph;

    public static HashMap<String, ArrayList<String>> getConceptNetMap(String keyword, Context context){

        String targetWord = keyword;

        CountDownLatch latch = new CountDownLatch(RELATIONS.length);
        knowledgeGraph = new HashMap<>();

        for (String relation : RELATIONS){
            ConceptNetQuery worker = new ConceptNetQuery(targetWord.toLowerCase(), relation, knowledgeGraph, context, latch);
            worker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        try {
            latch.await();
            return knowledgeGraph;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static int getConceptNetMapScore(HashMap<String, ArrayList<String>> map){
        int score = 0;
        for (String relation : map.keySet()){
            score += map.get(relation).size();
        }
        return score;
    }

}
