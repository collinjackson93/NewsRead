package edu.vanderbilt.newsread;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.service.voice.AlwaysOnHotwordDetector;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.UiThreadTest;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;
    Button backToHeadlines;
    ToggleButton pauseReading;
    int articleNumber = 0;
    int sentenceNumber = 0;
    Boolean interruptedReading = false;
    SharedPreferences settings;

    // Stores headlines and full articles. Articles are in the second dimension.
    String voiceInput;
    String[][] news = new String [][] {
            {"Makers accelerate a third industrial revolution",
                "Boom! might be the sound of an explosion of successful businesses created by an entirely new generation of inventors with only a few hundred dollars in their pockets who build prototypes at professional makerspaces and raise funds through crowdsourcing."},
            {"Tiny flying robots form teams, cooperate",
                "Vijay Kumar and his students at the University of Pennsylvania build small, agile flying robots that swarm, sense each other, and form ad hoc teams that could be used for search and rescue in large-scale disasters."},
            {"Surfer loses left leg after shark attack in Hawaii",
                "A 25-year-old Oahu man lost his left leg to a shark attack Friday while surfing off Oahu's North Shore, according to local media reports." +
                    "Colin Cook was straddling his board with both legs dangling in the water when the 10- to 12-foot shark latched onto his lower leg, media reports said. He punched the animal until it let go, screamed for help and was assisted to shore by another surfer and a kayaker, his family told reporters." +
                    "Rescuers used a surfboard leach as a tourniquet on Cook's leg until paramedics arrived. Doctors at Queens Medical Center in Honolulu later completed the amputation on his leg, Chris Webster, Cook's cousin, told KHON 2, a Hawaii television station." +
                    "During the attack, the shark got him below the knee ... but they had to take his leg above the knee so that was a little heart-breaking, Webster told the television station. Cook's left hand was also injured." +
                    "Cook is a native of Rhode Island who moved to Hawaii three years ago and learned to create custom surfboards, according to Hawaii News Now." +
                    "The attacked occurred about 10:30 a.m. near a popular surfing area known as Leftovers Beach Park. It was the fifth shark attack in Hawaii this year." +
                    "Lifeguards in the area were warning visitors to stay out of the ocean, and signs have been posted along the beach, according to the Associated Press."},
            {"Etsy.com shares unravel more after Amazon launches rival",
                "Shares of Etsy, the handmade marketplace, slid further Friday after sinking 4% Thursday when Amazon announced a competing marketplace called Handmade at Amazon." +
                    "The stock lost 1.3% by Friday mid-day, to $13.40 per share. That compared with a high of $14.68 on Wednesday before Amazon make its announcement."+
                    "Handmade at Amazon will feature more than 80,000 items, from all 50 states and 60 countries to start, said Peter Faricy, vice president for Amazon Marketplace." +
                    "In response to Amazon’s foray into the hand-crafted market, Etsy’s CEO Chad Dickerson released the following statement:" +
                    "We believe we are the best platform for creative entrepreneurs, empowering them to succeed on their own terms. Etsy has a decade of experience understanding the needs of artists and sellers and supporting them in ways that no other marketplace can. Our platform attracts 21+ million thoughtful consumers seeking to discover unique goods, and build relationships with the people who make and sell them."},
            {"Twitter to have major layoffs next week, report says",
                "Twitter is planning layoffs next week, according to technology news outlet Re/code." +
                    "The cuts will be made company wide, Re/code said." +
                    "We're not commenting on rumor and speculation, Twitter spokesman Jim Prosser said." +
                    "Twitter shares fell 3% in after-hours trading. They closed up about 2% to $30.85 on Friday." +
                    "Twitter had 4,200 employees at the end of June. That's more than double the 2,000 employees it had in the second quarter of 2013. Yet its user growth has not kept pace." +
                    "The layoffs would come one week after Twitter co-founder Jack Dorsey was appointed CEO. He has been serving as interim CEO since July 1." +
                    "Twitter has also apparently shelved plans to expand into a building on Market Street in San Francisco that is home to Uber and Square." +
                    "According to the San Francisco Business Times, Twitter was close to finalizing the deal to take about 100,000 square feet before abandoning the deal."}
        };
    final String[] headlines = new String[news.length];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // set preferences to default the first time the app is run
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    new OperationTask().execute();
                }
            }
        });
        if(settings.getBoolean("setupB", false)){
            t1.setSpeechRate((float) 0.9);
        }

        t1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            public void onDone(String utteranceId) {
                switch (utteranceId) {
                    case "welcome":
                        promptSpeechInput(102);
                        break;
                    case "sentence":
                        if(!interruptedReading) {
                            sentenceNumber++;
                            readCurrentSentence();
                        } else {
                            interruptedReading = false;
                        }
                        break;
                    case "lastSentence":
                        sentenceNumber = 0;
                        promptSpeechInput(101);
                        break;
                    case "headline":
                        if(settings.getBoolean("setupB", false)) {
                            t1.speak("Would you like to read this article or repeat its headline?", TextToSpeech.QUEUE_FLUSH, null, "readArticle?");
                        } else {
                            t1.speak("", TextToSpeech.QUEUE_FLUSH, null, "readArticle?");
                        }
                        break;
                    case "readArticle?":
                        if(settings.getBoolean("setupB", false)) {
                            promptSpeechInput(100, news[articleNumber][0]);
                        } else {
                            promptSpeechInput(100, "Speak now");
                        }
                        break;
                    case "noMatch":
                        promptSpeechInput(101);
                        break;
                    case "tutorial":
                        promptSpeechInput(102);
                        break;
                    case "begin":
                        readCurrentHeadline();
                        break;
                    case "end":
                        promptSpeechInput(103);
                        break;
                    case "restartUnclear":
                        promptSpeechInput(103);
                        break;
                    case "tutorialUnclear":
                        promptSpeechInput(102);
                        break;
                    case "headlineUnclear":
                        promptSpeechInput(100, news[articleNumber][0]);
                        break;
                }
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        backToHeadlines = (Button) findViewById(R.id.backToHeadlines);

        backToHeadlines.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToHeadlines.setEnabled(false);
                pauseReading.toggle();
                pauseReading.setEnabled(false);
                interruptedReading = true;
                t1.stop();
                sentenceNumber = 0;
                readNextHeadline();
            }
        });

        pauseReading = (ToggleButton) findViewById(R.id.pauseReading);

        pauseReading.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) { // pause reading
                    interruptedReading = true;
                    t1.stop();
                } else { // resume
                    readCurrentSentence();
                }
            }
        });


    }

    private class OperationTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (!settings.getBoolean("skipTutorial", false)) {
                t1.speak("Welcome to News Read. Would you like to hear the tutorial?",
                        TextToSpeech.QUEUE_FLUSH, null, "welcome");
            } else {
                readCurrentHeadline();
            }
            return null;
        }
    }

    private void promptSpeechInput(int code) {
        promptSpeechInput(code, "Say something");
    }

    private void promptSpeechInput(int code, String visualPrompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, visualPrompt);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        try {
            startActivityForResult(intent, code);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Device does not support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void readCurrentSentence() {
        String[] sentences = news[articleNumber][1].split("\\.");
        if (sentenceNumber < sentences.length){
            backToHeadlines.setEnabled(true);
            pauseReading.setEnabled(true);
            String sentence = sentences[sentenceNumber];
            t1.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "sentence");
        }
        else {
            t1.speak("Repeat or back to headlines?", TextToSpeech.QUEUE_FLUSH, null, "lastSentence");
        }
    }

    private void readCurrentHeadline() {
        backToHeadlines.setEnabled(false);
        pauseReading.setEnabled(false);
        t1.speak(news[articleNumber][0], TextToSpeech.QUEUE_FLUSH, null, "headline");
    }

    private void readNextHeadline() {
        ++articleNumber;
        if(articleNumber < news.length) {
            readCurrentHeadline();
        } else {
            t1.speak("End of headlines, would you like to hear the headlines again?", TextToSpeech.QUEUE_FLUSH, null, "end");
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: { //reading headlines
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput = result.get(0).toLowerCase();
                    if (voiceInput.equals("yes")) {
                        readCurrentSentence();
                    } else if(result.contains("no")){
                        readNextHeadline();
                    }
                    else if(voiceInput.equals("repeat")) {
                        readCurrentHeadline();
                    }
                    else {
                        t1.speak("I didn't understand your response. Please say yes, no, or repeat.", TextToSpeech.QUEUE_FLUSH, null, "headlineUnclear");
                    }
                }
                break;
            }
            case 101: { // finished reading article
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput = result.get(0).toLowerCase();
                    if (voiceInput.equals("repeat")) {
                        readCurrentSentence();
                    }
                    else if(voiceInput.equals("back to headlines")) {
                        readNextHeadline();
                    }
                    else {
                        t1.speak("I didn't understand your response. Please say repeat or back to headlines", TextToSpeech.QUEUE_FLUSH, null, "noMatch");
                    }
                }
                break;
            }
            case 102: { //yes/no to tutorial
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput = result.get(0).toLowerCase();
                    if (voiceInput.equals("yes")) {
                        t1.speak("News Read will read headlines to you. After each headline you will be asked if you would" +
                                "like to read the article. After an article has been read you have the option to reread the " +
                                "article or to go back to headlines. While an article is being read saying back to headlines" +
                                "will begin reading the remaining headlines. While an article is being read, saying pause will" +
                                "pause the narration. Then saying resume will continue narration, and back to headlines will read the" +
                                "remaining headlines. This ends the tutorial. Would you like to repeat the tutorial?"
                                , TextToSpeech.QUEUE_FLUSH, null, "tutorial");
                    } else if(result.contains("no")) {
                        settings.edit().putBoolean("skipTutorial", true).apply();
                        t1.speak("Proceeding to headlines", TextToSpeech.QUEUE_FLUSH, null, "begin");
                    }
                    else {
                        t1.speak("I didn't understand your response. Please say yes or no.", TextToSpeech.QUEUE_FLUSH, null, "tutorialUnclear");
                    }
                }
                break;
            }
            case 103: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    voiceInput = result.get(0).toLowerCase();
                    if(voiceInput.equals("yes")) {
                        articleNumber = 0;
                        readCurrentHeadline();
                    } else if(result.contains("no")) {
                        // Should we kill the application here?
                        return;
                    } else {
                        t1.speak("I didn't understand your response. Please say yes or no.", TextToSpeech.QUEUE_FLUSH, null, "restartUnclear");
                    }
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        t1.shutdown();
    }
}
