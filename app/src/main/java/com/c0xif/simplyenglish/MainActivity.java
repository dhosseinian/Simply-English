package com.c0xif.simplyenglish;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

import android.view.View;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements RecognitionListener{

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String NGRAM_SEARCH = "recog";

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;

    SpeechToTextFragment s2tfrag;
    TextToSimpleFragment t2sfrag;
    EditText t2tbox;

    StringBuilder actual;
    private CDrawer.CDrawThread mDrawThread;
    private CDrawer mdrawer;

    private View.OnClickListener listener;
    private Boolean m_bStart = Boolean.valueOf(false);
    private Boolean recording;
    private CSampler sampler;

    private boolean visualizerOn = true;
    private Button switch1;
    private boolean speech = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //s2tfrag.addText("Preparing listener.");
        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        runRecognizerSetup();

        actual = new StringBuilder("");

        final FragmentManager fm = getSupportFragmentManager();

        //getSupportFragmentManager().beginTransaction().replace(R.id.s2tFrag, new SpeechToTextFragment(),"s2tFrag").commit();
        s2tfrag = ((SpeechToTextFragment) fm.findFragmentById(R.id.s2tFrag));
        //getSupportFragmentManager().beginTransaction().replace(R.id.t2sFrag, new TextToSimpleFragment(),"t2sFrag").commit();
        t2sfrag = ((TextToSimpleFragment) fm.findFragmentById(R.id.t2sFrag));

        //t2tfrag = ((TextToTextFragment) fm.findFragmentById(R.id.t2tFrag));

        /*
        fm.beginTransaction()
                .hide(s2tfrag)
                .commit();
        fm.beginTransaction()
                .hide(t2sfrag)
                .commit();
                */
        t2tbox = (EditText) findViewById(R.id.editText);
        final Button submit = (Button) findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                t2sfrag.clear();
                processWords(t2tbox.getText().toString());
            }
        });

        switch1 = (Button) findViewById(R.id.switch1);
        Log.d("SwitchButton", switch1.toString());
        if (speech) {
            switch1.setText("Speech");
        } else switch1.setText("Text");

        switch1.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("SwitchButton", "Button clicked");

                if(!speech) {
                    Log.d("SwitchButton", "Text clicked");
                    switch1.setText("Speech");

                    clear();

                    submit.setVisibility(View.GONE);
                    t2tbox.setVisibility(View.GONE);

                    fm.beginTransaction()
                            .show(s2tfrag)
                            .commit();



                    //mdrawer.setVisibility(View.VISIBLE);
                    speech = true;

                    recognizer.startListening(NGRAM_SEARCH);
                } else {
                    Log.d("SwitchButton", "Speech clicked");
                    switch1.setText("Text");

                    clear();

                    fm.beginTransaction()
                            .hide(s2tfrag)
                            .commit();

                    submit.setVisibility(View.VISIBLE);
                    t2tbox.setVisibility(View.VISIBLE);

                    //mdrawer.setVisibility(View.GONE);
                    speech = false;

                    recognizer.stop();
                }

            }
        });

        Button clearer = (Button) findViewById(R.id.clear);
        clearer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("ClearButton", "Clearing");
                clear();
            }
        });

        Button clearhist = (Button) findViewById(R.id.clearhist);
        clearhist.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                clearHistory();
            }
        });

        if (visualizerOn) {
            //super.onCreate (savedInstanceState);
            //setContentView(R.layout.activity_main);
            mdrawer = (CDrawer) findViewById(R.id.drawer);
            m_bStart = Boolean.valueOf(false);


            while (true)
            {
                recording = Boolean.valueOf(false);
                run();
                System.out.println("mDrawThread NOT NULL");
                System.out.println("recorder NOT NULL");
                return;
            }
        }

    }

    private void runRecognizerSetup() {
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(MainActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    makeText(getApplicationContext(), "Listener died", Toast.LENGTH_SHORT).show();
                } else {
                    makeText(getApplicationContext(), "Listener is on!", Toast.LENGTH_SHORT).show();
                   // s2tfrag.addText("Listener is on");
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                runRecognizerSetup();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.cancel();
            recognizer.shutdown();
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        //if (hypothesis == null)
        //    return;

        //String text = hypothesis.getHypstr();
        //((TextView) findViewById(R.id.caption_text)).setText(text);
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (speech) {
            if (hypothesis != null) {
                String text = hypothesis.getHypstr();
                //makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                //actual.append(text + ". ");
                processWords(text);
            }
        }
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
        recognizer.stop();
        recognizer.startListening(NGRAM_SEARCH);
    }

    /*
    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.caption_text)).setText(caption);
    }
    */

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)

                .getRecognizer();
        recognizer.addListener(this);

        recognizer.addNgramSearch(NGRAM_SEARCH, new File(assetsDir, "en-70k-0.2-pruned.lm.bin"));
        recognizer.startListening(NGRAM_SEARCH);

    }

    @Override
    public void onError(Exception error) {
        //((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }


    @Override
    public void onTimeout() {
        //switchSearch(KWS_SEARCH);
    }

    public void processWords(String s) {
        Log.d("MainActivity","processing stuffz");
        String[] words = s.split(" ");
        //if in s2t state
        for (String word : words) {
            actual.append(" " + word);
            //Log.d("MainActivity", actual.toString());
            s2tfrag.updateText("<span style=\"background-color:#f9f03b;\">" + actual.toString() + "</span>");
            t2sfrag.receiveWord(word);
        }
        //if in t2t state
        actual.append(". ");
    }

    public void clear() {
        actual.setLength(0);
        s2tfrag.updateText("");
        t2sfrag.clear();
        t2tbox.setText("");
    }

    public void clearHistory(){
        new AlertDialog.Builder(this)
                .setTitle("Clear history")
                .setMessage("Are you sure you want to clear your history? This action will reset all data used for personalized translations.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        t2sfrag.clearHist();
                        clear();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }



    /**
     * Pause the visualizer when the app is paused
     */
    @Override
    protected void onPause()
    {
        /*
        if (visualizerOn) {
            System.out.println("onpause");
            sampler.SetRun(Boolean.valueOf(false));
            mDrawThread.setRun(Boolean.valueOf(false));
            sampler.SetSleeping(Boolean.valueOf(true));
            mDrawThread.SetSleeping(Boolean.valueOf(true));
            Boolean.valueOf(false);
        }
        */
        super.onPause();

    }
    /**
     * Resters the visualizer when the app restarts
     */
    @Override
    protected void onRestart()
    {
        /*
        if (visualizerOn) {
            m_bStart = Boolean.valueOf(true);
            System.out.println("onRestart");
        }
        */
        super.onRestart();
    }
    /**
     * Resume the visualizer when the app resumes
     */
    /*
    @Override
    protected void onResume()
    {
        if (visualizerOn) {
            System.out.println("onresume");
            int i = 0;
            while (true) {
                if ((sampler.GetDead2().booleanValue()) && (mdrawer.GetDead2().booleanValue())) {
                    System.out.println(sampler.GetDead2() + ", " + mdrawer.GetDead2());
                    sampler.Restart();
                    if (!m_bStart.booleanValue())
                        mdrawer.Restart(Boolean.valueOf(true));
                    sampler.SetSleeping(Boolean.valueOf(false));
                    mDrawThread.SetSleeping(Boolean.valueOf(false));
                    m_bStart = Boolean.valueOf(false);
                    super.onResume();
                    return;
                }
                try {
                    Thread.sleep(500L);
                    System.out.println("Hang on..");
                    i++;
                    if (!sampler.GetDead2().booleanValue())
                        System.out.println("sampler not DEAD!!!");
                    if (!mdrawer.GetDead2().booleanValue()) {
                        System.out.println("mDrawer not DeAD!!");
                        mdrawer.SetRun(Boolean.valueOf(false));
                    }
                    if (i <= 4)
                        continue;
                    mDrawThread.SetDead2(Boolean.valueOf(true));
                } catch (InterruptedException localInterruptedException) {
                    localInterruptedException.printStackTrace();
                }
            }
        } else {
            super.onResume();
        }
    }
    */

    @Override
    protected void onStart()
    {
        System.out.println("onstart");
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        System.out.println("onstop");
        super.onStop();
    }


    /**
     * Recives the buffert from the sampler
     * @param buffert
     */
    public void setBuffer(short[] paramArrayOfShort)
    {
        mDrawThread = mdrawer.getThread();
        mDrawThread.setBuffer(paramArrayOfShort);
    }

    /**
     * Called by OnCreate to get everything up and running
     */
    public void run()
    {
        if (visualizerOn) {
            try {
                if (mDrawThread == null) {
                    mDrawThread = mdrawer.getThread();
                }
                if (sampler == null)
                    sampler = new CSampler(this);
                //Context localContext = getApplicationContext();
                //Display localDisplay = getWindowManager().getDefaultDisplay();
                //Toast localToast = Toast.makeText(localContext, "Please make some noise..", Toast.LENGTH_LONG);
                //localToast.setGravity(48, 0, localDisplay.getHeight() / 8);
                //localToast.show();
                mdrawer.setOnClickListener(listener);
                if (sampler != null) {
                    sampler.Init();
                    sampler.StartRecording();
                    sampler.StartSampling();
                }
            } catch (NullPointerException e) {
                Log.e("Main_Run", "NullPointer: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
