package com.example.rp_iv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class CustomView extends ConstraintLayout {
    private EditText mEditText;
    private ImageButton mImageButton, deleteNote;

    private KeyListener keyListener;

    private boolean editable = false;

    private int noteId;

    private Context context;

    public CustomView(Context context, int noteId) {
        super(context);
        this.noteId = noteId;
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_view, this);
        mEditText = findViewById(R.id.edit_text);
        mImageButton = findViewById(R.id.image_button);
        deleteNote = findViewById(R.id.imageButton5);
        keyListener = mEditText.getKeyListener();
        mEditText.setKeyListener(null);
        mImageButton.setOnClickListener(v -> {
            if (getEditable()) {
                mImageButton.setImageResource(R.drawable.baseline_mode_edit_24);
                setEditable(false);
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                new EditNote(getText()).execute();
            } else {
                mImageButton.setImageResource(R.drawable.baseline_check_black);
                setEditable(true);
                mEditText.requestFocus();
            }
        });

        deleteNote.setOnClickListener(v -> {
            new DeleteNote().execute();
        });
    }

    public int getNoteId() {
        return noteId;
    }

    public void setText(String text) {
        mEditText.setText(text);
    }

    public String getText() {
        return mEditText.getText().toString();
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mImageButton.setOnClickListener(listener);
    }

    public void setEditable(boolean enabled) {
        if (enabled) {
            editable = true;
            mEditText.setKeyListener(keyListener);

        } else {
            editable = false;
            mEditText.setKeyListener(null);
        }
    }

    public boolean getEditable() {
        return editable;
    }

    @SuppressLint("StaticFieldLeak")
    private class EditNote extends AsyncTask<Void, Void, Void> {
        String text;

        public EditNote(String text) {
            this.text = text;
        }

        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/editNote.php?note_text=" + text + "&note_id=" + getNoteId()).openConnection();
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
            Toast.makeText(context, "Note updated", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteNote extends AsyncTask<Void, Void, Void> {
        @SuppressLint("NewApi")
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("https://todoist.trialhosting.cz/api/deleteNote.php?note_id=" + getNoteId()).openConnection();
                con.setRequestProperty("Authorization", "Basic " + new String(Base64.getEncoder().encode((getResources().getString(R.string.APIusername) + ":" + getResources().getString(R.string.APIpassword)).getBytes(StandardCharsets.UTF_8))));
                con.setRequestMethod("POST");
                System.out.println(con.getResponseCode()); //to je tady schválně
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            Toast.makeText(context, "Note deleted", Toast.LENGTH_SHORT).show();
            CustomView.this.setVisibility(GONE);
        }
    }

}
