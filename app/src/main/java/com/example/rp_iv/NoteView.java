package com.example.rp_iv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class NoteView extends AppCompatActivity {
    private LinearLayout mLayout;
    private ProgressBar loading;
    private int groupId, householdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        mLayout = findViewById(R.id.layout);
        loading = findViewById(R.id.loading_note_view);

        Intent i = getIntent();
        householdId = i.getIntExtra("householdId", 0);
        groupId = i.getIntExtra("groupId", 0);

        new LoadNotes().execute();
        getSupportActionBar().setTitle("Notes");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.groups_items, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                new CreateNote().execute();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void changeVisibility(boolean on) {
        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
        mLayout.setVisibility(on ? View.VISIBLE : View.INVISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadNotes extends AsyncTask<Void, Void, Void> {

        List<Integer> id = new ArrayList<>();
        List<String> text = new ArrayList<>(), created = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/userNotes.php?group_id=" + groupId).openConnection();
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

                    JSONArray myArray = new JSONArray(content.toString());
                    for (int i = 0; i < myArray.length(); i++) {
                        text.add(myArray.getJSONObject(i).get("note_text").toString());
                        id.add((int) myArray.getJSONObject(i).get("note_id"));
                        created.add((String) myArray.getJSONObject(i).get("created_at").toString());
                    }
                } else {
                    //error
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    throw new IllegalStateException((String) new JSONObject(content.toString()).get("error_description"));

                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            for (int i = 0; i < id.size(); i++) {
                CustomView customView = new CustomView(getApplicationContext(), id.get(i));
                if(text.get(i).equals("null")){
                    customView.setText("");
                } else {
                    customView.setText(text.get(i));
                }

                mLayout.addView(customView);
            }

            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CreateNote extends AsyncTask<Void, Void, Void> {

        int noteId = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/createNote.php?group_id=" + groupId).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((getResources().getString(R.string.APIusername) + ":" + getResources().getString(R.string.APIpassword)).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("POST");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject myObject = new JSONObject(content.toString());
                noteId = (int) myObject.get("id");

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            CustomView customView = new CustomView(getApplicationContext(), noteId);
            mLayout.addView(customView);
            changeVisibility(true);
        }
    }
}