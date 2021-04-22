package com.example.ecstasygroceryapp.CommonActivities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.Seller.Activities.DashboardSellerActivity;
import com.example.ecstasygroceryapp.User.Activities.DashboardUserActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    EditText registerNameEt, registerPhoneEt, registerEmailEt, registerPasswordEt;
    Button registerButton;
    TextView haveAccountTv;

    EditText loginName, loginPassword;
    Button loginButton;
    TextView dontHaveAccountTv, forgotPassTv;

    String mUID = "uid";

    private ConstraintLayout loginRl;
    private ConstraintLayout registerRl;
    private ProgressDialog pd;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginRl = findViewById(R.id.loginRl);
        registerRl = findViewById(R.id.registerRl);

        registerNameEt = findViewById(R.id.registerNameEt);
        registerPhoneEt = findViewById(R.id.registerPhoneEt);
        registerEmailEt = findViewById(R.id.registerEmailEt);
        registerPasswordEt = findViewById(R.id.registerPasswordEt);
        registerButton = findViewById(R.id.registerButton);
        haveAccountTv = findViewById(R.id.haveAccountTv);

        loginName = findViewById(R.id.loginName);
        loginPassword = findViewById(R.id.loginPassword);
        loginButton = findViewById(R.id.loginButton);
        dontHaveAccountTv = findViewById(R.id.dontHaveAccountTv);
        forgotPassTv = findViewById(R.id.forgotPassTv);

        firebaseAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setTitle("Please wait");
        pd.setCanceledOnTouchOutside(false);

        //////////////////////////////////////////////////////////////

        forgotPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRecoverPasswordDailog();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoginUi();
            }
        });

        //////////////////////////////////////////////////////////////

        dontHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterUi();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputRegisterData();
            }
        });

        showLoginUi();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                  Recover User Password User               //

    private void showRecoverPasswordDailog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout = new LinearLayout(this);
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void beginRecovery(String email) {
        pd.setMessage("Sending email");
        pd.show();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pd.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                  Login User               //

    private String emailLogin, passwordLogin;

    private void loginUser() {
        emailLogin = loginName.getText().toString().trim();
        passwordLogin = loginPassword.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(emailLogin).matches()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            loginName.setFocusable(true);
            return;
        }

        if (TextUtils.isEmpty(passwordLogin)) {
            Toast.makeText(this, "Enter Password", Toast.LENGTH_SHORT).show();
            loginPassword.setFocusable(true);
            return;
        }

        if (passwordLogin.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long...", Toast.LENGTH_SHORT).show();
            loginPassword.setFocusable(true);
            return;
        }

        pd.setMessage("Logging In");
        pd.show();

        firebaseAuth.signInWithEmailAndPassword(emailLogin, passwordLogin)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        makeMeOnline();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                  Register User               //

    private String fullName, phoneNumber, email, password;

    private void inputRegisterData() {

        fullName = registerNameEt.getText().toString();
        phoneNumber = registerPhoneEt.getText().toString();
        email = registerEmailEt.getText().toString();
        password = registerPasswordEt.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Your Name", Toast.LENGTH_SHORT).show();
            registerNameEt.setFocusable(true);
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Mobile Number", Toast.LENGTH_SHORT).show();
            registerPhoneEt.setFocusable(true);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
            registerEmailEt.setFocusable(true);
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long...", Toast.LENGTH_SHORT).show();
            registerPasswordEt.setFocusable(true);
            return;
        }

        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            registerPasswordEt.setFocusable(true);
            return;
        }

        createAccount();

    }

    private void createAccount() {

        pd.setMessage("Creating Account");
        pd.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {

        pd.setMessage("Saving Account Info");
        final String timeStamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + firebaseAuth.getUid());
        hashMap.put("email", "" + email);
        hashMap.put("name", "" + fullName);
        hashMap.put("phone", "" + phoneNumber);
        hashMap.put("accountTye", "User");
        hashMap.put("timeStamp", timeStamp);
        hashMap.put("country", "");
        hashMap.put("postalCode", "");
        hashMap.put("city", "");
        hashMap.put("address", "");
        hashMap.put("latitude", "");
        hashMap.put("longitude", "");
        hashMap.put("online", "true");
        hashMap.put("profileImage", "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                        finish();
                    }
                });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void showLoginUi() {
        loginRl.setVisibility(View.VISIBLE);
        registerRl.setVisibility(View.GONE);
    }

    private void showRegisterUi() {
        loginRl.setVisibility(View.GONE);
        registerRl.setVisibility(View.VISIBLE);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////


    private void makeMeOnline() {
        pd.setMessage("Checking User");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "true");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        checkUserType();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserType() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()){
                            String accountTye = ""+ds.child("accountTye").getValue();
                            if (accountTye.equals("Seller")){
                                pd.dismiss();
                                startActivity(new Intent(LoginActivity.this, DashboardSellerActivity.class));
                                finish();
                            }
                            else {
                                pd.dismiss();
                                startActivity(new Intent(LoginActivity.this, DashboardUserActivity.class));
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        //finishAndRemoveTask();
        finish();
        super.onBackPressed();
    }
}