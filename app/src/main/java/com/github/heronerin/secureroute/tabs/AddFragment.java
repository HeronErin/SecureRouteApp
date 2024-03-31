package com.github.heronerin.secureroute.tabs;

import static com.github.heronerin.secureroute.MainActivity.updateNotification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.TripUtils;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.tabs.addPages.AbstractAddPage;
import com.github.heronerin.secureroute.tabs.addPages.AddIncomeFragment;
import com.github.heronerin.secureroute.tabs.addPages.AddMillageTrackingFragment;
import com.github.heronerin.secureroute.tabs.addPages.AddNoteFragment;
import com.github.heronerin.secureroute.tabs.addPages.AddTripFragment;
import com.github.heronerin.secureroute.tabs.addPages.GasFillUpFragment;
import com.github.heronerin.secureroute.tabs.addPages.RangeEventAddFragment;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddFragment extends Fragment {
//    private String[] pleaseSelect = new String[]{"No category selected"};
//    private String[] rangeSubCategories = new String[]{"Start", "End"};

    private List<AbstractAddPage> addPages = getPages();
    private static List<AbstractAddPage> getPages(){
        return new ArrayList<>(Arrays.asList(
                AddNoteFragment.newInstance(),
                AddIncomeFragment.newInstance(),
                GasFillUpFragment.newInstance(),
                AddTripFragment.newInstance(),
                AddMillageTrackingFragment.newInstance(),
                RangeEventAddFragment.newInstance()
        ));
    }

    int currentCategory = 0;

    AbstractAddPage currentPage = null;

    public void updateSubCategory(){

        Spinner s = baseView.findViewById(R.id.addSubType);
//        String[] arr = pleaseSelect;
        s.setVisibility(View.VISIBLE);

        currentPage = addPages.get(currentCategory);

        String[] sub = currentPage.getSubTypes();
        if (sub == null)
            s.setVisibility(View.GONE);

//        if (currentCategory == 4)
//            arr = rangeSubCategories;

        subCategoryAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, sub != null ? sub : new String[0] );
        s.setAdapter(subCategoryAdaptor);


        if (currentPage != null) {
            if (currentPage.getView() == null) addPages = getPages();

            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.addFieldHolder, currentPage)
                    .commit();
            currentPage.setMode(s.getSelectedItemPosition() );
        }






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

        String[] addTypes = new String[addPages.size()];
        for (int i = 0; i < addPages.size(); i++)
            addTypes[i] = addPages.get(i).getDisplay();


        mainCategoryAdaptor = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, addTypes);
        s.setAdapter(mainCategoryAdaptor);


        baseView.findViewById(R.id.addSubType).setVisibility(View.GONE);


        ((Spinner)baseView.findViewById(R.id.addSubType)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (AddFragment.this.currentPage != null)
                    AddFragment.this.currentPage.setMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

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
            if (event.variety == Event.EventVariety.GasEvent){
                Event e = DataBase.getOrCreate(this.getContext()).getLastGas();
                if (e != null){
                    DataBase.getOrCreate(this.getContext()).addEvent(new Event(
                            Event.EventVariety.GasEventEnd,
                            UUID.randomUUID(),
                            event.timeStamp - 1,
                            0,
                            e.databaseId,
                            "End gas",
                            new JSONArray(),
                            event.odometer
                    ));
                }
            }

            DataBase.getOrCreate(this.getContext()).addEvent(event);

            updateNotification(this.getContext());

            currentPage.clearStorage();

            TripUtils.setLastUpdate(getContext());
            return true;

        }
        return false;

    }
}