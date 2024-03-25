package com.github.heronerin.secureroute;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageManager extends AppCompatActivity {
    public static class ImgTitleCombo{
        public String img;
        public String title;

        public ImgTitleCombo(String url, String title) {
            this.img=url;
            this.title=title;
        }
    }
    static class ImageAdaptor extends ArrayAdapter<ImgTitleCombo> {


        private ImageManager imageManager;
        private List<ImgTitleCombo> imageComboList = new ArrayList<>();
        public ImageAdaptor(List<ImgTitleCombo> list, ImageManager imageManager) {
            super(imageManager, 0 , list);
            imageComboList = list;
            this.imageManager = imageManager;
        }

        @SuppressLint("ResourceType")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null)
                listItem = LayoutInflater.from(imageManager).inflate(R.layout.photo_list_item,parent,false);
            final ImgTitleCombo current = imageComboList.get(position);

            Uri imageUri = Uri.parse(current.img);
            ImageView image = listItem.findViewById(R.id.imgPreview);

            Glide.with(imageManager)
                    .load(imageUri)
                    .override(256, 256) // Set the desired preview size
                    .into(image);

            final TextView title = listItem.findViewById(R.id.ImgName);
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
                                for (int i = 0; i < imageManager.imgs.length(); i++){
                                    JSONArray jsonArray = imageManager.imgs.getJSONArray(i);
                                    if (!jsonArray.getString(0).equals(current.img)) continue;
                                    jsonArray.put(1, current.title);
                                    imageManager.imgs.put(i, jsonArray);
                                    break;
                                }
                            } catch (JSONException ignored) { }
                        });
                        builder.show();

                    }
                    if (item.getItemId() == R.id.delete){
                        AlertDialog.Builder builder = new AlertDialog.Builder(c);
                        builder.setTitle("Are you sure you wish to remove this image?");
                        builder.setNegativeButton("no", ((dialog, which) -> dialog.cancel()));
                        builder.setPositiveButton("yes", ((dialog, which) -> {
                            c.getContentResolver().delete(Uri.parse(current.img), null, null);
                            for (int i = 0; i < imageManager.imgs.length(); i++){
                                try {
                                    JSONArray jsonArray = imageManager.imgs.getJSONArray(i);
                                    if (!jsonArray.getString(0).equals(current.img)) continue;

                                    imageManager.imgs.remove(i);
                                    this.remove(current);
                                    break;

                                } catch (JSONException ignored) { }


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
    void imageFromCam(View view){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Taken from camera at: "+ System.currentTimeMillis());
        lastPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Log.e("WEEWOO", lastPhotoUri.toString());
        // Continue only if the Insert was successful
        if (lastPhotoUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, lastPhotoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(intent, TAKE_IMAGE);
        } else {
            Toast.makeText(this, "Failed to create a new image URI", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != TAKE_IMAGE && requestCode != PICK_IMAGE) return;
        if (resultCode != RESULT_OK) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter image title");

        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", (dialog, which) -> {
            if (requestCode == PICK_IMAGE) {
                ContentValues values = new ContentValues();

                values.put(MediaStore.Images.Media.TITLE, input.getText().toString());
                values.put(MediaStore.Images.Media.DESCRIPTION, "Taken from gallery at: " + System.currentTimeMillis());
                lastPhotoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                try (InputStream is = new BufferedInputStream(getContentResolver().openInputStream(data.getData()))) {
                    try (OutputStream os = new BufferedOutputStream(getContentResolver().openOutputStream(lastPhotoUri))) {
                        byte[] bytes = new byte[1024];
                        while (-1 != is.read(bytes)) {
                            os.write(bytes);
                        }
                        os.flush();
                    }


                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                JSONArray jsonArray = new JSONArray();
                jsonArray.put(lastPhotoUri.toString());
                jsonArray.put(input.getText().toString());


                imageAdaptor.add(new ImgTitleCombo(lastPhotoUri.toString(), input.getText().toString()));
                imgs.put(jsonArray);
                Log.d("imageManager", "Adding image to image manager");

            }
            if (requestCode == TAKE_IMAGE) {
                JSONArray jsonArray = new JSONArray();

                jsonArray.put(lastPhotoUri);
                jsonArray.put(input.getText().toString());
                imgs.put(jsonArray);

                imageAdaptor.add(new ImgTitleCombo(lastPhotoUri.toString(), input.getText().toString()));
                Log.d("imageManager", "Adding image to image manager");
            }
        });
        builder.show();
    }


    JSONArray imgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.fragment_image_viewer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageAdaptor = new ImageAdaptor(new ArrayList<>(), this);

        Log.d("imageManager", "creating new image manager activity");

        String inputJson = getIntent().getStringExtra("json");
        Log.d("imageManager", inputJson);
        imgs = new JSONArray();
        if (inputJson != null) {
            try {
                imgs=new JSONArray(inputJson);
                for (int i = 0; i < imgs.length(); i++){
                    JSONArray image = imgs.getJSONArray(i);
                    imageAdaptor.add(new ImgTitleCombo(image.getString(0), image.getString(1)));
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }


        ((ListView)findViewById(R.id.imgList)).setAdapter(imageAdaptor);

        findViewById(R.id.addImgFromCam).setOnClickListener(this::imageFromCam);
        findViewById(R.id.addImgFromGall).setOnClickListener(this::imageFromGall);
        findViewById(R.id.BackImgBtn).setOnClickListener((view)->{
            Intent data = new Intent();
            data.putExtra("json", imgs.toString());
            ImageManager.this.setResult(RESULT_OK, data);
            ImageManager.this.finish();
        });
    }
}