package com.example.sotw.donationtracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class registration extends AppCompatActivity {
    /**
     * UI elements
     */
    private EditText name;
    private EditText email;
    private EditText password;
    private Spinner spinner;

    /**
     * Useful objects
     */
    public Actor user;              //User Object
    private FirebaseAuth mAuth;     //Firebase Autherization object
    private DatabaseReference ref; //Reference to the DB..to let us modify it


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //grab the DB reference
        ref = FirebaseDatabase.getInstance().getReference();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        spinner = findViewById(R.id.spinner);

        String[] spinnerValues = new String[]{"Pick a type of user", "User", "Location Employee", "Branch Manager", "Admin"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerValues);
        spinner.setAdapter(adapter);


        Button registerButton = findViewById(R.id.button4);
        Button cancelButton = findViewById((R.id.button2));
        mAuth = FirebaseAuth.getInstance();


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cancelIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(cancelIntent);

            }

        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // A good principle in software engineering is the error ladder
                //Just check for errors first/incorrect inputs, and then do things

                //Grab the textview, its not expensive
                TextView failed = findViewById(R.id.failed);
                if (!invalidName(name.getText().toString()) &&
                        !invalidEmail(email.getText().toString()) &&
                        !invalidPassword(password.getText().toString()) &&
                        !spinner.getSelectedItem().toString().equals("Pick a type of user")) {
                    if (spinner.getSelectedItem().equals("User")) {
                        user = new User(name.getText().toString(), email.getText().toString(),
                                password.getText().toString(), spinner.getSelectedItem().toString());
                    } else if (spinner.getSelectedItem().equals("LocationEmployee")) {
                        user = new LocationEmployee(name.getText().toString(), email.getText().toString(),
                                password.getText().toString(), spinner.getSelectedItem().toString());
                    } else if (spinner.getSelectedItem().equals("Branch Manager")) {
                        user = new BranchManager(name.getText().toString(), email.getText().toString(),
                                password.getText().toString(), spinner.getSelectedItem().toString());
                    } else {
                        user = new Admin(name.getText().toString(), email.getText().toString(),
                                password.getText().toString(), spinner.getSelectedItem().toString());

                    }
                    Intent registeredIntent = new Intent(getApplicationContext(), RegisteredActivity.class);
                    firstAuthentication(user);
                    registeredIntent.putExtra("userType", user.getUserType());
                    registeredIntent.putExtra("name", user.getName());
                    registeredIntent.putExtra("e-mail", user.getEmail());
                    startActivity(registeredIntent);



                }
                    else {

                    failed.setText("Invalid information. Please try again");
                }


            }


        });


    }


    private boolean invalidPassword(String password){
        //Validation logic from some framework
        //for now
        return password.equals("");
    }

    private boolean invalidEmail(String email){
        //Validation logic from some framework
        //for now
        return email.equals("");
    }
    private boolean invalidName(String name){
        return name.equals("") || name.length() <=2;
    }



    private boolean createUserInDB(Actor user,FirebaseUser firebaseUser){
        DatabaseReference usersRef = ref.child("users");

        //Two ways of modifying the DB... Useful for you reading over this code
        /*
        this way lets you put multiple users at the same time into the DB

            Map<String, User> users = new HashMap<>();
            users.put(firebaseUser.getUid(), user);
            usersRef.setValue(users);

        */

        //Since I only care about putting a single user in the DB

        usersRef.child(firebaseUser.getUid()).setValue(user);

        return true;
    }

    private void firstAuthentication(final Actor user){
        mAuth.createUserWithEmailAndPassword(user.getEmail(),user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Success", "createUserWithEmail:success");
                            FirebaseUser fireBaseUser = mAuth.getCurrentUser();

                            //Create the User in the DB
                            if(createUserInDB(user,fireBaseUser)){
                                Intent registerIntent = new Intent(getApplicationContext(),
                                        RegisteredActivity.class);
                                startActivity(registerIntent);
                                Log.d("Success", "DBCreationTask:success");

                            }else{
                                Log.w("Failure", "DBCreationTask:failure");
                                TextView failed = findViewById(R.id.failed);
                                failed.setText("Something Went Wrong, Please Try Again");
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Failure", "createUserWithEmail:failure", task.getException());
                            TextView failed = findViewById(R.id.failed);
                            failed.setText("Something Went Wrong, Please Try Again");
                        }

                    }
                });
    }





    /**
     * Suggestion from Firebase guide
     */
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //TODO do something
    }
}
