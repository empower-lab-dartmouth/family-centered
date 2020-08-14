package projects.android.aispy;

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
//    private final static String SIMILAR_RELATION = "SimilarTo";
    private final static String MADE_RELATION = "MadeOf";
    private final static String[] RELATIONS = new String[]{IS_RELATION, HAS_RELATION, USED_RELATION, CAPABLE_RELATION, MADE_RELATION};
    private final static String[] RELATIONS_NATURAL = new String[]{"is ", "has ", "is used ", "is capable of ", "is made of "};

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

        if (map == null) return display;

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



        String clue = "";
        endpoint = endpoint + " ";
        switch (relation){
            case IS_RELATION:
                endpoint = handleISGrammer(endpoint);
                break;
            case HAS_RELATION:
                break;
            case USED_RELATION:
                endpoint = handleUSEDGrammer(endpoint);
                break;
            case CAPABLE_RELATION:
                endpoint = handleCAPABLEGrammer(endpoint);
                break;
            case MADE_RELATION:
                break;
        }

        clue = relationToLanguage.get(relation) + endpoint;
        return clue;
    }

    private static String handleISGrammer(String endpoint){
        HashSet<Character> vowels = new HashSet<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');

        if (endpoint.endsWith("ness")) endpoint = endpoint.replace("ness", "");
        if (endpoint.contains("a ") || endpoint.contains(("an "))) return endpoint;

        if (endpoint.charAt(endpoint.length() - 1) == 's') return endpoint;

        if (vowels.contains(endpoint.charAt(0))) endpoint = "an " + endpoint;
        else endpoint = "a " + endpoint;

        return endpoint;
    }

    private static String handleUSEDGrammer(String endpoint){

        if (Character.isUpperCase(endpoint.charAt(0)) || endpoint.contains("ing ") || endpoint.contains("ship") || endpoint.contains("tion")) endpoint = "for " + endpoint; //Ex: "used for Art", "used for writing", "used for companionship", "used for hydration"
         else if (endpoint.startsWith("a ")) endpoint = "as " + endpoint; //Ex: a pet => "used as a pet"
        else if (!endpoint.startsWith("to ")) endpoint = "to " + endpoint; //EX: "used to write"
        return endpoint;
    }

    private static String handleCAPABLEGrammer(String endpoint){
        //Ex: "capable of hydration", "capable of companionship"
        if(endpoint.contains("tion") || endpoint.contains("ship")) return endpoint;

        int i = endpoint.indexOf(" ");
        String begin = endpoint;
        String end = "";

        if (i != -1) {
            begin = endpoint.substring(0, i);
            end = endpoint.substring(i);
        }

        //Handle adding "ing"
        String newEndpoint = endpoint;
        if (!begin.contains("ing")){

            //Handle double consonants before adding "ing"
            switch (begin.charAt(begin.length() - 1)){
                case 'p':
                    begin = begin + "p"; //Ex: stop => "capable of stopping"
                    break;
                case 't':
                    begin = begin + "t"; //Ex: hit => "capable of hitting"
                    break;
                case 'b':
                    begin = begin + "b"; //Ex: rub => "capable of rubbing"
                    break;
            }

            //Handle possibly removing a final 'e' before adding "ing"
            if(!begin.equals("be") && begin.charAt(begin.length() - 1) == 'e' && !begin.endsWith("se")){
                begin = begin.substring(0, begin.length() - 1); //Ex: bite => "capable of biting"
            }

            //Handle possibly removing a final 's' before adding "ing"
            if(begin.charAt(begin.length() - 1) == 's' && !begin.endsWith("ss")){
                begin = begin.substring(0, begin.length() - 1); //Ex: jumps => "capable of jumping"
            }

            newEndpoint = begin + "ing" + end;
        }
        return newEndpoint;
    }
}
