package com.chat.alokraj.firebasechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private static final int SIGN_IN_REQUEST_CODE=1;
    private FirebaseListAdapter<ChatMessage> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            //Start sign in/sign up activity
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().build(),SIGN_IN_REQUEST_CODE);
        }else{
            //If user is already signed in
            Toast.makeText(this,"Welcome"+FirebaseAuth.getInstance().getCurrentUser().getDisplayName(),Toast.LENGTH_LONG).show();
            displayChatMessages();
        }
        FloatingActionButton fab =(FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText input = (EditText)findViewById(R.id.input);

                //ReAD input field and push ChatMessage to Firebase Database

                FirebaseDatabase.getInstance().getReference().push().setValue(new ChatMessage(input.getText().toString(),FirebaseAuth.getInstance().getCurrentUser().getDisplayName()));
                    //Clear the input
                input.setText("");
            }
        });
    }
        @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode,resultCode,data);
            if(requestCode == SIGN_IN_REQUEST_CODE){
                if(resultCode==RESULT_OK){
                    Toast.makeText(this,"Successfully signed in. Welcome!",Toast.LENGTH_LONG).show();
                    displayChatMessages();
                }else{
                    Toast.makeText(this,"We couldn't sign you in try again.",Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_sign_out) {
            AuthUI.getInstance().signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(MainActivity.this,
                                    "You have been signed out.",
                                    Toast.LENGTH_LONG)
                                    .show();

                            // Close activity
                            finish();
                        }
                    });
        }
        return true;
    }
    private void displayChatMessages() {
        ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);

        adapter = new FirebaseListAdapter<ChatMessage>(this, ChatMessage.class,
                R.layout.message, FirebaseDatabase.getInstance().getReference()) {
            @Override
            protected void populateView(View v, ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = (TextView)v.findViewById(R.id.message_text);
                TextView messageUser = (TextView)v.findViewById(R.id.message_user);
                TextView messageTime = (TextView)v.findViewById(R.id.message_time);

                // Set their text
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                messageTime.setText(DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                        model.getMessageTime()));
            }
        };

        listOfMessages.setAdapter(adapter);
    }
}
