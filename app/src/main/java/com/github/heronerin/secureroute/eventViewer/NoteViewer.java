package com.github.heronerin.secureroute.eventViewer;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.heronerin.secureroute.DataBase;
import com.github.heronerin.secureroute.ImageViewerFragment;
import com.github.heronerin.secureroute.R;
import com.github.heronerin.secureroute.events.Event;
import com.github.heronerin.secureroute.events.EventArrayAdapter;
import com.github.heronerin.secureroute.tabs.addPages.AbstractAddPage;
import com.github.heronerin.secureroute.tabs.addPages.AddNoteFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class NoteViewer extends AppCompatActivity {
    static class ImageAdaptor extends ArrayAdapter<ImageViewerFragment.ImgTitleCombo> {

        private Context mContext;
        private List<ImageViewerFragment.ImgTitleCombo> imageComboList = new ArrayList<>();

        public ImageAdaptor(@NonNull Context context, List<ImageViewerFragment.ImgTitleCombo> list) {
            super(context, 0 , list);
            mContext = context;
            imageComboList = list;
        }

        @SuppressLint("ResourceType")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.photo_list_item,parent,false);
            ImageViewerFragment.ImgTitleCombo combo = imageComboList.get(position);

            final Uri imageUri = Uri.parse(combo.img);
            ImageView image = (ImageView)listItem.findViewById(R.id.imgPreview);

            Glide.with(mContext)
                    .load(imageUri)
                    .override(256, 256) // Set the desired preview size
                    .into(image);

            final TextView title = (TextView) listItem.findViewById(R.id.ImgName);
            title.setText(combo.title);

            listItem.setOnClickListener((v)->{
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);


                intent.setDataAndType(imageUri, "image/*");
                mContext.startActivity(intent);
            });

            return listItem;
        }
    }
    private static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();

        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
    Event event;


    public static void handleNoteViewAndImgs(Event event, AppCompatActivity activity, Class<?> fragmentCls){
        ((ImageView)activity.findViewById(R.id.eventVirietyPreview)).setImageResource(event.getIcon());

        ((TextView)activity.findViewById(R.id.noteData)).setText(event.noteData);
        List<ImageViewerFragment.ImgTitleCombo> imageComboList = new ArrayList<>();
        ImageAdaptor imageAdaptor = new ImageAdaptor(activity, imageComboList);

        JSONArray jsonImgs = event.getImageData();

        if (jsonImgs.length() == 0)
            activity.findViewById(R.id.imagesTitle).setVisibility(View.GONE);

        try {
            for (int i = 0; i < jsonImgs.length(); i++){
                JSONArray img = jsonImgs.getJSONArray(i);
                imageAdaptor.add(new ImageViewerFragment.ImgTitleCombo(img.getString(0), img.getString(1)));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        ((ListView) activity.findViewById(R.id.imageDisplayHolder)).setAdapter(imageAdaptor);
        if (event.cachedRanges.isEmpty())
            activity.findViewById(R.id.rangesTitle).setVisibility(View.GONE);
        Log.d("NoteView", "Amount of ranges: "+ event.cachedRanges.size());
        EventArrayAdapter eventArrayAdapter = new EventArrayAdapter(activity, event.cachedRanges);
        ((ListView)activity.findViewById(R.id.ranges)).setAdapter(eventArrayAdapter);

        setListViewHeightBasedOnChildren(((ListView)activity.findViewById(R.id.imageDisplayHolder)));

        if (event.cachedRanges.isEmpty())
            activity.findViewById(R.id.rangesTitle).setVisibility(View.GONE);

        activity.findViewById(R.id.eventVirietyPreview).setOnClickListener((v)->{
            try {
                final AbstractAddPage fragment = (AbstractAddPage) fragmentCls.newInstance();

                Bundle bundle = new Bundle();
                bundle.putString("event", event.encodeAsString());
                fragment.setArguments(bundle);

                activity.findViewById(R.id.revisionMenu).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.commitChanges).setVisibility(View.VISIBLE);
                activity.findViewById(R.id.noteViewScroll).setVisibility(View.GONE);
                activity.getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.revisionMenu, fragment)
                        .commit();
                activity.findViewById(R.id.commitChanges).setOnClickListener((vv)->{
                    if (!fragment.isValid()) return;

                    activity.findViewById(R.id.revisionMenu).setVisibility(View.GONE);
                    activity.findViewById(R.id.commitChanges).setVisibility(View.GONE);
                    activity.findViewById(R.id.noteViewScroll).setVisibility(View.VISIBLE);

                    DataBase.getOrCreate(activity).updateEvent(fragment.genValidEvent());
                    activity.finish();
                });
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }

        });
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_viewer);

        EdgeToEdge.enable(this);


        Bundle extras = getIntent().getExtras();
        assert extras != null;

        event = Event.decodeFromString(extras.getString("event"));
        handleNoteViewAndImgs(event, this, AddNoteFragment.class);


    }
}