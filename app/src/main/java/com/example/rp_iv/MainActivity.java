package com.example.rp_iv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Spinner spinner1, spinner2;
    private Button button;
    private int householdId, groupId;

    private ImageButton addHousehold, addGroup, deleteHousehold, deleteGroup, info;

    private ProgressBar loading;

    private List<String> householdTokens = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        button = findViewById(R.id.signupButton);
        loading = findViewById(R.id.progressBar);
        addHousehold = findViewById(R.id.imageButton);
        addGroup = findViewById(R.id.imageButton2);
        deleteGroup = findViewById(R.id.imageButton3);
        deleteHousehold = findViewById(R.id.imageButton4);
        info = findViewById(R.id.imageButton7);

        button.setOnClickListener(view -> {
            if (spinner1.getSelectedItem() == null) {
                Toast.makeText(this, "Please create a household first", Toast.LENGTH_SHORT).show();
                return;
            } else if (spinner2.getSelectedItem() == null) {
                Toast.makeText(this, "Please create a group first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent i = new Intent(this, NoteView.class);
            i.putExtra("householdId", householdId);
            i.putExtra("groupId", groupId);
            startActivity(i);
        });

        addGroup.setOnClickListener(view -> {
            Intent i = new Intent(this, CreateGroupActivity.class);
            i.putExtra("id", householdId);
            startActivity(i);
        });

        addHousehold.setOnClickListener(view -> {
            startActivity(new Intent(this, JoinHouseholdActivity.class));
        });

        deleteGroup.setOnClickListener(v -> {
            new DeleteGroup(groupId).execute();
        });

        deleteHousehold.setOnClickListener(v -> {
            new DeleteHousehold(householdId).execute();
        });

        info.setOnClickListener(v -> {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("InflateParams") View inflatedLayoutViewResults = layoutInflater.inflate(R.layout.popup_view, null);
            inflatedLayoutViewResults.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));
            PopupWindow results = new PopupWindow(inflatedLayoutViewResults);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            results.setWidth(700);
            results.setHeight(1000);
            results.setFocusable(true);
            results.showAtLocation(v, Gravity.CENTER, 0, 0);
            results.setOutsideTouchable(false);
            String token = householdTokens.get(spinner1.getSelectedItemPosition());
            TextView tv_token = inflatedLayoutViewResults.findViewById(R.id.textView6);
            Button close = inflatedLayoutViewResults.findViewById(R.id.button5);
            ImageButton ib = inflatedLayoutViewResults.findViewById(R.id.imageButton6);
            ib.setImageResource(R.drawable.baseline_copy_black);
            ib.setOnClickListener(v12 -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(token, token);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
            });
            close.setOnClickListener(v1 -> results.dismiss());
            tv_token.setText(token);
        });

        getSupportActionBar().setTitle("Todoist");
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getHouseholds(this).execute();
    }

    private void changeVisibility(boolean on) {
        int value = on ? View.VISIBLE : View.INVISIBLE;
        spinner1.setVisibility(value);
        spinner2.setVisibility(value);
        button.setVisibility(value);
        deleteHousehold.setVisibility(value);
        deleteGroup.setVisibility(value);
        info.setVisibility(value);

        loading.setVisibility(on ? View.INVISIBLE : View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteHousehold extends AsyncTask<Void, Void, Void> {
        int id;

        boolean success = false;

        public DeleteHousehold(int id) {
            this.id = id;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/deleteHousehold.php?household_id=" + id + "&uid=" + GlobalVariables.userId).openConnection();
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
                Toast.makeText(getApplicationContext(), "Household deleted", Toast.LENGTH_SHORT).show();
                new getHouseholds(getApplicationContext()).execute();
            } else {
                Toast.makeText(getApplicationContext(), "You have to be admin to delete that household", Toast.LENGTH_SHORT).show();
            }
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteGroup extends AsyncTask<Void, Void, Void> {
        int id;

        public DeleteGroup(int id) {
            this.id = id;
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
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/deleteGroup.php?group_id=" + id).openConnection();
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
            Toast.makeText(getApplicationContext(), "Group deleted", Toast.LENGTH_SHORT).show();
            new getHouseholds(getApplicationContext()).execute();
            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getHouseholds extends AsyncTask<Void, Void, Void> {

        Context context;
        List<String> householdName = new ArrayList<>();
        List<Integer> householdIds = new ArrayList<>();

        public getHouseholds(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/userHouseholds.php?uid=" + GlobalVariables.userId).openConnection();
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
                    householdTokens.clear();
                    for (int i = 0; i < myArray.length(); i++) {
                        householdName.add((String) myArray.getJSONObject(i).get("household_name"));
                        householdIds.add((int) myArray.getJSONObject(i).get("household_id"));
                        householdTokens.add((String) myArray.getJSONObject(i).get("household_token"));
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, householdName);
            spinner1.setAdapter(adapter);
            if (householdName.isEmpty()) {
                new AlertDialog.Builder(context).setTitle("No households").setMessage("Please create or join new household").setNegativeButton("OK", null).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    int element = householdIds.get(i);
                    householdId = householdIds.get(i);
                    new getGroups(element).execute();

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            changeVisibility(true);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getGroups extends AsyncTask<Void, Void, Void> {

        List<String> groupName = new ArrayList<>();
        List<Integer> groupIds = new ArrayList<>();
        int element;

        public getGroups(int element) {
            this.element = element;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            changeVisibility(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/householdGroups.php?household_id=" + householdId).openConnection();
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
                        groupName.add((String) myArray.getJSONObject(i).get("group_name"));
                        groupIds.add((int) myArray.getJSONObject(i).get("group_id"));
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
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, groupName);
            spinner2.setAdapter(adapter);
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    groupId = groupIds.get(i);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            changeVisibility(true);
        }
    }

}