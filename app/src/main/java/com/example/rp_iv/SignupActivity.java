package com.example.rp_iv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    EditText email, username, password, passwordConfirm;

    TextView emailLabel, usernameLabel, passwordLabel, passwordConfirmLabel;

    Button signupButton;

    ProgressBar loading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        email = findViewById(R.id.email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        passwordConfirm = findViewById(R.id.passwordConfirm);
        signupButton = findViewById(R.id.signup);
        emailLabel = findViewById(R.id.emailLable);
        usernameLabel = findViewById(R.id.usernameLable);
        passwordLabel = findViewById(R.id.passwordLable);
        passwordConfirmLabel = findViewById(R.id.passwordConfirmLable);
        loading = findViewById(R.id.progressBar2);


        signupButton.setOnClickListener(view -> {

            if (username.getText().toString().isEmpty() || password.getText().toString().isEmpty() || email.getText().toString().isEmpty() || passwordConfirm.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please fill out the whole login form", Toast.LENGTH_SHORT).show();
                return;
            } else if (!password.getText().toString().equals(passwordConfirm.getText().toString())) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            } else if (Pattern.compile("^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$").matcher(email.getText().toString()).matches()) {
                new ApiSignup(email.getText().toString(), username.getText().toString(), password.getText().toString(), passwordConfirm.getText().toString()).execute();
            }

        });

        getSupportActionBar().setTitle("Sign up");
    }

    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        email.setVisibility(value);
        username.setVisibility(value);
        password.setVisibility(value);
        passwordConfirm.setVisibility(value);
        emailLabel.setVisibility(value);
        usernameLabel.setVisibility(value);
        passwordLabel.setVisibility(value);
        passwordConfirmLabel.setVisibility(value);

        signupButton.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class ApiSignup extends AsyncTask<Void, Void, Void> {
        String email = "";
        String username = "";
        String password = "";

        String confirmPassword = "";
        int userId = 0;
        boolean success = false;

        public ApiSignup(String email, String username, String password, String confirmPassword) {
            this.email = email;
            this.username = username;
            this.password = password;
            this.confirmPassword = confirmPassword;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/registerUser.php?username=" + username + "&password=" + password + "&email=" + email).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((getResources().getString(R.string.APIusername) + ":" + getResources().getString(R.string.APIpassword)).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("POST");

                 success = con.getResponseCode() == 200;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
                if (success) {
                    Toast.makeText(SignupActivity.this, "Signup successful, please refer Your email to verify Your account", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                } else {
                    Toast.makeText(SignupActivity.this, "Something went wrong Try again", Toast.LENGTH_SHORT).show();
                    changeVisibility(true);
                    SignupActivity.this.email.setText("");
                    SignupActivity.this.username.setText("");
                    SignupActivity.this.password.setText("");
                    SignupActivity.this.password.setText("");
                    SignupActivity.this.passwordConfirm.setText("");
                    SignupActivity.this.email.requestFocus();
                }

            //changeVisibility(true);
        }
    }
}