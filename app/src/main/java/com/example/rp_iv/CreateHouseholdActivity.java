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

public class CreateHouseholdActivity extends AppCompatActivity {

    private EditText name;
    private TextView tv;
    private Button create;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_household);
        name = findViewById(R.id.editTextTextPersonName2);
        tv = findViewById(R.id.textView3);
        create = findViewById(R.id.button2);
        loading = findViewById(R.id.progressBar4);

        create.setOnClickListener(v -> {
            if (name.getText().toString().matches("")){
                Toast.makeText(this, "Name of the household cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            new CreateHousehold(name.getText().toString()).execute();
        });

        getSupportActionBar().setTitle("Create household");
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        name.setVisibility(value);
        tv.setVisibility(value);
        create.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateHousehold extends AsyncTask<Void, Void, Void> {
        String name;

        public CreateHousehold(String name) {
            this.name = name;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/createHousehold.php?uid=" + GlobalVariables.userId + "&household_name=" + name).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((getResources().getString(R.string.APIusername) + ":" + getResources().getString(R.string.APIpassword)).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("POST");
                System.out.println(con.getResponseCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Toast.makeText(getApplicationContext(), "Household created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            changeVisibility(true);
        }
    }
}