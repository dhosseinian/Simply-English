package com.c0xif.simplyenglish;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;


public class SpeechToTextFragment extends Fragment {
    WebView wv;

    //public static SpeechToTextFragment newInstance() {
    //    return new SpeechToTextFragment();
    //}

    public SpeechToTextFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_speech_to_text, container, false);
        //Log.d("s2tf", "Did I die here?");
        wv = (WebView)v.findViewById(R.id.speech2textBox);
        //Log.d("s2tf", "How about here?");
        return v;
    }

    public boolean updateText(String s) {

        wv.loadUrl("about:blank");
        wv.loadData(s, "text/html", null);
        return true;
    }
}

