package com.github.heronerin.secureroute.tabs.addPages;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.interactions.Event;

import org.json.JSONObject;

public abstract class AbstractAddPage extends Fragment {
    public abstract boolean isValid();
    public abstract Event genValidEvent();

    public abstract void clearStorage();
}
