package com.github.heronerin.secureroute.tabs.addPages;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.events.Event;

public abstract class AbstractAddPage extends Fragment {
    public abstract boolean isValid();
    public abstract Event genValidEvent();

    public abstract void clearStorage();

    public abstract void setMode(int index);

    public abstract String getDisplay();
    public abstract String[] getSubTypes();
}
