package com.github.heronerin.secureroute;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ManageBackups extends AppCompatActivity {
    FragmentActivity mContext;
    List<File> fileList;
    class BackupAdapter extends ArrayAdapter<File>{
        public BackupAdapter(@NonNull FragmentActivity context, List<File> list) {
            super(context, 0 , list);
            mContext = context;
            fileList = list;
        }
        @SuppressLint("ResourceType")
        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if (listItem == null)
                listItem = LayoutInflater.from(mContext).inflate(R.layout.backup_list_item, parent, false);
            final File file = fileList.get(position);
            ((TextView)listItem.findViewById(R.id.backupDate)).setText(new Date(file.getModifiedTime().getValue()).toString());

            listItem.findViewById(R.id.trashBtn).setOnClickListener((view)->{
                new Thread(()->{
                    try {
                        GoogleDriveHelper.deleteFile(mContext, file);
                    } catch (IOException e) {
                        mContext.runOnUiThread(()->
                                        Toast.makeText(mContext, "deletion failed", Toast.LENGTH_LONG).show()
                                );

                        return;
                    }
                    this.remove(file);
                }).start();

            });

            return listItem;
        }

    }



    BackupAdapter backupAdapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_backups);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Thread(()->{
            try {
                List<File> files = GoogleDriveHelper.getDatabases(ManageBackups.this);
                ManageBackups.this.runOnUiThread(()-> {
                    backupAdapter = new BackupAdapter(ManageBackups.this, files);
                    ((ListView) findViewById(R.id.backupsList)).setAdapter(backupAdapter);
                });
            } catch (IOException e) {
                ManageBackups.this.runOnUiThread(()->
                                Toast.makeText(mContext, "Failure to get google drive listing", Toast.LENGTH_LONG).show()
                        );
            }
        }).start();

    }
}