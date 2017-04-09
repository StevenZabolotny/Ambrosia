package bitcamp.ambrosia;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.ConceptsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.DocumentEmotionResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.DocumentSentimentResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EmotionOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SentimentOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    String conversationStarters[] = {
            "How are you doing today?",
            "Do you have anything planned today?",
            "How was your day today?",
            "What's up?"
    };

    String sadMessages[] = {
            "I'm sorry to hear that."
    };

    // Variables related to list
    private ListView messagesListView;
    private MessagesListAdapter messagesListAdapter;
    private ArrayList<Message> messages;

    // Variables related to user input
    ImageButton sttButton;
    Button sendButton;
    EditText editText;

    private TextToSpeech tts;
    private NaturalLanguageUnderstanding nlu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            // If the instance is null, this app was just opened
            // Set the messages list to a new empty one
            messages = new ArrayList<Message>();
        } else {
            // Load messages from savedState
            messages = savedInstanceState.getParcelableArrayList("messages");
        }

        messagesListAdapter = new MessagesListAdapter(this, messages);
        messagesListView = (ListView) findViewById(R.id.list_messages);
        messagesListView.setAdapter(messagesListAdapter);

        tts = new TextToSpeech(this, this);
        nlu = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27);
        nlu.setUsernameAndPassword("b104d1fb-e584-4470-9df2-3fcaed2ccd29", "mfBvfXIcV27E");
        nlu.setEndPoint("https://gateway.watsonplatform.net/natural-language-understanding/api");

        if(savedInstanceState == null) {
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
                if(userInput.length() > 0) {
                    messagesListAdapter.add(new Message(false, userInput));
                    messagesListAdapter.notifyDataSetChanged();
                    processInput(userInput);
                    editText.getText().clear();
                }
            }
        });
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("messages", messages);
    }

    private void sendFromAmbrosia(String s) {
        // Add message to the adapter
        messagesListAdapter.add(new Message(true, s));
        // Signal update
        messagesListAdapter.notifyDataSetChanged();

        tts.speak(s, TextToSpeech.QUEUE_ADD, null);
    }

    // Analyze input and then send a message back from Ambrosia
    private void processInput(String input) {
        SentimentOptions sentiment = new SentimentOptions.Builder().build();
        EmotionOptions emotions = new EmotionOptions.Builder().build();
        ConceptsOptions concepts = new ConceptsOptions.Builder().build();
        Features features = new Features.Builder().sentiment(sentiment).emotion(emotions).concepts(concepts).build();
        AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(input).features(features).build();
        nlu.analyze(parameters).enqueue(new ServiceCallback<AnalysisResults>() {
            @Override
            public void onResponse(final AnalysisResults response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DocumentEmotionResults der = response.getEmotion().getDocument();
                        DocumentSentimentResults ser = response.getSentiment().getDocument();
                        List<ConceptsResult> cr = response.getConcepts();
                        double sadness = der.getEmotion().getSadness();
                        String message = "";
                        if(sadness > 0.1 && sadness < 1) {

                        } else {
                            message = "I'm sorry, I'm not quite sure I understand. Can you please clarify?";
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

    int getRandomNumber(int l, int h) {
        Random r = new Random();
        return r.nextInt(h - l) + l;
    }

    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.ENGLISH);
        }
    }
}
