package com.example.rp_iv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    TextView usernameLabel, passwordLabel, newHere;
    Button loginButton, signupButton;
    ProgressBar loading;

    CheckBox remember;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.loginUsername);
        password = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        signupButton = findViewById(R.id.signupButton);
        usernameLabel = findViewById(R.id.loginTextUsername);
        passwordLabel = findViewById(R.id.loginPasswordText);
        newHere = findViewById(R.id.textView2);
        loading = findViewById(R.id.loading);
        remember = findViewById(R.id.checkBox);

        SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
        if (preferences.getString("remember", "").equals("true")) {
            remember.setChecked(true);
            username.setText(preferences.getString("username", ""));
            password.setText(preferences.getString("password", ""));
        }

        remember.setOnCheckedChangeListener((compoundButton, b) -> {
            SharedPreferences preferences1 = getSharedPreferences("checkbox", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            if (compoundButton.isChecked()) {
                editor.putString("remember", "true");
            } else {
                editor.putString("remember", "false");
            }
            editor.apply();
        });

        loginButton.setOnClickListener(view -> {
            if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill out the whole login form", Toast.LENGTH_SHORT).show();
                return;
            }
            new ApiLogin(username.getText().toString(), password.getText().toString()).execute();
        });

        signupButton.setOnClickListener(view -> startActivity(new Intent(this, SignupActivity.class)));

        getSupportActionBar().setTitle("Login");
    }


    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        username.setVisibility(value);
        password.setVisibility(value);
        usernameLabel.setVisibility(value);
        passwordLabel.setVisibility(value);
        newHere.setVisibility(value);
        loginButton.setVisibility(value);
        signupButton.setVisibility(value);
        remember.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class ApiLogin extends AsyncTask<Void, Void, Void> {

        String username = "";
        String password = "";
        int userId = 0;
        boolean verified = false, loginSuccess = false;
        String email = "";

        public ApiLogin(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/userData.php?username=" + username + "&password=" + password).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((getResources().getString(R.string.APIusername) + ":" + getResources().getString(R.string.APIpassword)).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("GET");

                if (con.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONObject myObject = new JSONArray(content.toString()).getJSONObject(0);
                    userId = (int) myObject.get("user_id");
                    email = myObject.get("email").toString();
                    verified = myObject.get("verified_status").toString().equals("1");
                    loginSuccess = true;

                } else {
                    loginSuccess = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (loginSuccess) {
                if (verified) {
                    SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.apply();

                    GlobalVariables.userId = userId;
                    GlobalVariables.email = email;
                    GlobalVariables.username = username;
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Please verify account", Toast.LENGTH_SHORT).show();
                    changeVisibility(true);
                }
            } else {
                LoginActivity.this.username.setText("");
                LoginActivity.this.password.setText("");
                LoginActivity.this.username.requestFocus();
                Toast.makeText(LoginActivity.this, "Wrong credentials", Toast.LENGTH_SHORT).show();
                changeVisibility(true);
            }
        }
    }

}