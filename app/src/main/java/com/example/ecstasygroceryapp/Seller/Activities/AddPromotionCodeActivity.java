package com.example.ecstasygroceryapp.Seller.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecstasygroceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

public class AddPromotionCodeActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    ProgressDialog pd;
    private ImageView backBtn, imageIv;
    private EditText promoCodeEt, promoDescriptionEt, promoPriceEt, minimumOrderPriceEt;
    private TextView expireDateTv, toolbarTextView;
    private Button addBtn;
    private String promoId;
    private boolean isUpdating = false;
    private String description, promoCode, promoPrice, minimumOrderPrice, expireDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_promotion_code);

        backBtn = findViewById(R.id.backBtn);
        imageIv = findViewById(R.id.imageIv);
        promoCodeEt = findViewById(R.id.promoCodeEt);
        promoDescriptionEt = findViewById(R.id.promoDescriptionEt);
        promoPriceEt = findViewById(R.id.promoPriceEt);
        minimumOrderPriceEt = findViewById(R.id.minimumOrderPriceEt);
        expireDateTv = findViewById(R.id.expireDateTv);
        addBtn = findViewById(R.id.addBtn);
        toolbarTextView = findViewById(R.id.toolbarTextView);

        firebaseAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);

        Intent intent = getIntent();
        if (intent.getStringExtra("promoId") != null) {
            promoId = intent.getStringExtra("promoId");

            toolbarTextView.setText("Update Promotion Code");
            addBtn.setText("Update");
            isUpdating = true;
            loadPromoInfo();
        } else {


            toolbarTextView.setText("Add Promotion Code");
            addBtn.setText("Add");
            isUpdating = false;
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        expireDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickDateDialog();
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });
    }

    private void loadPromoInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String id = "" + snapshot.child("id").getValue();
                        String timestamp = "" + snapshot.child("timeStamp").getValue();
                        String description = "" + snapshot.child("description").getValue();
                        String promoCode = "" + snapshot.child("promoCode").getValue();
                        String promoPrice = "" + snapshot.child("promoPrice").getValue();
                        String minimumOrderPrice = "" + snapshot.child("minimumOrderPrice").getValue();
                        String expireDate = "" + snapshot.child("expireDate").getValue();

                        promoCodeEt.setText(promoCode);
                        promoDescriptionEt.setText(description);
                        promoPriceEt.setText(promoPrice);
                        minimumOrderPriceEt.setText(minimumOrderPrice);
                        expireDateTv.setText(expireDate);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void pickDateDialog() {

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

                DecimalFormat mFormat = new DecimalFormat("00");
                String pDay = mFormat.format(dayOfMonth);
                String pMonth = mFormat.format(monthOfYear);
                String pYear = "" + year;
                String pDate = pDay + "/" + pMonth + "/" + pYear;

                expireDateTv.setText(pDate);

            }
        }, mYear, mMonth, mDay);

        datePickerDialog.show();
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
    }

    private void inputData() {

        promoCode = promoCodeEt.getText().toString().trim();
        description = promoDescriptionEt.getText().toString().trim();
        promoPrice = promoPriceEt.getText().toString().trim();
        minimumOrderPrice = minimumOrderPriceEt.getText().toString().trim();
        expireDate = expireDateTv.getText().toString().trim();

        if (TextUtils.isEmpty(promoCode)) {
            Toast.makeText(this, "Enter discount code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "Enter code description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(promoPrice)) {
            Toast.makeText(this, "Enter code price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(minimumOrderPrice)) {
            Toast.makeText(this, "Enter minimum order price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(expireDate)) {
            Toast.makeText(this, "Select expire date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isUpdating) {
            updateDataToDb();
        } else {
            addToDb();
        }

    }

    private void updateDataToDb() {

        pd.setMessage("Updating Promotion Code");
        pd.show();

        HashMap<String, Object> hashmap = new HashMap<>();
        hashmap.put("description", description + "");
        hashmap.put("promoCode", promoCode + "");
        hashmap.put("promoPrice", promoPrice + "");
        hashmap.put("minimumOrderPrice", minimumOrderPrice + "");
        hashmap.put("expireDate", expireDate + "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(promoId)
                .updateChildren(hashmap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        pd.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void addToDb() {

        pd.setMessage("Adding Promotion Code");
        pd.show();

        String timestamp = "" + System.currentTimeMillis();

        HashMap<String, Object> hashmap = new HashMap<>();
        hashmap.put("id", timestamp);
        hashmap.put("timeStamp", "" + timestamp);
        hashmap.put("description", description + "");
        hashmap.put("promoCode", promoCode + "");
        hashmap.put("promoPrice", promoPrice + "");
        hashmap.put("minimumOrderPrice", minimumOrderPrice + "");
        hashmap.put("expireDate", expireDate + "");

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions").child(timestamp)
                .setValue(hashmap)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        Toast.makeText(AddPromotionCodeActivity.this, "Promotion Code Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(AddPromotionCodeActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}