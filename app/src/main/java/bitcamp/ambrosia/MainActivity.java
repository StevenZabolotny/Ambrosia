package bitcamp.ambrosia;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.http.ServiceCallback;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.CategoriesResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.DocumentEmotionResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.DocumentSentimentResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private File cache;

    private DisorderParser disorderParser;

    String conversationStarters[] = {
            "How are you doing today?",
            "Do you have anything planned today?",
            "How was your day today?",
            "What's up?",
            "How's it going?"
    };

    String sadMessages[] = {
            "Don't worry, it'll be alright.",
            "That sucks...",
            "I'm sorry to hear that.",
            "I hope you feel better.",
            "Oh... :(",
            "I can’t really understand what you are feeling, but I can offer my compassion."
    };

    String sadQuestions[] = {
            "Is there anything that can take your mind off of it?",
            "You can always talk to me or any of your friends about it.",
            "Do you want a hug? :)",
            "Maybe tomorrow will be better?",
            "Maybe there's a good side to it?"
    };

    String happyMessages[] = {
            "Nice!",
            "I'm happy for you.",
            "You sound excited.",
            "Wow, that sounds great!",
            "I'm glad things are going your way."
    };

    String happyQuestions[] = {
            "Tell me more.",
            "Do you have any other interestng stories?",
            "Any other good news?",
            "What do you like to do when you're in a good mood?",
            "I'm hoping tomorrow will be a good day, too."
    };

    String angryMessages[] = {
            "You sound excited.",
            "Calm down. It's okay.",
            "You should just close your eyes and listen to some music.",
            "It's okay to vent about it if you want.",
            "Sometimes things can be frustrating."
    };

    String angryQuestions[] = {
            "Tell me more.",
            "Did anything else happen after that?",
            "Are you angry?",
            "What else grinds your gears?",
            "I hope you're not planning on making a hasty decision..."
    };

    String disgustMessages[] = {
            "Weird...",
            "I don't think I would like that very much either.",
            "That sounds gross.",
            "I hope that didn't make you uncomfortable.",
            "Ugh, that sounds disgusting"
    };

    String disgustQuestions[] = {
            "Can you be more descriptive?",
            "What else happened?",
            "I can't tell if that's a good thing or a bad thing.",
            "I wonder what it was like...",
            "What can you do about it?"
    };

    String fearMessages[] = {
            "That's really not cool.",
            "I hope you're safe.",
            "I wish I could help you...",
            "That sounds frightening!",
            "Oh my god, I'd faint if that happened to me!"
    };

    String fearQuestions[] = {
            "What did you do after?",
            "Are you alright?",
            "I hope everyone is safe...",
            "Do you have any friends you can ask for help?",
            "Do you need me to contact emergency services for you?"
    };

    // Variables related to list
    private ListView messagesListView;
    private MessagesListAdapter messagesListAdapter;
    private ArrayList<Message> messages;
    private ArrayList<Message> pastMessages;

    // Variables related to user input
    ImageButton sttButton;
    Button sendButton;
    EditText editText;

    private TextToSpeech tts;
    private NaturalLanguageUnderstanding nlu;

    private String name;
    private ArrayList<String> history;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final SharedPreferences sp = getSharedPreferences("", Context.MODE_PRIVATE);
        if(savedInstanceState == null) {
            // If the instance is null, this app was just opened
            // Set the messages list to a new empty one
                disorderParser = new DisorderParser();
                messages = new ArrayList<Message>();

                reloadPastMessages();
        } else {
            // Load messages from savedState
            messages = savedInstanceState.getParcelableArrayList("messages");
        }

        history = new ArrayList<String>();
        cache = new File(this.getFilesDir(), "cache.txt");
        try {
            BufferedReader r = new BufferedReader(new FileReader(cache));
            while(r.ready()) {
                history.add(r.readLine());
            }
            r.close();
        } catch(IOException e) {}

        messagesListAdapter = new MessagesListAdapter(this, messages);
        messagesListView = (ListView) findViewById(R.id.list_messages);
        messagesListView.setAdapter(messagesListAdapter);

        tts = new TextToSpeech(this, this);
        nlu = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27);
        nlu.setUsernameAndPassword("b104d1fb-e584-4470-9df2-3fcaed2ccd29", "mfBvfXIcV27E");
        nlu.setEndPoint("https://gateway.watsonplatform.net/natural-language-understanding/api");

        name = sp.getString("name", "");
        if("".equals(name)) {
            sendFromAmbrosia("Hello, my name is Ambrosia. I'm a personal chatbot with an emphasis on mental health. What's your name?");
        } else if(savedInstanceState == null) {
            sendFromAmbrosia(conversationStarters[getRandomNumber(0, 4)]);
        }

        sttButton = (ImageButton) findViewById(R.id.button_stt);
        sendButton = (Button) findViewById(R.id.button_send);
        editText = (EditText) findViewById(R.id.text_edit);

        sttButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use Android Speech to Text API to listen to user speech and put the words into the EditText
                // Create Recognizer Intent which will listen and then send result to onActivityResult
                Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                startActivityForResult(recognizerIntent, 1);
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userInput = editText.getText().toString().trim();

                try {
                    Date date = new Date();
                    BufferedWriter w = new BufferedWriter(new FileWriter(cache, true));
                    w.write(date.getTime() + " U " + userInput);
                    w.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {}

                if(userInput.length() > 0) {
                    messagesListAdapter.add(new Message(false, userInput));
                    messagesListAdapter.notifyDataSetChanged();
                    if(!"".equals(name)) {
                        processInput(userInput);
                    } else {
                        name = userInput;
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("name", name);
                        editor.commit();
                        sendFromAmbrosia("Hello, " + name + "!");
                        sendFromAmbrosia(conversationStarters[getRandomNumber(0, 4)]);
                    }
                    editText.getText().clear();
                }
            }
        });
    }

    private void reloadPastMessages() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //if(results.size() == 0 || results.size() > 2) {
            if(results.size() == 0) {
                Toast.makeText(this, "Sorry, Try speaking a bit clearer", Toast.LENGTH_LONG).show();
            } else {
                editText.getText().clear();
                editText.getText().append(results.get(0));
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void getLocation() {
        LocationManager locationManager;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("messages", messages);
    }

    private void sendFromAmbrosia(String s) {
        try {
            Date date = new Date();
            BufferedWriter w = new BufferedWriter(new FileWriter(cache, true));
            w.write(date.getTime() + " A " + s);
            w.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {}

        // Add message to the adapter
        messagesListAdapter.add(new Message(true, s));
        // Signal update
        messagesListAdapter.notifyDataSetChanged();
        messagesListView.smoothScrollToPosition(messagesListAdapter.getCount() - 1);

        tts.speak(s, TextToSpeech.QUEUE_ADD, null);
    }

    // Analyze input and then send a message back from Ambrosia
    private void processInput(String input) {
        final String inputc = input;
        CategoriesOptions categories = new CategoriesOptions();
        SentimentOptions sentiment = new SentimentOptions.Builder().build();
        EmotionOptions emotions = new EmotionOptions.Builder().build();
        ConceptsOptions concepts = new ConceptsOptions.Builder().build();
        Features features = new Features.Builder().sentiment(sentiment).emotion(emotions).concepts(concepts).categories(categories).build();
        AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(input).features(features).build();
        nlu.analyze(parameters).enqueue(new ServiceCallback<AnalysisResults>() {
            @Override
            public void onResponse(final AnalysisResults response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DocumentEmotionResults der = response.getEmotion().getDocument();
                        DocumentSentimentResults dsr = response.getSentiment().getDocument();
                        List<ConceptsResult> cr = response.getConcepts();
                        List<CategoriesResult> ctr = response.getCategories();
                        String message = "";
                        if (!emergencyCheck(inputc, cr, ctr)) {
                            message = chooseResponse(der, dsr, cr, ctr);
                        } else {
                            message = "EMERGENCY DETECTED! If you are thinking about hurting yourself or anyone else or believe that you are not in a good state of mind, please:\nCall 911 for Emergency Services\nText CONNECT to 741741 for Mental Health Hotlines\nCall 1-800-273-8255 for the National Suicide Prevention Lifeline.";
                        }
                        sendFromAmbrosia(message);
                    }
                });

            }

            @Override
            public void onFailure(final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

    }

    private String chooseResponse(DocumentEmotionResults der, DocumentSentimentResults dsr, List<ConceptsResult> cr, List<CategoriesResult> ctr) {
        double sent = dsr.getScore();
        double sad = der.getEmotion().getSadness();
        double joy = der.getEmotion().getJoy();
        double fea = der.getEmotion().getFear();
        double mad = der.getEmotion().getAnger();
        double dis = der.getEmotion().getDisgust();
        double avg = (sad + joy + fea + mad + dis) / 5.0;
        double max = Math.max(Math.max(Math.max(Math.max(sad, joy), fea), mad), dis);

        String message = "";
        double mod = ((max - avg) / 0.8);
        int pick1 = modifiedRandom(mod);
        int pick2 = modifiedRandom(mod);
        if (max <= 0.2) {
            message = "I'm sorry, I'm not quite sure I understand. Can you please clarify?";
        } else if (max == sad) {
            message = sadMessages[pick2] + " " + sadQuestions[pick1];
        } else if (max == joy) {
            message = happyMessages[pick2] + " " + happyQuestions[pick1];
        } else if (max == fea) {
            message = fearMessages[pick2] + " " + fearQuestions[pick1];
        } else if (max == mad) {
            message = angryMessages[pick2] + " " + angryQuestions[pick1];
        } else if (max == dis) {
            message = disgustMessages[pick2] + " " + disgustQuestions[pick1];
        } else {
            message = "I'm sorry, I'm not quite sure I understand. Can you please clarify?";
        }
        return message;
    }

    private int modifiedRandom(double mod) {
        mod = (mod - 0.5) / 5;
        Random r = new Random();
        double pick = r.nextDouble();
        if (pick <= (0.2 - 2*mod)) {
            return 0;
        } else if (pick <= (0.4 - 3*mod)) {
            return 1;
        } else if (pick <= (0.6 - 3*mod)) {
            return 2;
        } else if (pick <= (0.8 - 2*mod)) {
            return 3;
        }
        return 4;
    }

    private int getRandomNumber(int l, int h) {
        Random r = new Random();
        return r.nextInt(h - l) + l;
    }

    private boolean emergencyCheck(String input, List<ConceptsResult> cr, List<CategoriesResult> ctr) {
        String[] sa = input.split(" ");
        ArrayList<String> s = new ArrayList<String>();
        for (int i = 0;i < sa.length;i++) {
            s.add(sa[i]);
        }
        ArrayList<String> crs = new ArrayList<String>();
        for (int i = 0;i < cr.size();i++) {
            crs.add(cr.get(i).getText());
        }
        ArrayList<String> ctrs = new ArrayList<String>();
        for (int i = 0;i < ctr.size();i++) {
            ctrs.add(ctr.get(i).getLabel());
            //Toast.makeText(this, ctrs.get(i), Toast.LENGTH_SHORT).show();
        }
        boolean self = false;
        boolean emergency = false;
        if (s.contains("myself") || s.contains("me") || s.contains("I")) {
            self = true;
        }
        if (self && ((crs.contains("kill") || crs.contains("violence") || crs.contains("suicide") || crs.contains("hurt"))
            || (s.contains("kill") || s.contains("violence") || s.contains("suicide") || s.contains("hurt")))) {
            emergency = true;
        }
        return emergency;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }
}
