package com.example.rp_iv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class JoinHouseholdActivity extends AppCompatActivity {

    private EditText token;
    private TextView tv;
    private Button join, create;

    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_household);

        token = findViewById(R.id.editTextTextPersonName);
        tv= findViewById(R.id.textView);
        create = findViewById(R.id.button);
        join = findViewById(R.id.button3);

        loading = findViewById(R.id.progressBar3);

        create.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateHouseholdActivity.class));
            finish();
        });

        join.setOnClickListener(v -> {
            new JoinHousehold(token.getText().toString()).execute();
        });

        getSupportActionBar().setTitle("Join household");
    }

    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        token.setVisibility(value);
        tv.setVisibility(value);
        create.setVisibility(value);
        join.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class JoinHousehold extends AsyncTask<Void, Void, Void> {
        String token = "";
        boolean success = false;

        public JoinHousehold(String token) {
            this.token = token;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/connectUserToHousehold.php?uid=" +  GlobalVariables.userId + "&household_token=" + token ).openConnection();
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
                Toast.makeText(getApplicationContext(), "Joined household", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "This token doesn't exist", Toast.LENGTH_SHORT).show();
                changeVisibility(true);
                JoinHouseholdActivity.this.token.setText("");
                JoinHouseholdActivity.this.token.requestFocus();
            }
        }
    }
}