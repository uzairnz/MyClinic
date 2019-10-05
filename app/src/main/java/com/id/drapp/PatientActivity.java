package com.id.drapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
public class PatientActivity extends AppCompatActivity {
    private Button createAccount;
    private Button loginButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient);


        mFirebaseDatabase = MyDatabaseUtil.getDatabase();
        mDatabaseReference = mFirebaseDatabase.getReference().child("patientData");

        if(doctorPreference.getBooleanFromSP(this)){
            Intent intent = new Intent(this, patientsListActivity.class);
            startActivity(intent);
        }

        Context context = myApp.getAppContext();

        createAccount = findViewById(R.id.createAccount);
        loginButton = findViewById(R.id.loginButton);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientActivity.this, PatientSignup.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PatientActivity.this, PatientLogin.class);
                startActivity(intent);
            }
        });
    }

}
