package com.github.heronerin.secureroute;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerFragment extends Fragment {
    public static Uri swapUriScheme(Uri uri, String newScheme) {
        if (uri == null || newScheme == null) {
            return uri;
        }

        String oldScheme = uri.getScheme();
        if (oldScheme == null || oldScheme.equals(newScheme)) {
            return uri;
        }

        String uriString = uri.toString();
        return Uri.parse(uriString.replaceFirst(oldScheme + ":", newScheme + ":"));
    }

    public static class ImgTitleCombo{
        public String img;
        public String title;

        public ImgTitleCombo(String url, String title) {
            this.img=url;
            this.title=title;
        }
    }
    static class ImageAdaptor extends ArrayAdapter<ImgTitleCombo> {

        private Context mContext;
        private List<ImgTitleCombo> imageComboList = new ArrayList<>();
        ImageViewerFragment imageViewerFragment;

        public ImageAdaptor(@NonNull Context context, List<ImgTitleCombo> list, ImageViewerFragment _imageViewerFragment) {
            super(context, 0 , list);
            imageViewerFragment = _imageViewerFragment;
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
            final ImgTitleCombo current = imageComboList.get(position);

            Uri imageUri = Uri.parse(current.img);
            ImageView image = (ImageView)listItem.findViewById(R.id.imgPreview);

            Glide.with(mContext)
                    .load(imageUri)
                    .override(256, 256) // Set the desired preview size
                    .into(image);

            final TextView title = (TextView) listItem.findViewById(R.id.ImgName);
            title.setText(current.title);
            final Context c = this.getContext();
            listItem.setOnClickListener((view)->{
                PopupMenu popup = new PopupMenu(this.getContext(), view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.drawable.photo_menu, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.view){
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);


                        intent.setDataAndType(imageUri, "image/*");
                        c.startActivity(intent);
                    }
                    if (item.getItemId() == R.id.rename){
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle("Enter image title");
                        final EditText input = new EditText(c);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        input.setText(current.title);
                        builder.setView(input);

                        builder.setNegativeButton("Cancel",(dialog, which)->{
                            dialog.cancel();
                        });
                        builder.setPositiveButton("Rename", (dialog, which)->{
                            current.title = input.getText().toString();
                            title.setText(current.title);
                            try {
                                for (int i = 0; i < imageViewerFragment.imgs.length(); i++){
                                    JSONArray jsonArray = imageViewerFragment.imgs.getJSONArray(i);
                                    if (!jsonArray.getString(0).equals(current.img)) continue;
                                    jsonArray.put(1, current.title);
                                    imageViewerFragment.imgs.put(i, jsonArray);
                                    break;
                                }


                                imageViewerFragment.saveJson();
                            } catch (JSONException | IOException ignored) { }
                        });
                        builder.show();

                    }
                    if (item.getItemId() == R.id.delete){
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle("Are you sure you wish to remove this image?");
                        builder.setNegativeButton("no", ((dialog, which) -> dialog.cancel()));
                        builder.setPositiveButton("yes", ((dialog, which) -> {
                            c.getContentResolver().delete(Uri.parse(current.img), null, null);
                            for (int i = 0; i < imageViewerFragment.imgs.length(); i++){
                                try {
                                    JSONArray jsonArray = imageViewerFragment.imgs.getJSONArray(i);
                                    if (!jsonArray.getString(0).equals(current.img)) continue;

                                    imageViewerFragment.imgs.remove(i);
                                    this.remove(current);
                                    imageViewerFragment.saveJson();
                                    break;

                                } catch (JSONException | IOException ignored) { }


                            }
                        }));
                        builder.show();




                    }
                    return true;
                });
            });

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
    public Uri lastPhotoUri;

    public static final int PICK_IMAGE = 1;
    public static final int TAKE_IMAGE = 2;

    void imageFromGall(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }
    public static boolean renameImageTitle(ContentResolver contentResolver, Uri imageUri, String newTitle) {
        // Create ContentValues object with the updated title
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, newTitle);

        // Update the metadata of the image in the content resolver
        int rowsAffected = contentResolver.update(imageUri, values, null, null);
        return rowsAffected > 0;
    }
    void imageFromCam(View view){
//        File file = new File(path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Generate a new content URI for the captured image
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Taken from camera at: "+ System.currentTimeMillis());
        lastPhotoUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.e("WEEWOO", lastPhotoUri.toString());
        // Continue only if the Insert was successful
        if (lastPhotoUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, TAKE_IMAGE);
        } else {
            Toast.makeText(getContext(), "Failed to create a new image URI", Toast.LENGTH_SHORT).show();
        }
    }

    void saveJson() throws JSONException, IOException {
        objectToSave.put("imgs", imgs);
        byte[] bytes = objectToSave.toString().getBytes(StandardCharsets.UTF_8);
        File file = new File(saveTo);
        Log.d("SVV", saveTo.toString());
        if (!file.exists()) file.createNewFile();

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d("R1", String.valueOf(requestCode));
        Log.d("R2", String.valueOf(resultCode));
        if (requestCode != TAKE_IMAGE && requestCode != PICK_IMAGE) return;
        Log.d("R", String.valueOf(resultCode));
        if (resultCode != -1) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Enter image title");


        final EditText input = new EditText(this.getContext());

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        final Context c = this.getContext();
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (requestCode == PICK_IMAGE){

                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.TITLE, input.getText().toString());
                values.put(MediaStore.Images.Media.DESCRIPTION, "Taken from gallery at: "+ System.currentTimeMillis());
                lastPhotoUri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try(InputStream is = new BufferedInputStream(getContext().getContentResolver().openInputStream(data.getData()))){
                    try(OutputStream os = new BufferedOutputStream(getContext().getContentResolver().openOutputStream(lastPhotoUri))) {
                        int byteRead;
                        while ((byteRead = is.read()) != -1) {
                            os.write(byteRead);
                        }
                        os.flush();
                    }


                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(lastPhotoUri.toString());
                jsonArray.put(input.getText().toString());


                imageAdaptor.add(new ImgTitleCombo(lastPhotoUri.toString(), input.getText().toString()));
                imgs.put(jsonArray);


            }
            if (requestCode == TAKE_IMAGE){
                JSONArray jsonArray = new JSONArray();

                jsonArray.put(lastPhotoUri);
                jsonArray.put(input.getText().toString());
                imgs.put(jsonArray);

//                renameImageTitle(getContext().getContentResolver(), lastPhotoUri, input.getText().toString());

                imageAdaptor.add(new ImgTitleCombo(lastPhotoUri.toString(), input.getText().toString()));
            }
            try {
                saveJson();
            } catch (JSONException | IOException e) {
                throw new RuntimeException(e);
            }
        });
        builder.show();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        objectToSave = new JSONObject();

        imageAdaptor = new ImageAdaptor(this.getContext(), new ArrayList<>(), this);

        imgs = new JSONArray();

        try {
            if (getArguments() != null) {
                saveTo = getArguments().getString("saveTo");
                if (saveTo.startsWith("file:")) saveTo=saveTo.substring("file:".length());
                if (getArguments().getBoolean("doLoad")){
                    StringBuilder sb = new StringBuilder();

                    try (InputStream inputStream = new BufferedInputStream(new FileInputStream(saveTo))) {

                        int b;
                        while (-1 != (b = inputStream.read())) {
                            sb.append((char) b);
                        }
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                        return;
                    }
                    objectToSave = new JSONObject(sb.toString());
                    imgs = objectToSave.getJSONArray("imgs");
                    for (int i = 0; i < imgs.length(); i++){
                        JSONArray img = imgs.getJSONArray(i);
                        imageAdaptor.add(new ImgTitleCombo(img.getString(0), img.getString(1)));

                    }
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
        View v =  inflater.inflate(R.layout.fragment_image_viewer, container, false);
        ((ListView)v.findViewById(R.id.imgList)).setAdapter(imageAdaptor);
        v.findViewById(R.id.addImgFromCam).setOnClickListener(this::imageFromCam);
        v.findViewById(R.id.addImgFromGall).setOnClickListener(this::imageFromGall);
        v.findViewById(R.id.BackImgBtn).setOnClickListener((view)->{
            CameraManager.instance.handleExit();
            getActivity().getFragmentManager().popBackStack();
            }
        );


        return v;
    }

}