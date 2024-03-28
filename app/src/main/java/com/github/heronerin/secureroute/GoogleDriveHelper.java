package com.github.heronerin.secureroute;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

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
    public static void getLastDriveBackup(Context context) throws IOException {
        GoogleSignInAccount account = getAccountForDrive(context);
//        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account.getAccount() == null)
            throw  new RuntimeException("Can't get account");

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        context, Collections.singletonList(DriveScopes.DRIVE_APPDATA));
        credential.setSelectedAccount(account.getAccount());
        new Thread(()->{
            try {
                java.io.File f = java.io.File.createTempFile("tempdb", ".json");


                Drive d = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Secure Route")
                        .build();

                File fileMetadata = new File();
                fileMetadata.setName("database.json");
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                fileMetadata.setMimeType("application/json");

                File file = d.files().create(fileMetadata, new FileContent("application/json", f))
                        .setFields("id")
                        .execute();
//                System.out.println("File ID: " + file.getId());
//                System.out.println("File ID: " + d.files().list().size());
                FileList result = d.files().list()
                        .setQ("mimeType='application/json'")
                        .setSpaces("appDataFolder")
                        .setFields("files(id, name, modifiedTime)")
                        .execute();
                System.out.println("File list: " + result.getFiles().size());
                for (int i = 0; i < result.getFiles().size(); i++) {
                    file = result.getFiles().get(i);
                    System.out.println(file.getName() + " : " + file.getModifiedTime().toString());
//                    d.files().delete(file.getId()).execute();
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();

        return;
    }

    public static void uploadBackup(Context context) {
        new Thread(()-> {
            DataBase db = DataBase.getOrCreate(context);
            java.io.File zipFileObject = null;
            try {
                zipFileObject = java.io.File.createTempFile("tempExport", ".zip");

                db.exportDBToZip(context, Uri.fromFile(zipFileObject));
                GoogleSignInAccount account = getAccountForDrive(context);
                if (account.getAccount() == null)
                    throw new RuntimeException("Can't get account");

                GoogleAccountCredential credential =
                        GoogleAccountCredential.usingOAuth2(
                                context, Collections.singletonList(DriveScopes.DRIVE_APPDATA));
                credential.setSelectedAccount(account.getAccount());

                Drive d = new Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("Secure Route")
                        .build();

                File fileMetadata = new File();
                fileMetadata.setName("backup.zip");
                fileMetadata.setParents(Collections.singletonList("appDataFolder"));
                fileMetadata.setMimeType("application/zip");

                File file = d.files().create(fileMetadata, new FileContent("application/zip", zipFileObject))
                        .setFields("id")
                        .execute();


            } catch (IOException e){
                e.printStackTrace();
            }
            finally {
                if (zipFileObject != null)
                    zipFileObject.delete();
            }
        }).start();
    }

////        DriveResourceClient resourceClient = Drive.getDriveResourceClient(context, account);
//        Drive service = new Drive.Builder(new NetHttpTransport(),
//                GsonFactory.getDefaultInstance(),
//                requestInitializer)
//                .setApplicationName("Drive samples")
//                .build();
//    }
}
