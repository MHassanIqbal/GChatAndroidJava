package com.mhassaniqbal22.gchat.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mhassaniqbal22.gchat.R;

import org.json.JSONObject;


public class SignUpActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPass, etConfirmPass;
    private Button btSignUp, btLogin;
    private CheckBox cbShowPass, cbShowConfirmPass;

    private String name, email, pass, confirmPass;

    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPass = findViewById(R.id.et_pass);
        etConfirmPass = findViewById(R.id.et_confirm_pass);
        cbShowPass = findViewById(R.id.cb_show_pass);
        cbShowConfirmPass = findViewById(R.id.cb_show_confirm_pass);
        btSignUp = findViewById(R.id.bt_sign_up);
        btLogin = findViewById(R.id.bt_login);

        databaseReference = FirebaseDatabase.getInstance().getReference();
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

        cbShowConfirmPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    etConfirmPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    etConfirmPass.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });


        btSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignUp();
            }
        });

        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
                finish();
            }
        });
    }

    private void attemptSignUp() {
        etName.setError(null);
        etEmail.setError(null);
        etPass.setError(null);
        etConfirmPass.setError(null);

        name = etName.getText().toString();
        email = etEmail.getText().toString();
        pass = etPass.getText().toString();
        confirmPass = etConfirmPass.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(confirmPass)) {
            etConfirmPass.setError(getString(R.string.error_field_required));
            focusView = etConfirmPass;
            cancel = true;
        } else if (!isConfirmPassValid(confirmPass)) {
            etConfirmPass.setError(getString(R.string.error_invalid_confirm_password));
            focusView = etConfirmPass;
            cancel = true;
        }

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

        if (TextUtils.isEmpty(name)) {
            etName.setError(getString(R.string.error_field_required));
            focusView = etName;
            cancel = true;
        } else if (!isNameValid(name)) {
            etName.setError(getString(R.string.error_invalid_name));
            focusView = etName;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            createNewUser();
        }
    }

    private boolean isNameValid(String name) {
        return name.matches("[ A-Za-z0-9]+");
    }

    private boolean isEmailValid(String username) {
        return username.contains("@");
    }

    private boolean isPasswordValid(String pass) {
        return pass.length() > 5;
    }

    private boolean isConfirmPassValid(String confirmPass) {
        return confirmPass.equals(pass);
    }

    private void startProgressDialog(String s) {
        progressDialog = new ProgressDialog(SignUpActivity.this);
        progressDialog.setTitle(getString(R.string.loading));
        progressDialog.setMessage(s);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void createNewUser() {
        startProgressDialog(getString(R.string.please_wait));
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            progressDialog.dismiss();
                            Toast.makeText(SignUpActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                        } else {
                            updateUser();
                            Toast.makeText(SignUpActivity.this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                            writeUserOnDb();
                        }
                    }
                });

    }

    private void updateUser() {
        firebaseUser = firebaseAuth.getCurrentUser();

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(Uri.parse(""))
                .build();
        firebaseUser.updateProfile(profileChangeRequest);
    }

    private void startMainActivity() {
        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
        finish();
    }

    private void writeUserOnDb() {
        progressDialog.dismiss();
        startProgressDialog(getString(R.string.adding_user));

        String url = "https://gchat-3a570.firebaseio.com/users.json";
        databaseReference = firebaseDatabase.getReference("user");
        firebaseUser = firebaseAuth.getCurrentUser();
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        if (s.isEmpty()) {
                            databaseReference.child(firebaseUser.getUid()).child("id").setValue(firebaseUser.getUid());
                            databaseReference.child(firebaseUser.getUid()).child("name").setValue(firebaseUser.getDisplayName());
                            databaseReference.child(firebaseUser.getUid()).child("email").setValue(firebaseUser.getEmail());
                            databaseReference.child(firebaseUser.getUid()).child("picture").setValue(firebaseUser.getPhotoUrl());

                        } else {
                            JSONObject object = new JSONObject();
                            if (!object.has(firebaseUser.getUid())) {
                                databaseReference.child(firebaseUser.getUid()).child("id").setValue(firebaseUser.getUid());
                                databaseReference.child(firebaseUser.getUid()).child("name").setValue(name);
                                databaseReference.child(firebaseUser.getUid()).child("email").setValue(email);
                                databaseReference.child(firebaseUser.getUid()).child("picture").setValue("");
                            } else {
                                Toast.makeText(SignUpActivity.this, "User Already Exist", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("" + error);
                progressDialog.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(SignUpActivity.this);
        rQueue.add(request);

        startMainActivity();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
        finish();
    }
}
