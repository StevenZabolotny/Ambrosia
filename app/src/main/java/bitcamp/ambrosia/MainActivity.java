package bitcamp.ambrosia;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
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
import java.io.PrintWriter;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private File cache;
	private DoctorData doctors;

    private File resp; // File that contains the hard coded responses

    private final int MIN_STORY_WORD_COUNT = 20;
    private DisorderParser disorderParser;
    private LocationManager locationManager;
    private Location currentLocation;

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

    String transitions[] = {
            "Go on.",
            "Tell me more.",
            "What else can you tell me?",
            "I'm listening.",
            "Uh-huh.",
            "Yeah...",
            "I want to hear more from you.",
            "Keep going.",
            "Interesting...",
            "And then what happened?"
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
    private CSString respin;
    private CSString respout;
    private String prevInput;

    private double sadtot;
    private double joytot;
    private double featot;
    private double madtot;
    private double distot;
    private int wordcount;
    private int entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        final SharedPreferences sp = getSharedPreferences("", Context.MODE_PRIVATE);
        if (savedInstanceState == null) {
            // If the instance is null, this app was just opened
            // Set the messages list to a new empty one
            disorderParser = new DisorderParser();
			doctors = new DoctorData();
            messages = new ArrayList<Message>();

            reloadPastMessages();
        } else {
            // Load messages from savedState
            messages = savedInstanceState.getParcelableArrayList("messages");
        }

        /*NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, BroadcastReceiver.class);
        PendingIntent pintent = PendingIntent.getActivity(this, (int)System.currentTimeMillis(), intent, 0);
        Notification n = new Notification.Builder(this).setContentTitle(name + ", feeling lonely?").setContentText("Ambrosia would like to talk to you!").setAutoCancel(false).build();
        notificationManager.notify(0, n);*/

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        listenForLocationChanges();

        history = new ArrayList<String>();
        cache = new File(this.getFilesDir(), "cache.txt");
        try {
            BufferedReader r = new BufferedReader(new FileReader(cache));
            while (r.ready()) {
                history.add(r.readLine());
            }
            r.close();
        } catch(IOException e) {}
        /*
        for (String s:history) {
            Log.d("test", s + "\n");
        }*/
        // Opening response file
        resp = new File(this.getFilesDir(), "resp.txt");
        respin = new CSString();
        respout = new CSString();
        prevInput = new String();
        try {
            BufferedReader r = new BufferedReader(new FileReader(resp));
            while(r.ready()) {
                // String is formated "Input : Output"
                String line = r.readLine();
                try {
                    String[] parts = line.split(" : ");
                    respin.add(parts[0]);
                    respout.add(parts[1]);
                } catch(NullPointerException e) {
                    messagesListAdapter.add(new Message(false, "Resp.txt file parsing error."));
                    messagesListAdapter.notifyDataSetChanged();
                }
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
        if ("".equals(name)) {
            sendFromAmbrosia("Hello, my name is Ambrosia. I'm a personal chatbot with an emphasis on mental health. What's your name?");
        } else if (savedInstanceState == null) {
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
                    w.newLine();
                    w.close();
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }



                if(userInput.length() > 0) {
                    if (userInput.toLowerCase().contains("/read")) {
                        try {
                            BufferedReader r = new BufferedReader(new FileReader(resp));
                            while(r.ready()) {
                                // String is formated "Input : Output"
                                String line = r.readLine();
                                Log.d("aaa", line);
                            }
                            r.close();
                        } catch(IOException e) {}
                    } else if (userInput.toLowerCase().contains("/clear")) {
                        try {
                            PrintWriter pw = new PrintWriter(resp);
                            pw.close();
                            respin = new CSString();
                            respout = new CSString();
                        } catch (FileNotFoundException e) {

                        }
                    } else if (userInput.toLowerCase().contains("/add ")) {
                        // Should replace if prevInput is in respin
                        if (respin.contains(prevInput)) {
                            try {
                                // just rewrite into resp
                                BufferedWriter w = new BufferedWriter(new FileWriter(cache, false));
                                respout.set(respin.indexOf(prevInput), userInput.replace("/add ", ""));
                                for (int i=0; i<respin.size(); i++) {
                                    w.write(respin.get(i) + " : " + respout.get(i));
                                    w.newLine();
                                }
                                w.close();
                            } catch (FileNotFoundException e) {}
                            catch (IOException e) {}

                        } else {
                            String formated = userInput.replace("/add ", "");
                            respin.add(prevInput);
                            respout.add(formated);

                            try {
                                BufferedWriter w = new BufferedWriter(new FileWriter(resp, true));
                                w.write(prevInput + " : " + formated);
                                w.newLine();
                                w.close();
                            } catch (FileNotFoundException e) {
                            } catch (IOException e) {
                            }
                        }
                    } else if (respin.contains(userInput)) {
                        // Need to parse userinput for trash later and refine compare method
                        prevInput = userInput;
                        messagesListAdapter.add(new Message(false, userInput));
                        messagesListAdapter.notifyDataSetChanged();

                        int iof = respin.indexOf(userInput);
                        String returnString = respout.get(iof);
                        sendFromAmbrosia(returnString);
                    }
                    else {
                        prevInput = userInput;
                        messagesListAdapter.add(new Message(false, userInput));
                        messagesListAdapter.notifyDataSetChanged();
                        if (!"".equals(name)) {
                            processInput(userInput);
                        } else {
                            name = userInput;
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", name);
                            editor.commit();
                            sendFromAmbrosia("Hello, " + name + "!");
                            sendFromAmbrosia(conversationStarters[getRandomNumber(0, 4)]);
                        }
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
        if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //if(results.size() == 0 || results.size() > 2) {
            if (results.size() == 0) {
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

    private void listenForLocationChanges() {
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                currentLocation = location;
                Toast.makeText(MainActivity.this, String.valueOf(currentLocation.getLatitude()), Toast.LENGTH_LONG).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
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
            w.newLine();
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
                        String[] inputs = inputc.split(" ");
                        wordcount += inputs.length;
                        entries++;
                        DocumentEmotionResults der = response.getEmotion().getDocument();
                        DocumentSentimentResults dsr = response.getSentiment().getDocument();
                        List<ConceptsResult> cr = response.getConcepts();
                        List<CategoriesResult> ctr = response.getCategories();
                        String message = "";
                        if (!emergencyCheck(inputc, cr, ctr)) {
							String[] splitStr = input.trim().split("\\s+");
							for(int i = 0; i < splitStr.size(); i++)
							{
								if(splitStr[i] == "doctor" || splitStr[i] == "doctor." || splitStr[i] == "doctor?")
								{
									String[][] options = doctors.getData(splitStr[i-1], "39.0016", "-77.0353");
									message = "Here are some for you to choose from: \n";
									for(int i = 0; i < 3; i++)
									{
										message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
									}
									break
								}
								else
								{
									disorderParser.processKeyWord(splitStr[i]);
								}
							}
							String disorder = disorderParser.checkWeights()
							if(message != "")
							{
								message = message;
							}
							else if (inputc.charAt(inputc.length() - 1) == '?') {
                                message = "Don't worry about me, I'm here to hear about you.";
                            }
							else if(disorder == "")
							{
								message = chooseResponse(der, dsr, cr, ctr);
							}
                            else if(disorder == "mood")
							{
								message = "It appears that you are suffering from a severe mood disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "anxiety")
							{
								message = "It appears that you are suffering from a severe anxiety disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "psychotic")
							{
								message = "It appears that you are suffering from a severe psychotic disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "eating")
							{
								message = "It appears that you are suffering from a severe eating disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "impulse")
							{
								message = "It appears that you are suffering from a severe impulse disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "personality")
							{
								message = "It appears that you are suffering from a severe personality disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "ocd")
							{
								message = "It appears that you are suffering from a severe obsessive compulsive disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
							else if(disorder == "ptsd")
							{
								message = "It appears that you are suffering from a severe post traumatic stress disorder. Please arrange an appointment with a psychiatrist. Here are some for you to choose from: \n";
								String[][] options = doctors.getData("Psychiatric", "39.0016", "-77.0353");
								for(int i = 0; i < 3; i++)
								{
									message = message + "Name: " + options[i][0] + "\n Phone: " + options[i][2] + "\n Address: " + options[i][1] + "\n\n";
								}
							}
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

        sadtot += sad;
        joytot += joy;
        featot += fea;
        madtot += mad;
        distot += dis;

        if (wordcount <= MIN_STORY_WORD_COUNT) {
            Random r = new Random();
            int pick = (int)Math.floor(r.nextDouble() * 10);
            return transitions[pick];
        }

        sad = sadtot / entries;
        joy = joytot / entries;
        fea = featot / entries;
        mad = madtot / entries;
        dis = distot / entries;
        double avg = (sad + joy + fea + mad + dis) / 5.0;
        double max = Math.max(Math.max(Math.max(Math.max(sad, joy), fea), mad), dis);

        sadtot = 0;
        joytot = 0;
        featot = 0;
        madtot = 0;
        distot = 0;
        wordcount = 0;
        entries = 0;

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

    public class CSString extends ArrayList<String> {
        @Override
        public int indexOf(Object o) {
            String str = (String) o;
            for (int i = 0; i<this.size(); i++) {
                if (str.replaceAll("[^a-zA-Z]", "").equalsIgnoreCase(this.get(i).replaceAll("[^a-zA-Z]", ""))) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public boolean contains(Object o) {
            return indexOf(o) >= 0;
        }
    }
}
