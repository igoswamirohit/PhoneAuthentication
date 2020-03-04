package com.example.phoneauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private TextView mErrTextView;
    private TextView mPhoneNumber;
    private Button mGenerateOtp;
    private ProgressBar mProgressBar;

    private Spinner spinner;

    public String cc;
    public String pn;
    public String complete_pn;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        mPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        mGenerateOtp = (Button) findViewById(R.id.generateOtp);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mErrTextView = (TextView) findViewById(R.id.errTextView);
        spinner = (Spinner) findViewById(R.id.spinner);

        /*ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("+91");
        arrayList.add("+1");*/

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(LoginActivity.this,R.array.number_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cc = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cc = "+91";
            }
        });
        mGenerateOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pn = mPhoneNumber.getText().toString();
                complete_pn = cc + "" + pn;
                if(pn.isEmpty()){
                    mErrTextView.setVisibility(View.VISIBLE);
                    mErrTextView.setText("Please Fill in the form to Continue");
                }else{
                    mProgressBar.setVisibility(View.VISIBLE);
                    mGenerateOtp.setEnabled(false);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            complete_pn,
                            60,
                            TimeUnit.SECONDS,
                            LoginActivity.this,
                            mCallbacks
                    );
                }

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(LoginActivity.this, "Verification Failed, Kindly try again.", Toast.LENGTH_SHORT).show();
                //mGenerateOtp.setEnabled(true);
                e.printStackTrace();
                Log.e("Firebase error", e.getMessage());
            }

            @Override
            public void onCodeSent(final String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                new android.os.Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                Intent otpintent = new Intent(LoginActivity.this,OtpActivity.class);
                                otpintent.putExtra("AuthCredentials",s);
                                startActivity(otpintent);
                            }
                        }
                ,1000);
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mCurrentUser != null)
        {
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendUserToHome();
                            // ...
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(LoginActivity.this, "Wrong Otp", Toast.LENGTH_SHORT).show();
                                mGenerateOtp.setEnabled(true);
                            }
                        }
                    }
                });
    }

    public void sendUserToHome(){
            Intent homeIntent = new Intent(LoginActivity.this, MainActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
    }
}