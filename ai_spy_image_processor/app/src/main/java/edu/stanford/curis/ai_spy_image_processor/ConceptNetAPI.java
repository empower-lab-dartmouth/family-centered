package edu.stanford.curis.ai_spy_image_processor;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class ConceptNetAPI {

    private final static String IS_RELATION = "IsA";
    private final static String HAS_RELATION = "HasA";
    private final static String USED_RELATION = "UsedFor";
    private final static String CAPABLE_RELATION = "CapableOf";
    private final static String SIMILAR_RELATION = "SimilarTo";
    private final static String MADE_RELATION = "MadeOf";
    private final static String[] RELATIONS = new String[]{IS_RELATION, HAS_RELATION, USED_RELATION, CAPABLE_RELATION, SIMILAR_RELATION, MADE_RELATION};
    private final static String[] RELATIONS_NATURAL = new String[]{"is ", "has ", "is used ", "is capable of ", "is similar to ", "is made of "};

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

    public static String getReadableRepresentation(HashMap<String, ArrayList<String>> map){
        String display = "ConceptNet Map:";

        for (String relation : map.keySet()){
            ArrayList<String> endPoints = map.get(relation);
            String results = endPoints.size() > 0 ? endPoints.toString() : "No results for this relation";
            String line = relation + ":\n" + results + "\n";
            display = display + "\n" + line;
        }

        return display;
    }

    public static String makeConceptNetClue(String relation, String endpoint){

        HashMap<String, String> relationToLanguage = new HashMap<>();
        for (int i = 0; i < RELATIONS.length; i++){
            relationToLanguage.put(RELATIONS[i], RELATIONS_NATURAL[i]);
        }

        HashSet<Character> vowels = new HashSet<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');

        String clue = "";

        switch (relation){
            case IS_RELATION:
                if (vowels.contains(endpoint.charAt(0))) endpoint = "an " + endpoint;
                else endpoint = "a " + endpoint;
                clue = relationToLanguage.get(relation) + " " + endpoint;
                break;
            case HAS_RELATION:
                clue = relationToLanguage.get(relation) + endpoint;
                break;
            case USED_RELATION:
                if (endpoint.contains("ing")) endpoint = "for " + endpoint;
                else endpoint = "to " + endpoint;
                clue = relationToLanguage.get(relation) + endpoint;
                break;
            case CAPABLE_RELATION:
                String newEndpoint = endpoint;
                if (!endpoint.contains("ing")){
                    int i = endpoint.indexOf(" ");
                    String begin = endpoint.substring(0, i);
                    String end = endpoint.substring(i);
                    newEndpoint = begin + "ing" + end;
                }
                clue = relationToLanguage.get(relation) + newEndpoint;
                break;
            case SIMILAR_RELATION:
                clue = relationToLanguage.get(relation) + "a " + endpoint;
                break;
            case MADE_RELATION:
                clue = relationToLanguage.get(relation) + " " + endpoint;
                break;
        }

        return clue;
    }

}
