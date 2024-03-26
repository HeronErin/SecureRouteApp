package com.github.heronerin.secureroute;

import android.content.Context;
import android.os.CancellationSignal;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;

import java.util.concurrent.Executor;

public class driveHelper {
    private final String CLIENT_ID = "244113212850-1447iegol1kv3e33i0idjns20pudaavt.apps.googleusercontent.com";

    driveHelper(Context context){
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(true)
                .setServerClientId(CLIENT_ID)
                .build();
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build();
        CredentialManager credentialManager= CredentialManager.create(context);

        credentialManager.getCredentialAsync(
                context, request, new CancellationSignal(), command -> command.run(),
                new CredentialManagerCallback() {
                    @Override
                    public void onResult(Object o) {
                        GetCredentialResponse response = (GetCredentialResponse) o;
                        Log.d("res", response.toString());
                    }

                    @Override
                    public void onError(@NonNull Object o) {
                        if (o instanceof androidx.credentials.exceptions.NoCredentialException){
                            ((NoCredentialException) o).printStackTrace();
                            Toast.makeText(context, "ERROR: No google acount found", Toast.LENGTH_LONG).show();
                        }
                        if (o instanceof androidx.credentials.exceptions.GetCredentialCancellationException)
                            ((GetCredentialCancellationException) o).printStackTrace();
//                        GetCredentialResponse response = (GetCredentialResponse) o;
                        Log.d("Error", o.toString());
                    }
                }
        );
    }
}
