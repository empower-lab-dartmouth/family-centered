package projects.android.aispy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Essentially a 'struct' containing the features of an object:
 * -color (String)
 * -locations (a hashmap mapping a direction ("rightO", "leftOf", "above", "below") to a HashSet of AISpy objects corresponding to that direction
 */
public class Features {

    public String color;
    public HashMap<String, HashSet<AISpyObject>> locations;
    public String wiki;
    public HashMap<String, ArrayList<String>> conceptNet;

}
