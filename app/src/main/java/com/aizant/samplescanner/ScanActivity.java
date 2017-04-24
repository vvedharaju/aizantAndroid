package com.aizant.samplescanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class ScanActivity extends AppCompatActivity {

    static final String SCAN = "com.google.zxing.client.android.SCAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
    }

    public void scanDataMatrix(View view) {
        try {
            Intent intent = new Intent(SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
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

    public class ScanBarcodeRequest extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){}

        protected String doInBackground(String... barcodes) {
            try {
                URL url = new URL("http://192.168.40.79:8080/aizantit/barcode");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                System.out.println("SCANNING BARCODEEE" + barcodes[0]);
//                writer.write(barcodes[0]);
//
//                writer.flush();
//                writer.close();
                System.out.println("BARCODEEEE" + barcodes[0]);
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
                    return "Error scanning";
                }
            } catch(Exception e) {
                return "Error scanning";
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
