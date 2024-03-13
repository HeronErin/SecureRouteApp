package com.github.heronerin.secureroute.tabs;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.tabs.addPages.AddNoteActivity;
import com.github.heronerin.secureroute.tabs.addPages.GasFillUpFragment;

public class AddFragment extends Fragment {
    private String[] pleaseSelect = new String[]{"No category selected"};
    private String[] addTypes = new String[]{
            "Addition type",
            "Gas fill up",
            "Millage tracking",
            "Expense"
    };
    int currentCategory = 0;

    private Fragment emptyNoteFragment = null;
    private Fragment gasFillFragment = null;
    public void updateSubCategory(){

        Spinner s = (Spinner)getActivity().findViewById(R.id.addSubType);
        String[] arr = pleaseSelect;
        s.setVisibility(View.VISIBLE);

        if (currentCategory == 1 || currentCategory == 0){
            s.setVisibility(View.INVISIBLE);
        }
        if (currentCategory == 0){
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.addFieldHolder, emptyNoteFragment)
                    .commit();
        }
        if (currentCategory == 1){
            if (gasFillFragment == null)
                gasFillFragment = GasFillUpFragment.newInstance();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.addFieldHolder, gasFillFragment)
                    .commit();
        }


        subCategoryAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, arr);
        s.setAdapter(subCategoryAdaptor);



    }
    public AdapterView.OnItemSelectedListener MainCategoryChange = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            currentCategory = position;
            updateSubCategory();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };
    ArrayAdapter<String>  mainCategoryAdaptor;
    ArrayAdapter<String>  subCategoryAdaptor;
    public AddFragment() {}

    public static AddFragment newInstance() {
        AddFragment fragment = new AddFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View add_fragment = inflater.inflate(R.layout.fragment_add, container, false);



        Spinner s = (Spinner)add_fragment.findViewById(R.id.addType);
        s.setOnItemSelectedListener(MainCategoryChange);

        mainCategoryAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, addTypes);
        s.setAdapter(mainCategoryAdaptor);

        add_fragment.findViewById(R.id.addSubType).setVisibility(View.INVISIBLE);

        emptyNoteFragment = AddNoteActivity.newInstance();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.addFieldHolder, emptyNoteFragment)
                .commit();

        return add_fragment;
    }
}