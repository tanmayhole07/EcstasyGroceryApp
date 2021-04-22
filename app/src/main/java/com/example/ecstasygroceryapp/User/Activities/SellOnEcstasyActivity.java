package com.example.ecstasygroceryapp.User.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecstasygroceryapp.CommonActivities.LoginActivity;
import com.example.ecstasygroceryapp.Seller.Activities.DashboardSellerActivity;
import com.example.ecstasygroceryapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SellOnEcstasyActivity extends AppCompatActivity {

    ImageView profileIv;
    EditText nameEt, emailEt, passwordEt, phoneEt, shopNameEt, deliveryFeeEt, countryEt, postalCodeEt, cityEt, addressEt;
    Button registerSellerButton;
    TextView gpsTv;

    ConstraintLayout layout_register_seller_1, layout_register_seller_2, layout_register_seller_3;

    FirebaseAuth firebaseAuth;
    String mUID = "uid";
    private ProgressDialog pd;

    private Uri image_uri;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 200;
    private static final int IMAGE_PICK_GALLERY_CODE = 300;
    private static final int IMAGE_PICK_CAMERA_CODE = 400;

    //permission arrays
    String cameraPermission[];
    String storagePermissions[];

    FloatingActionButton fabStep1Complete, fabStep2Complete, fabStep2Prev, fabStep3Prev;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell_on_ecstasy);

        profileIv = findViewById(R.id.profileIv);//
        nameEt = findViewById(R.id.nameEt);//
        emailEt = findViewById(R.id.emailEt);//
        phoneEt = findViewById(R.id.phoneEt);//
        shopNameEt = findViewById(R.id.shopNameEt);//
        deliveryFeeEt = findViewById(R.id.deliveryFeeEt);//
        passwordEt = findViewById(R.id.passwordEt);//
        countryEt = findViewById(R.id.countryEt);//
        postalCodeEt = findViewById(R.id.postalCodeEt);//
        cityEt = findViewById(R.id.cityEt);//
        addressEt = findViewById(R.id.addressEt);//
        registerSellerButton = findViewById(R.id.registerSellerButton);//
        gpsTv = findViewById(R.id.gpsTv);//

        fabStep1Complete = findViewById(R.id.fabStep1Complete);
        fabStep2Complete = findViewById(R.id.fabStep2Complete);
        fabStep2Prev = findViewById(R.id.fabStep2Prev);
        fabStep3Prev = findViewById(R.id.fabStep3Prev);

        layout_register_seller_1 = findViewById(R.id.layout_register_seller_1);
        layout_register_seller_2 = findViewById(R.id.layout_register_seller_2);
        layout_register_seller_3 = findViewById(R.id.layout_register_seller_3);


        pd = new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                profileIv.setImageResource(R.drawable.ic_store_grey);
                showImagePicDialog();
            }
        });

        gpsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                turnOnnLocation();
            }
        });

        registerSellerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData();
            }
        });

        fabStep1Complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDataStep1();

            }
        });

        fabStep2Complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputDataStep2();

            }
        });

        fabStep2Prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout1();
            }
        });

        fabStep3Prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout2();
            }
        });

        layout1();
        checkUserStatus();
        loadUserData();
    }


    private void layout1() {
        layout_register_seller_1.setVisibility(View.VISIBLE);
        layout_register_seller_2.setVisibility(View.GONE);
        layout_register_seller_3.setVisibility(View.GONE);
    }

    private void layout2() {
        layout_register_seller_2.setVisibility(View.VISIBLE);
        layout_register_seller_1.setVisibility(View.GONE);
        layout_register_seller_3.setVisibility(View.GONE);
    }

    private void layout3() {
        layout_register_seller_3.setVisibility(View.VISIBLE);
        layout_register_seller_2.setVisibility(View.GONE);
        layout_register_seller_1.setVisibility(View.GONE);
    }

    private String fullName, shopName, phoneNumber, deliveryFee, country, postalCode, city, address, email, password;

    private void inputDataStep1() {
        shopName = shopNameEt.getText().toString();
        deliveryFee = deliveryFeeEt.getText().toString();

        if (TextUtils.isEmpty(shopName)) {
            Toast.makeText(this, "Enter shop name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(deliveryFee)) {
            Toast.makeText(this, "Enter Delivery fee", Toast.LENGTH_SHORT).show();
            return;
        }

        layout2();
    }

    private void inputDataStep2() {

        country = countryEt.getText().toString();
        postalCode = postalCodeEt.getText().toString();
        city = cityEt.getText().toString();
        address = addressEt.getText().toString();

        if (latitude == 0.0 || longitude == 0.0) {
            Toast.makeText(this, "Gps turn on plz...", Toast.LENGTH_SHORT).show();

        }

        layout3();
    }

    private void inputData() {

        fullName = nameEt.getText().toString();

        phoneNumber = phoneEt.getText().toString();


        email = emailEt.getText().toString();
        password = passwordEt.getText().toString();

        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }


        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter phone Number", Toast.LENGTH_SHORT).show();
            return;
        }


        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long...", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(SellOnEcstasyActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        pd.setMessage("Saving Account Info");
        final String timeStamp = "" + System.currentTimeMillis();

        if (image_uri == null) {

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + firebaseAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + fullName);
            hashMap.put("shopName", "" + shopName);
            hashMap.put("phone", "" + phoneNumber);
            hashMap.put("deliveryFee", "" + deliveryFee);
            hashMap.put("country", "" + country);
            hashMap.put("postalCode", "" + postalCode);
            hashMap.put("city", "" + city);
            hashMap.put("address", "" + address);
            hashMap.put("latitude", "" + latitude);
            hashMap.put("longitude", "" + longitude);
            hashMap.put("timeStamp", timeStamp);
            hashMap.put("accountTye", "Seller");
            hashMap.put("online", "true");
            hashMap.put("shopOpen", "true");
            hashMap.put("profileImage", "");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            startActivity(new Intent(SellOnEcstasyActivity.this, DashboardSellerActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            startActivity(new Intent(SellOnEcstasyActivity.this, DashboardSellerActivity.class));
                            finish();
                        }
                    });

        } else {

            String filePathAndName = "profile_images/" + "" + firebaseAuth.getUid();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + firebaseAuth.getUid());
                                hashMap.put("email", "" + email);
                                hashMap.put("name", "" + fullName);
                                hashMap.put("shopName", "" + shopName);
                                hashMap.put("phone", "" + phoneNumber);
                                hashMap.put("deliveryFee", "" + deliveryFee);
                                hashMap.put("country", "" + country);
                                hashMap.put("postalCode", "" + postalCode);
                                hashMap.put("city", "" + city);
                                hashMap.put("address", "" + address);
                                hashMap.put("latitude", "" + latitude);
                                hashMap.put("longitude", "" + longitude);
                                hashMap.put("timeStamp", timeStamp);
                                hashMap.put("accountTye", "Seller");
                                hashMap.put("online", "true");
                                hashMap.put("shopOpen", "true");
                                hashMap.put("profileImage", "" + downloadImageUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                startActivity(new Intent(SellOnEcstasyActivity.this, DashboardSellerActivity.class));
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                startActivity(new Intent(SellOnEcstasyActivity.this, DashboardSellerActivity.class));
                                                finish();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(SellOnEcstasyActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void turnOnnLocation() {
        pd.setMessage("Accessing location");
        pd.show();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationUser();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private double latitude, longitude;

    private void getLocationUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {

                    try {
                        Geocoder geocoder = new Geocoder(SellOnEcstasyActivity.this, Locale.getDefault());

                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                        latitude = addresses.get(0).getLatitude();
                        longitude = addresses.get(0).getLongitude();

                        cityEt.setText(addresses.get(0).getLocality());
                        postalCodeEt.setText(addresses.get(0).getPostalCode());
                        countryEt.setText(addresses.get(0).getCountryName());

                        addressEt.setText(addresses.get(0).getAddressLine(0));

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                pd.dismiss();
                            }
                        }, 2000);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadUserData() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            //String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();

                            nameEt.setText(name);
                            //emailEt.setText(email);
                            phoneEt.setText(phone);


                            profileIv.setImageResource(R.drawable.ic_store_grey);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();

        } else {
            startActivity(new Intent(SellOnEcstasyActivity.this, LoginActivity.class));
            finish();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Camera & Storage permissions, requests and setting Data//

    private void showImagePicDialog() {

        String[] options = {"Camera", "Gallery"};
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(SellOnEcstasyActivity.this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            //camera clicked
                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            //gallery clicked
                            if (checkStoragePermission()) {
                                pickFromGallery();
                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }

    private void pickFromCamera() {
//        ContentValues cv = new ContentValues();
//        cv.put(MediaStore.Images.Media.TITLE, "Temp_Image Title");
//        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image Description");
//
//        image_uri = this.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
//        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
//                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(90, 90)
                .setActivityTitle("Crop Image")
                .setFixAspectRatio(true)
                .setCropMenuCropButtonTitle("Done")
                .start(this);
    }

    private void pickFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        requestPermissions(storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(cameraPermission, CAMERA_REQUEST_CODE);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Permission request and Activity result//


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case CAMERA_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Camera permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGallery();
                    } else {
                        Toast.makeText(this, "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == IMAGE_PICK_GALLERY_CODE) {
//                image_uri = data.getData();
//                eventPicIv.setImageURI(image_uri);

                image_uri = data.getData();
                CropImage.activity(image_uri)
                        .setGuidelines(CropImageView.Guidelines.ON)
//                        .setCropShape(CropImageView.CropShape.RECTANGLE)
                        .setAspectRatio(100, 100)
                        .setActivityTitle("Crop Image")
                        .setFixAspectRatio(true)
                        .setCropMenuCropButtonTitle("Done")
                        .start( this);


            }
//            else if (requestCode == IMAGE_PICK_CAMERA_CODE) {
//                eventPicIv.setImageURI(image_uri);
//            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                image_uri = result.getUri();
                profileIv.setImageURI(image_uri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                profileIv.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}