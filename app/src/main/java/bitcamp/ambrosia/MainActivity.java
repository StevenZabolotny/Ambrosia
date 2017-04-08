package bitcamp.ambrosia;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // Variables related to list
    ListView messagesListView;
    MessagesListAdapter messagesListAdapter;
    ArrayList<Message> messages;

    // Variables related to user input
    Button sttButton;
    Button sendButton;
    EditText editText;

    private TextToSpeech tts;

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

        start();

        sttButton = (Button) findViewById(R.id.button_stt);
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
            if(results.size() == 0 || results.size() > 2) {
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

    private void start() {
            // Send question from Ambrosia
            sendFromAmbrosia("Yo sup b");

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
        sendFromAmbrosia("Cool story bro");
    }

    @Override
    public void onInit(int status) {
    }
}
