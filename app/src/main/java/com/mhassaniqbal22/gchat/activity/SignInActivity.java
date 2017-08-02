package com.mhassaniqbal22.gchat.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mhassaniqbal22.gchat.R;


public class SignInActivity extends AppCompatActivity {

    private EditText etEmail, etPass;
    private CheckBox cbShowPass;
    private Button btSignIn, btRegister;

    private String email, pass;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_pass);
        cbShowPass = findViewById(R.id.cb_show_pass);
        btSignIn = findViewById(R.id.bt_sign_in);
        btRegister = findViewById(R.id.bt_register);

        firebaseAuth = FirebaseAuth.getInstance();

        cbShowPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    etPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    etPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });

        btRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignIn();
            }
        });

    }

    private void attemptSignIn() {
        etEmail.setError(null);
        etPass.setError(null);

        email = etEmail.getText().toString();
        pass = etPass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(pass)) {
            etPass.setError(getString(R.string.error_field_required));
            focusView = etPass;
            cancel = true;
        } else if (!isPasswordValid(pass)) {
            etPass.setError(getString(R.string.error_invalid_password));
            focusView = etPass;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.error_field_required));
            focusView = etEmail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            etEmail.setError(getString(R.string.error_invalid_email));
            focusView = etEmail;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            getUser();
        }
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String pass) {
        return pass.length() > 5;
    }

    private void RunProgressDialog(String s) {
        progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(s);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();

    }

    private void getUser() {
        RunProgressDialog(getString(R.string.please_wait));

        firebaseAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}
