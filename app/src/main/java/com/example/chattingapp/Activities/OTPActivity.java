package com.example.chattingapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chattingapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    private EditText et_otp;
    private Button btn_verifyOtp;
    private TextView phoneLabel,resendOtp;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpactivity);
        getSupportActionBar().hide();

        phoneLabel=findViewById(R.id.phoneLabel);
        et_otp=findViewById(R.id.et_otp);
        resendOtp=findViewById(R.id.resendOtp);
        btn_verifyOtp=findViewById(R.id.btn_verifyOtp);
        progressBar=findViewById(R.id.progressBar);
        auth=FirebaseAuth.getInstance();

//........................Receiving Phone number from previous activity..........................
        String phoneNumber=getIntent().getStringExtra("mobile");
        phoneLabel.setText("Verify +92"+phoneNumber);

        verificationId=getIntent().getStringExtra("VerificationId");
//................................................................................................

//....................Continue Buttton Listener...................................................
        btn_verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_otp.getText().toString().trim().isEmpty()) {
                    Toast.makeText(OTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(verificationId!=null){

                    progressBar.setVisibility(View.VISIBLE);
                    btn_verifyOtp.setVisibility(View.INVISIBLE);

                    PhoneAuthCredential phoneAuthCredential=PhoneAuthProvider.getCredential(
                            getIntent().getStringExtra("VerificationId").toString()
                            ,et_otp.getText().toString());

                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        progressBar.setVisibility(View.GONE);
                                        btn_verifyOtp.setVisibility(View.VISIBLE);
                                        Intent intent=new Intent(OTPActivity.this,SetupProfileActivity.class);
                                        startActivity(intent);
                                        finishAffinity();
                                    }
                                    else{
                                        progressBar.setVisibility(View.INVISIBLE);
                                        btn_verifyOtp.setVisibility(View.VISIBLE);
                                        Toast.makeText(OTPActivity.this, "Verification code entered was invalid", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

//......................Resend OTP Listener..........................................................
        resendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone="+92"+getIntent().getStringExtra("mobile");

                PhoneAuthProvider.getInstance().verifyPhoneNumber(phone,
                        60, TimeUnit.SECONDS,
                        OTPActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                            }

                            @Override
                            public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                verificationId=newVerificationId;
                                Toast.makeText(OTPActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

            }
        });
//................................................................................................

    }

}