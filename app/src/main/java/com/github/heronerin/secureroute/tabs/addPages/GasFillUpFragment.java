package com.github.heronerin.secureroute.tabs.addPages;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.heronerin.secureroute.R;


public class GasFillUpFragment extends Fragment {
    public GasFillUpFragment() {
        // Required empty public constructor
    }

    public static GasFillUpFragment newInstance() {
        GasFillUpFragment fragment = new GasFillUpFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gas_fill_up, container, false);
    }
}