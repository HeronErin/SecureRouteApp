package com.github.heronerin.secureroute;

import android.content.Context;
import android.net.Uri;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public class GoogleDriveHelper {
    public final static int GOOGLE_SIGNIN = 69345;
    public static void signIn(FragmentActivity fragmentActivity) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .requestScopes(new Scope(DriveScopes.DRIVE_APPDATA))
                .requestEmail()
                .build();
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(fragmentActivity, signInOptions);
        fragmentActivity.startActivityForResult(googleSignInClient.getSignInIntent(), GOOGLE_SIGNIN);
    }
    public static GoogleSignInAccount getAccountForDrive(Context context){
        return GoogleSignIn.getAccountForScopes(context, new Scope(DriveScopes.DRIVE_APPDATA));
    }

    public static Thread uploadBackup(Context context) {
        Thread t = new Thread(()-> {
            DataBase db = DataBase.getOrCreate(context);
            java.io.File zipFileObject = null;
            try {
                zipFileObject = java.io.File.createTempFile("tempExport", ".zip");

                db.exportDBToZip(context, Uri.fromFile(zipFileObject));
                Drive d = getDrive(context);

                File fileMetadata = new File();
                fileMetadata.setName("backup.zip");
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                fileMetadata.setMimeType("application/zip");

                d.files().create(fileMetadata, new FileContent("application/zip", zipFileObject))
                        .setFields("id")
                        .execute();


            } catch (IOException e){
                e.printStackTrace();
            }
            finally {
                if (zipFileObject != null)
                    zipFileObject.delete();
            }
        });
        t.start();
        return t;
    }

    private static Drive getDrive(Context context){
        GoogleSignInAccount account = getAccountForDrive(context);
        if (account.getAccount() == null)
            throw new RuntimeException("Can't get account");

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singletonList(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(account.getAccount());

        return new Drive.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Secure Route")
                .build();
    }
    public static void deleteFile(Context context, File file) throws IOException {
        Drive d = getDrive(context);
        d.files().delete(file.getId()).execute();
    }
    public static java.io.File downloadFile(Context context, File file) throws IOException{
        Drive d = getDrive(context);
        java.io.File output = java.io.File.createTempFile("downloaded", ".zip");

        try (OutputStream outputStream = new FileOutputStream(output)) {
            d.files().get(file.getId()).executeMediaAndDownloadTo(outputStream);
        }
        return output;
    }
    public static List<File> getDatabases(Context context) throws IOException {
        Drive drive = getDrive(context);
        FileList files = drive
            .files().list()
            .setQ("mimeType='application/zip'")
            .setSpaces("appDataFolder")
            .setFields("files(id, name, modifiedTime)")
            .execute();
        return files.getFiles();
    }


////        DriveResourceClient resourceClient = Drive.getDriveResourceClient(context, account);
//        Drive service = new Drive.Builder(new NetHttpTransport(),
//                GsonFactory.getDefaultInstance(),
//                requestInitializer)
//                .setApplicationName("Drive samples")
//                .build();
//    }
}
