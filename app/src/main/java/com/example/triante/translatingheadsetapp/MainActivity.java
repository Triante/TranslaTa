package com.example.triante.translatingheadsetapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

/* Main activity. Home to the UI for accessing the buttons for speech recognition, translation, and speech synthesis*/
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public Button bSpeechRecognition, bSpeechSynthesis, bTranslate, bTest, bSettings; //Main UI buttons
    private IBMSpeechToText stt; //Speech-to-text model 
    private IBMTextToSpeech tts; //Text-to-speech model
    public TextView editTextField;
    public TextView translatedTextView; //Shows translated text
    boolean isInRecording = false; //flag for checking if the system is currently recording speech
    MSTranslator translator; //Translator model
    Language language; //Language model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        /* Create view for activity*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        /* Attempt to initialize Translator model*/
        try {
            translator = new MSTranslator();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        /* Initialize IBM speech-to-text and text-to-speech*/
        stt = new IBMSpeechToText(this);
        tts = new IBMTextToSpeech(this);

        /* Initialize buttons*/
        bSpeechRecognition = (Button) findViewById(R.id.bSpeechRecognition);
        bSpeechSynthesis = (Button) findViewById(R.id.bSpeechSynthesis);
        bTranslate = (Button) findViewById(R.id.bTranslate);
        bSettings = (Button) findViewById(R.id.bOptions);
        editTextField = (TextView) findViewById(R.id.editTextDemo);
        translatedTextView = (TextView) findViewById(R.id.translatedTextView);
        editTextField.setClickable(false);
        
        /* Declare all the on click listeners for the buttons*/
        bSpeechRecognition.setOnClickListener(this);
        bSpeechSynthesis.setOnClickListener(this);
        bTranslate.setOnClickListener(this);
        bSettings.setOnClickListener(this);
        bTest = (Button) findViewById(R.id.bTest);
        bTest.setOnClickListener(this);
    }

    /* Method to manage all the on click listeners for the buttons*/
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bSpeechRecognition: //Speech Recognition button listener
                
                /* Checks if the system is currently recording speech */
                if (isInRecording) { 
                    
                    /* New task created to end the recording session */
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... none) {
                            try {
                                stt.end();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                    }.execute();
                    
                    /* New task created to switch the state of the button from "Stop Recording" to "Start Recording" */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            bSpeechRecognition.setText("Start Recording");
                            isInRecording = false;
                        }
                    });
                }               
                /* If the system is currently not recording */
                else {
                    stt.record(); //Start recording
                    
                    /* New task created to change button state from "Start Recording" to "Stop Recording" */
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run() {
                            bSpeechRecognition.setText("Stop Recording");
                            isInRecording = true;
                        }
                    });
                }
                break;
            case R.id.bSpeechSynthesis: //Speech synthesis button listener
                tts.synthesize(translatedTextView.getText().toString(), ""); //Performs speech synthesis on IBMTextToSpeech
                break;
            case R.id.bTranslate: //Translator button listener
                final String input = translatedTextView.getText().toString(); //Gets contents of the translated text view
                
                /* New taks created to perform the translation*/
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... none) {
                        try {
                            String myCode = Language.getMyLanguageCode(); //User's preferred language model
                            String RespCode = Language.getResponseLanguageCode(); //Other party's preferred language model
                            final String output = translator.translate(input, myCode, RespCode); //translated text
                            
                            /* New task created to set translated text to the translated text view*/
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    translatedTextView.setText(output);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                }.execute();

                break;
            case R.id.bTest: //Testing method (not yet finished)
                //language.getSupportedSpeech();
                break;
            case R.id.bOptions: //Settings button listener
                Intent options = new Intent(this, SettingsActivity.class); 
                startActivity(options); //Starts new settings activity
                break;
        }
    }
}
