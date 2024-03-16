package com.github.heronerin.secureroute.tabs;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.interactions.Event;
import com.github.heronerin.secureroute.tabs.addPages.AbstractAddPage;
import com.github.heronerin.secureroute.tabs.addPages.AddNoteActivity;
import com.github.heronerin.secureroute.tabs.addPages.GasFillUpFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddFragment extends Fragment {
    private String[] pleaseSelect = new String[]{"No category selected"};

    private List<AbstractAddPage> addPages = getPages();
    private static List<AbstractAddPage> getPages(){
        return new ArrayList<>(Arrays.asList(
                AddNoteActivity.newInstance(),
                GasFillUpFragment.newInstance(),
                null
        ));
    }
    private String[] addTypes = new String[]{
            "Addition type",
            "Gas fill up",
            "Millage tracking",
            "Expense"
    };
    int currentCategory = 0;

    AbstractAddPage currentPage = null;

    public void updateSubCategory(){

        Spinner s = baseView.findViewById(R.id.addSubType);
        String[] arr = pleaseSelect;
        s.setVisibility(View.VISIBLE);

        if (currentCategory == 1 || currentCategory == 0){
            s.setVisibility(View.INVISIBLE);
        }
        currentPage = addPages.get(currentCategory);
        if (currentPage != null)
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.addFieldHolder, currentPage)
                    .commit();


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
    View baseView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        baseView = inflater.inflate(R.layout.fragment_add, container, false);



        Spinner s = baseView.findViewById(R.id.addType);
        s.setOnItemSelectedListener(MainCategoryChange);

        mainCategoryAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, addTypes);
        s.setAdapter(mainCategoryAdaptor);

        baseView.findViewById(R.id.addSubType).setVisibility(View.INVISIBLE);
        updateSubCategory();

        return baseView;
    }

    public boolean onAddClick() {
        if (currentPage != null){
            if (!currentPage.isValid()){
                Toast.makeText(this.getContext(), "Please enter the required fields!", Toast.LENGTH_LONG).show();
                return false;
            }
            Event event = currentPage.genValidEvent();
            if (event == null){
                Toast.makeText(this.getContext(), "Error getting event from page, can't add to DB", Toast.LENGTH_LONG).show();
                return false;
            }
            DataBase.getOrCreate(this.getContext()).addEvent(event);
            Toast.makeText(this.getContext(), "Added page to DB", Toast.LENGTH_LONG).show();
            currentPage.clearStorage();

            addPages = getPages();
            updateSubCategory();
            return true;

        }
        return false;

    }
}