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
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ScanActivity extends AppCompatActivity {

    static final String SCAN = "com.google.zxing.client.android.SCAN";
    static final String IP_ADDRESS = "192.168.1.12:8080";
    static final String BASE_URL = "http://" + IP_ADDRESS + "/aizantit";
    static final String CURRENT_SESSION = "currentSession";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
    }

    public void scanDataMatrix(View view) {
        try {
            Intent intent = new Intent(SCAN);
            intent.putExtra("SCAN_MODE", "DATA_MATRIX_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException e) {
            System.out.println("NO SCANNER FOUNDDDD");
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

    public void loginUser(View view) {
        Log.e("AIZANT", "TRYING TO LOG IN");
        new LoginSessionRequest().execute(BASE_URL);
    }

    public class LoginSessionRequest extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){}

        protected String doInBackground(String... urls) {
            Log.e("AIZANT", "doInBackground " + BASE_URL);
            try {
                URL url = new URL(BASE_URL + "/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.connect();

                String setCookieHeaderField = conn.getHeaderField("Set-Cookie");
                if (setCookieHeaderField == null) {
                    throw new Exception("No session returned");
                }

                Log.e("AIZANT", "COOOKIE HEADER FIELD " + setCookieHeaderField);

                String username = "ravi";
                String password = "ravi";
                String data = URLEncoder.encode("username", "UTF-8")
                        + "=" + URLEncoder.encode(username, "UTF-8");

                data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password, "UTF-8");


                URL loginUrl = new URL(BASE_URL + "/j_spring_security_check");
                HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
                loginConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                loginConn.setRequestProperty("charset", "utf-8");
                loginConn.setRequestProperty("Cookie", setCookieHeaderField);
                loginConn.setRequestMethod("POST");
                loginConn.setDoInput(true);
                loginConn.setDoOutput(true);
                loginConn.connect();


                OutputStreamWriter wr = new OutputStreamWriter(loginConn.getOutputStream());
                wr.write(data);
                wr.flush();

                int responseCode = loginConn.getResponseCode();
                Log.e("AIZANT", "RESPONSE CODEEEEE " + responseCode);
                if (responseCode != 200) {
                    throw new Exception("UNABLE TO LOGIN");
                }
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(CURRENT_SESSION, setCookieHeaderField);
                editor.commit();
                return  setCookieHeaderField;
            } catch (Exception e) {
                Log.e("AIZANT", "GOT ITTTTTT " + e.getMessage());
                Toast.makeText(getApplicationContext(), e.getMessage(),Toast.LENGTH_LONG).show();
                return e.getMessage();
            }
        }
    }

    public class ScanBarcodeRequest extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){}

        protected String doInBackground(String... barcodes) {
            try {
                SharedPreferences settings = getPreferences(MODE_PRIVATE);
                String cookie = settings.getString(CURRENT_SESSION, null);

                if (cookie == null) {
                    throw new Exception("Please login. If already logged in, please logout and login");
                }

                URL url = new URL(BASE_URL + "/barcode");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
                conn.setRequestProperty("Cookie", cookie);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                byte[] barcodeBytes = barcodes[0].getBytes("UTF-8");
                os.write( barcodeBytes );
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
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
                new ScanBarcodeRequest().execute(contents);
            }
        }
    }
}
