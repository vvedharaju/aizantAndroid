package com.aizant.samplescanner;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    static final String IP_ADDRESS = "192.168.1.6:8080";
    static final String BASE_URL = "http://" + IP_ADDRESS + "/aizantit";
    static final String PREFERENCE_FILE = "com.aizant.samplescanner.PREFERENCE_FILE";
    static final String USERNAME = "username";
    static final String PASSWORD = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void loginUser(View view) {
        Log.e("AIZANT", "TRYING TO LOG IN");
        EditText usernameEditText = (EditText) findViewById(R.id.username);
        EditText passwordEditText = (EditText) findViewById(R.id.password);

        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        new LoginRequest().execute(username, password);
    }

    public class LoginRequest extends AsyncTask<String, Void, String>{

        protected void onPreExecute(){}

        protected String doInBackground(String... credentials) {
            try {
                String username = credentials[0];
                String password = credentials[1];
                String data = URLEncoder.encode("username", "UTF-8")
                        + "=" + URLEncoder.encode(username, "UTF-8");

                data += "&" + URLEncoder.encode("password", "UTF-8") + "="
                        + URLEncoder.encode(password, "UTF-8");

                URL loginUrl = new URL(BASE_URL + "/auth_check");
                HttpURLConnection loginConn = (HttpURLConnection) loginUrl.openConnection();
                loginConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                loginConn.setRequestMethod("POST");
                loginConn.setDoInput(true);
                loginConn.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(loginConn.getOutputStream());
                wr.write(data);
                wr.flush();
                wr.close();

                int responseCode = loginConn.getResponseCode();

                if (responseCode != 200) {
                    throw new Exception("UNABLE TO LOGIN");
                }

                SharedPreferences settings = getSharedPreferences(PREFERENCE_FILE, MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(USERNAME, username);
                editor.putString(PASSWORD, password);
                editor.commit();

                Intent intent = new Intent(LoginActivity.this, ScanActivity.class);
                startActivity(intent);

                return  "Successfully logged in";
            } catch (Exception e) {
                return "Error logging in: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }

    }
}

