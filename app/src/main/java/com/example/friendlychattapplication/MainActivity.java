package com.example.friendlychattapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final int RC_SIGN_IN =1 ;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    FirebaseDatabase mFirebaseDatabase;             // firebase daatabase obj  (This is a entry point for our app to acess the database)
    DatabaseReference mMessagesDatabaseRefrence;   //Database refrence obj    ( it reffers to a specific part of a database.
    ChildEventListener mchildEventListener;

    //AuthStateListner is used to check the the state whether the user is signedIn or signedOut
    FirebaseAuth mfirebaseAuth;
    FirebaseAuth.AuthStateListener mauthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsername = ANONYMOUS;

        mFirebaseDatabase = FirebaseDatabase.getInstance();     //getInstance() - used to get instance of class  (main accesing point for accesing the database)
        //using this acess point we are going to acess to a specifc part of database
        mMessagesDatabaseRefrence = mFirebaseDatabase.getReference().child("messages");        //getReference() - Gets a refrence to root node. THESE "messages" is the ROOT NODE


        // Initialize Firebase Auth object
        mfirebaseAuth = FirebaseAuth.getInstance();
                                                                                               //child("messages") - and in this we only interested in the messages portion of database
        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Fire an intent to show an image picker
            }
        });

        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Send messages on click
                //This object has all the keys that we’ll store as a message in the realtime database
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUsername, null);

                // In the next step we’ll store this data to the cloud in our realtime database.

                //WRITTING  messages to the DATABASE
                mMessagesDatabaseRefrence.push().setValue(friendlyMessage);      // mMessagesDatabaseRefrence.setValue(friendlyMessage); if we use this it will replace the messages in the database and will not create a unique id for each message

                // Clear input box
                mMessageEditText.setText("");
            }
        });
       /* mchildEventListener = new ChildEventListener() {
            @Override
            //DataSnapshot dataSnapshot - contains the data from firebase database

            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // This method is called whenever new message is inserted into the messages list
                // It is also called for every child message in the list when the listner is first attached (it means that for every child message that already exixt in the list the code in this method will be called

                //the message will be deserialized to a friedly message object i.e  "friendlyMessage"
                FriendlyMessage friendlyMessage= dataSnapshot.getValue(FriendlyMessage.class);                 //getValue()- to get data of new msg. the getValue parameter takes a class as an argument by passing in this parameter the (code will deserialize the msg from the database into our FriendlyMessage obj this works becoz our FriendlyMessage class has same feilds tht match with our database
                mMessageAdapter.add(friendlyMessage);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // This gets called when contents of exixting message gets changed
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                //It is called when exixting child is deleted
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // it is called if one of our messages changed position in the list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //It indicates some sort of error occured when you are trying make changes
                //If gets called you do not have permission to read data
            }
        };
        //The refrence defines what actually i'm listening to and listner obj derfines exactly what happens to the data
        mMessagesDatabaseRefrence.addChildEventListener(mchildEventListener);   //addChildEventListener()- Add a listener for child events ourring at this location. When child locations are added, removed, changed, or moved, the listener will be triggered for the appropriate event

*/
       mauthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //The diff b/w FirebaseAuth firebaseAuth and the one which we created(mfirebaseAuth) is that this FirebaseAuth parameter only contins that whether at that moment the user is authenticated or not

                //FirebaseAuth firebaseAuth-  it contains 2 states either signedIn or signedOut

                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user!=null){
                    //User SignedIn
                    //Toast.makeText(MainActivity.this, "You are Signed In!!", Toast.LENGTH_SHORT).show();

                    //In signup initialization we will pass username and we can get the username by taking the FirebaseUser obj i.e (user)
                    onSignedInInitialize(user.getDisplayName());
                }else {
                    //User SignedOut
                    onSignedOutCleanup();
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)       //smartlock allows the phone to sort credentials and try to log them in
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);        //RC_SIGN_IN - it is a flag for when we return from starting the activity for result
                }

            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN){
            if(resultCode==RESULT_OK){
                Toast.makeText(this, "Signed In!", Toast.LENGTH_SHORT).show();
            }
            else if (resultCode==RESULT_CANCELED){
                Toast.makeText(this, "Signed in Cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    // will only show msg(Database) when user is logged in
    @Override
    protected void onResume() {
        super.onResume();
        mfirebaseAuth.addAuthStateListener(mauthStateListener);     //add AuthState listner
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Removal of Authstate listner
        mfirebaseAuth.removeAuthStateListener(mauthStateListener);
        dettachDatabaseReadListner();
        mMessageAdapter.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.sign_out_menu:
                //sign out
                AuthUI.getInstance().signOut(this);
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }
    //In onSignedInInitialize we will set the username we can do that by taking my username memb variable
    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListner();

    }

    // this func will unset the username,clr the msgsList and detach the listner
    // Teardown the UI
    private void onSignedOutCleanup() {
        mUsername =ANONYMOUS;
        mMessageAdapter.clear();
        dettachDatabaseReadListner();
    }

    private void attachDatabaseReadListner() {

        if(mchildEventListener==null) {


            //We have placed the code here becoz priorly thus code was in onCreate and for acessing database the user must be signedIn and this we are setting the username so it must be done before reading the database
            mchildEventListener = new ChildEventListener() {
                @Override
                //DataSnapshot dataSnapshot - contains the data from firebase database

                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // This method is called whenever new message is inserted into the messages list
                    // It is also called for every child message in the list when the listner is first attached (it means that for every child message that already exixt in the list the code in this method will be called

                    //the message will be deserialized to a friedly message object i.e  "friendlyMessage"
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);                 //getValue()- to get data of new msg. the getValue parameter takes a class as an argument by passing in this parameter the (code will deserialize the msg from the database into our FriendlyMessage obj this works becoz our FriendlyMessage class has same feilds tht match with our database
                    mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // This gets called when contents of exixting message gets changed
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    //It is called when exixting child is deleted
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    // it is called if one of our messages changed position in the list
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    //It indicates some sort of error occured when you are trying make changes
                    //If gets called you do not have permission to read data
                }
            };
            //The refrence defines what actually i'm listening to and listner obj derfines exactly what happens to the data
            mMessagesDatabaseRefrence.addChildEventListener(mchildEventListener);   //addChildEventListener()- Add a listener for child events ourring at this location. When child locations are added, removed, changed, or moved, the listener will be triggered for the appropriate event
        }
    }
    private void dettachDatabaseReadListner() {
        if(mchildEventListener!=null){
            mMessagesDatabaseRefrence.removeEventListener(mchildEventListener);
            mchildEventListener=null;
        }

    }
}

