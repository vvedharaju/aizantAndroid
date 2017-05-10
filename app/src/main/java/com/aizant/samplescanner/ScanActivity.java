package com.aizant.samplescanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanActivity extends AppCompatActivity {

    static final String SCAN = "com.google.zxing.client.android.SCAN";
    static final String IP_ADDRESS = "192.168.1.6:8080";
    static final String BASE_URL = "http://" + IP_ADDRESS + "/aizantit";
    static final String TOKEN =  "30652800-6672-41a8-9605-292dbdc95734";


    static final String USERNAME = "username";
    static final String PASSWORD = "password";
    static final String PREFERENCE_FILE = "com.aizant.samplescanner.PREFERENCE_FILE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
        String username = sharedPreferences.getString(USERNAME, "Anonymous");

        TextView usernameText = (TextView) findViewById(R.id.loggedInUser);
        usernameText.setText("Hi " + username + "!");
    }

    public void logout(View view) {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(USERNAME);
        editor.remove(PASSWORD);
        editor.commit();

        Toast.makeText(getApplicationContext(), "Logging out!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void scanDataMatrix(View view) {
        try {
            Intent intent = new Intent(SCAN);
            intent.putExtra("SCAN_MODE", "DATA_MATRIX_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            showDialog(ScanActivity.this, "No Scanner Found.", "Download a scanner?",
                    "Yes", "No");
        }
    }

    private Dialog showDialog(final Activity activity, CharSequence title, CharSequence message,
                              CharSequence yesText, CharSequence noText) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent marketIntent = new Intent(Intent.ACTION_VIEW).setData(uri);
                System.out.println("MARKET INTENT");
                activity.startActivity(marketIntent);
            }
        });
        dialog.setNegativeButton(noText, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        return dialog.show();
    }

    public class ScanBarcodeRequest extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){}

        protected String doInBackground(String... params) {
            try {
                String barcode = params[0];
                String username = params[1];
                String password = params[2];

                String data = "{" +
                            "\"username\": \""  + username + "\"," +
                            "\"password\": \""  + password + "\"," +
                            "\"scannedId\": \"" + barcode  + "\"," +
                            "\"token\": \""     + TOKEN    + "\"" +
                        "}";

                URL url = new URL(BASE_URL + "/barcode");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                wr.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    return "Successful scan!";
                }
                else {
                    return "Unexpected response from server";
                }
            } catch(Exception e) {
                return "Error scanning: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");

                SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
                String username = sharedPreferences.getString(USERNAME, "");
                String password = sharedPreferences.getString(PASSWORD, "");

                new ScanBarcodeRequest().execute(contents, username, password);
            }
        }
    }
}
