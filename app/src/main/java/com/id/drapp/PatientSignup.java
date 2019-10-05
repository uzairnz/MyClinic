package com.id.drapp;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.id.drapp.doctorContract.patientEntry;
import com.id.drapp.models.doctorInfo;
import com.id.drapp.models.patientsInfo;

import java.io.IOException;

public class PatientSignup extends AppCompatActivity{

    //Buttons and Strings
    private Button createAccount;
    private TextView inputName;
    private TextView inputphone;
    private TextView inputemail;
    private TextView inputpassword;
    private TextView inputdob;
    private TextView inputaddress;
    private RadioGroup radioGroup;
    private String usermail;

    //For Gender Radio Button Conversion
    public static int GENDER = 0;

    private byte[] bmpByte;


    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private ProgressDialog progressDialog;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_signup);

        //Progress Dialogue
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setMessage("Please Wait..");
        progressDialog.setCancelable(false);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        createAccount = findViewById(R.id.createPatientButton);
        inputName = findViewById(R.id.patientName);
        inputphone = findViewById(R.id.patientphno);
        inputemail = findViewById(R.id.patientEmail);
        inputpassword = findViewById(R.id.patientpassword);
        inputdob = findViewById(R.id.patientDob);
        inputaddress = findViewById(R.id.patientAdd);
        radioGroup = findViewById(R.id.radioGroup);


        //Firebase Objects
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReference();


        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signupPatient();
            }
        });
    }


    public void signupPatient(){
    final String name = inputName.getText().toString();
    final String phone = inputphone.getText().toString();
    final String email = inputemail.getText().toString();
    final String password = inputpassword.getText().toString();
    final String dob = inputdob.getText().toString();
    final String address = inputaddress.getText().toString();

    usermail = email;

    if(TextUtils.isEmpty(name)){
        inputName.setError("Cannot be empty");
    }else{
        if (TextUtils.isEmpty(phone)){
            inputphone.setError("Cannot be empty");
        }else {
            if (TextUtils.isEmpty(email)){
                inputemail.setError("Cannot be empty");
            }else {
                if (TextUtils.isEmpty(password)){
                    inputpassword.setError("Cannot be empty");
                }else {
                    if (TextUtils.isEmpty(dob)){
                        inputdob.setError("Cannot be empty");
                    }else {
                        if (TextUtils.isEmpty(address)){
                            inputaddress.setError("Cannot be empty");
                        }else {
                            progressDialog.show();
                            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                            if (networkInfo == null){
                                Toast.makeText(this,"No Internet", Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                                return;
                            }else {
                                final ContentValues cv = new ContentValues();
                                cv.put(patientEntry.COLUMN_NAME,name);
                                cv.put(patientEntry.COLUMN_PHONE_NUMBER,phone);
                                cv.put(patientEntry.COLUMN_EMAIL,email);
                                cv.put(patientEntry.COLUMN_PASSWORD,password);
                                cv.put(patientEntry.COLUMN_DOB,dob);
                                cv.put(patientEntry.COLUMN_ADDRESS,address);
                                cv.put(patientEntry.COLUMN_GENDER, GENDER);
                                cv.put(patientEntry.COLUMN_IMAGE,bmpByte);

                                final Uri[] uri = new Uri[1];

                                firebaseAuth.createUserWithEmailAndPassword(email,password)
                                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                            @Override
                                            public void onSuccess(AuthResult authResult) {


                                                mDatabaseReference = mFirebaseDatabase.getReference().push();
                                                final String pushId = mDatabaseReference.getKey();

                                                cv.put(doctorContract.doctorEntry.COLUMN_PUSHID, pushId);

                                                Toast.makeText(PatientSignup.this, "Firebase Success", Toast.LENGTH_SHORT).show();
                                                uri[0] = getContentResolver().insert(doctorContract.patientEntry.CONTENT_URI, cv);
                                                createUserInLocalDb(uri[0]);
                                                if(bmpByte == null){
                                                    mDatabaseReference = mDatabaseReference.child(charUtility.filterString(email)).child("patientsInfo");
                                                    mDatabaseReference.setValue(new patientsInfo(name, phone, email, password, dob, address, GENDER, null, pushId));
                                                    progressDialog.dismiss();
                                                }else {
                                                    mStorageReference = mFirebaseStorage.getReference().child(pushId);
                                                    mStorageReference = mStorageReference.child(charUtility.filterString(email)).child("patientsInfo");
                                                    UploadTask uploadTask = mStorageReference.putBytes(bmpByte);

                                                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                                        @Override
                                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                                            if (!task.isSuccessful()) {
                                                                throw task.getException();
                                                            }

                                                            // Continue with the task to get the download URL
                                                            return mStorageReference.getDownloadUrl();

                                                        }
                                                    })
                                                            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Uri> task) {
                                                                    if (task.isSuccessful()) {
                                                                        Uri downloadUri = task.getResult();

                                                                        mDatabaseReference = mDatabaseReference.child(charUtility.filterString(email)).child("patientsInfo");
                                                                        mDatabaseReference.setValue(new patientsInfo(name, phone, email, password,dob,address, GENDER, null, pushId));
                                                                    } else {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(PatientSignup.this, "Error Uploading Image", Toast.LENGTH_LONG).show();
                                                                    }

                                                                }
                                                            });
                                                }
                                                createUserInFirebase(email);
                                                uploadUserPicIfHas(pushId, email);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String message = e.getMessage();
                                        progressDialog.dismiss();
                                        Toast.makeText(PatientSignup.this, message,Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                });

                            }
                        }
                    }
                }
            }
        }
    }

        }



        //For Radio button conversion to int 0 or 1
    public void onRadioClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()){
            case R.id.maleRadio:
                if(checked){
                    Toast.makeText(this, "Male", Toast.LENGTH_LONG).show();
                    GENDER = 0;
                }
                break;
            case R.id.femaleRadio:
                if(checked){
                    Toast.makeText(this, "Female", Toast.LENGTH_LONG).show();
                    GENDER = 1;
                }
                break;
        }
    }

    //Creating Patient in local db
    private void createUserInLocalDb(Uri uri) {
        if(uri == null){
            Toast.makeText(this, "Patient Already Exist", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "Patient Created Successfully" ,Toast.LENGTH_SHORT).show();

            patientDbHelper.createPatientDb(this, usermail);
            finish();

            Intent intent = new Intent(this, PatientLogin.class);
            startActivity(intent);
        }
    }

    //For Email Verification
    private void createUserInFirebase(String useremail) {
        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification();
        Toast.makeText(this, "Email Verification Link has been Sent to: " + useremail, Toast.LENGTH_LONG).show();
    }

    //For Uploading Photo **Currently not working
    private void uploadUserPicIfHas(String pushId, String useremail) {
    }


}
