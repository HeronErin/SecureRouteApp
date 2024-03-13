package com.github.heronerin.secureroute;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerFragment extends Fragment {
    static class ImgTitleCombo{
        String img;
        String title;

        public ImgTitleCombo(String url, String title) {
            this.img=url;
            this.title=title;
        }
    }
    static class ImageAdaptor extends ArrayAdapter<ImgTitleCombo> {

        private Context mContext;
        private List<ImgTitleCombo> moviesList = new ArrayList<>();

        public ImageAdaptor(@NonNull Context context, ArrayList<ImgTitleCombo> list) {
            super(context, 0 , list);
            mContext = context;
            moviesList = list;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.photo_list_item,parent,false);

            ImgTitleCombo current = moviesList.get(position);


            ImageView image = (ImageView)listItem.findViewById(R.id.imgPreview);
            image.setImageURI(Uri.parse(current.img));

            TextView title = (TextView) listItem.findViewById(R.id.ImgName);
            title.setText(current.title);

            return listItem;
        }
    }
    public ImageViewerFragment() {
        // Required empty public constructor
    }

    public static ImageViewerFragment newInstance(String saveToUri, boolean doLoad) {
        ImageViewerFragment fragment = new ImageViewerFragment();
        Bundle args = new Bundle();
        args.putString("saveTo", saveToUri);
        args.putBoolean("doLoad", doLoad);
        fragment.setArguments(args);
        return fragment;

    }
    String saveTo;
    JSONObject objectToSave;
    JSONArray imgs;
    ImageAdaptor imageAdaptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        objectToSave = new JSONObject();

        imageAdaptor = new ImageAdaptor(this.getContext(), new ArrayList<>());
        imageAdaptor.add(new ImgTitleCombo("https://www.google.com/images/branding/googlelogo/1x/googlelogo_light_color_272x92dp.png", "WeeWooo"));

        try {
            if (getArguments() != null) {
                saveTo = getArguments().getString("saveTo");
                if (getArguments().getBoolean("doLoad")){
                    StringBuilder sb = new StringBuilder();

                    try (FileInputStream inputStream = new FileInputStream(saveTo)) {

                        int b;
                        while (-1 != (b = inputStream.read())) {
                            sb.append((char) b);
                        }
                    }
                    objectToSave = new JSONObject(sb.toString());
                    imgs = objectToSave.getJSONArray("imgs");
                }
    //            mParam2 = getArguments().getString(ARG_PARAM2);
            }else{
                Toast.makeText(this.getContext(), "Error with image viewer", Toast.LENGTH_LONG).show();
                throw new RuntimeException("No img viewer uri");
            }
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_image_viewer, container, false);
        ((ListView)v.findViewById(R.id.imgList)).setAdapter(imageAdaptor);
        return v;
    }
}