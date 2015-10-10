package edu.vanderbilt.newsread;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextToSpeech t1;
    Button b1;
    TextView userInput;
    Button startListening;

    // Stores headlines and full articles. Articles are in the second dimension.
    String[][] news = new String [][] {
            {"Surfer loses left leg after shark attack in Hawaii",
            "Etsy.com shares unravel more after Amazon launches rival",
            "Twitter to have major layoffs next week, report says"},
            {"A 25-year-old Oahu man lost his left leg to a shark attack Friday while surfing off Oahu's North Shore, according to local media reports." +
                    "Colin Cook was straddling his board with both legs dangling in the water when the 10- to 12-foot shark latched onto his lower leg, media reports said. He punched the animal until it let go, screamed for help and was assisted to shore by another surfer and a kayaker, his family told reporters." +
                    "Rescuers used a surfboard leach as a tourniquet on Cook's leg until paramedics arrived. Doctors at Queens Medical Center in Honolulu later completed the amputation on his leg, Chris Webster, Cook's cousin, told KHON 2, a Hawaii television station." +
                    "During the attack, the shark got him below the knee ... but they had to take his leg above the knee so that was a little heart-breaking, Webster told the television station. Cook's left hand was also injured." +
                    "Cook is a native of Rhode Island who moved to Hawaii three years ago and learned to create custom surfboards, according to Hawaii News Now." +
                    "The attacked occurred about 10:30 a.m. near a popular surfing area known as Leftovers Beach Park. It was the fifth shark attack in Hawaii this year." +
                    "Lifeguards in the area were warning visitors to stay out of the ocean, and signs have been posted along the beach, according to the Associated Press.",
            "Shares of Etsy, the handmade marketplace, slid further Friday after sinking 4% Thursday when Amazon announced a competing marketplace called Handmade at Amazon." +
                    "The stock lost 1.3% by Friday mid-day, to $13.40 per share. That compared with a high of $14.68 on Wednesday before Amazon make its announcement."+
                    "Handmade at Amazon will feature more than 80,000 items, from all 50 states and 60 countries to start, said Peter Faricy, vice president for Amazon Marketplace." +
                    "In response to Amazon’s foray into the hand-crafted market, Etsy’s CEO Chad Dickerson released the following statement:" +
                    "We believe we are the best platform for creative entrepreneurs, empowering them to succeed on their own terms. Etsy has a decade of experience understanding the needs of artists and sellers and supporting them in ways that no other marketplace can. Our platform attracts 21+ million thoughtful consumers seeking to discover unique goods, and build relationships with the people who make and sell them.",
            "Twitter is planning layoffs next week, according to technology news outlet Re/code." +
                    "The cuts will be made company wide, Re/code said." +
                    "We're not commenting on rumor and speculation, Twitter spokesman Jim Prosser said." +
                    "Twitter shares fell 3% in after-hours trading. They closed up about 2% to $30.85 on Friday." +
                    "Twitter had 4,200 employees at the end of June. That's more than double the 2,000 employees it had in the second quarter of 2013. Yet its user growth has not kept pace." +
                    "The layoffs would come one week after Twitter co-founder Jack Dorsey was appointed CEO. He has been serving as interim CEO since July 1." +
                    "Twitter has also apparently shelved plans to expand into a building on Market Street in San Francisco that is home to Uber and Square." +
                    "According to the San Francisco Business Times, Twitter was close to finalizing the deal to take about 100,000 square feet before abandoning the deal."}
        };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    t1.setLanguage(Locale.US);
                }
            }
        });

        b1 = (Button)findViewById(R.id.button);
        startListening = (Button) findViewById(R.id.button);
        startListening.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SpeakInBackground().execute("The quick brown fox jumps over the lazy dog", "Hi Collin, you are the bees knees.");
                promptSpeechInput();
            }
        });

        userInput = (TextView) findViewById(R.id.speechInput);


    }

    private class SpeakInBackground extends AsyncTask<String, Void, Void>{


        @Override
        protected Void doInBackground(String... stringsToSpeak) {
            for(String s : stringsToSpeak) {
                t1.speak(s, TextToSpeech.QUEUE_FLUSH, null, "init");
                while (t1.isSpeaking()) {
                    //do nothing
                }
            }
            return null;
        }
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Say something");
        try {
            startActivityForResult(intent, 100);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Device does not support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 100: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    userInput.setText(result.get(0));
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
