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

public class CreateGroupActivity extends AppCompatActivity {

    private EditText name;
    private TextView tv;
    private Button create;
    private ProgressBar loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        name = findViewById(R.id.editText);
        tv = findViewById(R.id.textView4);
        create = findViewById(R.id.button4);
        loading = findViewById(R.id.progressBar4);

        Intent i = getIntent();
        int householdId = i.getIntExtra("id", 0);

        create.setOnClickListener(v -> {
            if (name.getText().toString().matches("")){
                Toast.makeText(this, "Name of the group cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            new CreateGroup(name.getText().toString(), householdId).execute();
        });
        getSupportActionBar().setTitle("Create group");

    }

    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        name.setVisibility(value);
        tv.setVisibility(value);
        create.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateGroup extends AsyncTask<Void, Void, Void> {
        String name;
        int householdId;

        public CreateGroup(String name, int householdId) {
            this.name = name;
            this.householdId = householdId;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/createGroup.php?household_id=" + householdId + "&group_name=" + name).openConnection();
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
            Toast.makeText(getApplicationContext(), "Group created", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
            changeVisibility(true);
        }
    }
}