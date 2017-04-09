package bitcamp.ambrosia;

import android.os.health.SystemHealthManager;
import android.util.Log;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.porterStemmer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Riyadh on 4/9/2017.
 */

public class DisorderParser {

    private HashMap<String, Integer> disorders;
    private SnowballStemmer snowballStemmer;

    private String anxietyKeyWords[] = {
            "heart",
            "sweat",
            "fear",
            "dread",
            "panic",
            "anxieti"
    };

    private String moodKeyWords[] = {
            "persist",
            "sad",
            "depress",
            "overli",
            "bipolar"
    };

    private String psychoticKeyWords[] = {
            "hallucin",
            "delus"
    };

    private String eatingKeyWords[] = {
            "hungri",
            "weight",
            "vomit"
    };

    private String impulseKeyWords[] = {
            "impuls",
            "alcohol",
            "drug",
            "steal",
            "compuls",
            "urg"
    };

    private String personalityKeyWords[] = {
            "antisoci"
    };

    private String ocKeyWords[] = {
            "fear",
            "constant",
            "obsess",
            "ritual",
            "unreason",
            "irrat",
            "compuls"
    };

    private String ptsKeyWords[] = {
            "trauma",
            "emot",
            "hallucin"
    };

    public DisorderParser() {
        snowballStemmer = new porterStemmer();

        disorders = new HashMap<String, Integer>();
        disorders.put("anxiety", 0);
        disorders.put("mood", 0);
        disorders.put("psychotic", 0);
        disorders.put("eating", 0);
        disorders.put("impulse", 0);
        disorders.put("personality", 0);
        disorders.put("ocd", 0);
        disorders.put("ptsd", 0);
    }

    // Takes a keyword and then stems it
    // The word is then compared against each array and if words match
    // The value of the disorder is incremented
    public void processKeyWord(String keyword) {
        String stemmed = "";
        snowballStemmer.setCurrent(keyword);
        for(int i = 0; i < keyword.length(); i++) {
            snowballStemmer.stem();
        }
        stemmed = snowballStemmer.getCurrent();

        for(int i = 0; i < anxietyKeyWords.length; i++) {
            if(anxietyKeyWords[i].equals(stemmed)) {
                disorders.put("anxiety", disorders.get("anxiety") + 1);
            }
        }

        for(int i = 0; i < moodKeyWords.length; i++) {
            if(moodKeyWords[i].equals(stemmed)) {
                disorders.put("mood", disorders.get("mood") + 1);
                break;
            }
        }

        for(int i = 0; i < psychoticKeyWords.length; i++) {
            if(psychoticKeyWords[i].equals(stemmed)) {
                disorders.put("psychotic", disorders.get("psychotic") + 1);
                break;
            }
        }

        for(int i = 0; i < eatingKeyWords.length; i++) {
            if(eatingKeyWords[i].equals(stemmed)) {
                disorders.put("eating", disorders.get("eating") + 1);
                break;
            }
        }

        for(int i = 0; i < impulseKeyWords.length; i++) {
            if(impulseKeyWords[i].equals(stemmed)) {
                disorders.put("impulse", disorders.get("impulse") + 1);
                break;
            }
        }

        for(int i = 0; i < personalityKeyWords.length; i++) {
            if(personalityKeyWords[i].equals(stemmed)) {
                disorders.put("personality", disorders.get("personality") + 1);
                break;
            }
        }

        for(int i = 0; i < ocKeyWords.length; i++) {
            if(ocKeyWords[i].equals(stemmed)) {
                disorders.put("ocd", disorders.get("ocd") + 1);
                break;
            }
        }

        for(int i = 0; i < ptsKeyWords.length; i++) {
            if(ptsKeyWords[i].equals(stemmed)) {
                disorders.put("ptsd", disorders.get("ptsd") + 1);
                break;
            }
        }
    }

    public int getWeight(String key) {
        return disorders.get(key);
    }

}
