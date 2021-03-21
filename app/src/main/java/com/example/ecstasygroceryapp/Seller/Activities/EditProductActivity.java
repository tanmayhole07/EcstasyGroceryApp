package com.example.ecstasygroceryapp.Seller.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecstasygroceryapp.CommonActivities.LoginActivity;
import com.example.ecstasygroceryapp.Constants;
import com.example.ecstasygroceryapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProductActivity extends AppCompatActivity {

    private ImageView productIconIv;
    private EditText titleEt, descriptionEt, quantityEt, priceEt, discountPriceEt, discountNoteEt;
    private TextView categoryTv ;
    private SwitchCompat discountSwitch;
    private Button updateProductBtn;

    FloatingActionButton fabStep1Complete, fabStep2Prev;

    ConstraintLayout layout_add_product_1, layout_add_product_2;

    TextInputLayout textInputDiscountPriceEt, textInputDiscountNoteEt;

    private String productId;

    //permission constants
    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;

    //image pick constants
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    //permission arrays
    private String[] cameraPermissions;
    private String[] storagePermissions;


    private Uri image_uri;
    private ProgressDialog pd;
    String mUID = "uid";

    //Firebase Variables
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_product);

        productIconIv = findViewById(R.id.productIconIv);
        titleEt = findViewById(R.id.titleEt);
        descriptionEt = findViewById(R.id.descriptionEt);
        quantityEt = findViewById(R.id.quantityEt);
        discountPriceEt = findViewById(R.id.discountPriceEt);
        priceEt = findViewById(R.id.priceEt);
        discountNoteEt = findViewById(R.id.discountNoteEt);
        categoryTv = findViewById(R.id.categoryTv);
        discountSwitch = findViewById(R.id.discountSwitch);

        fabStep1Complete = findViewById(R.id.fabStep1Complete);
        fabStep2Prev = findViewById(R.id.fabStep2Prev);
        updateProductBtn = findViewById(R.id.addProductBtn);

        textInputDiscountPriceEt = findViewById(R.id.textInputDiscountPriceEt);
        textInputDiscountNoteEt = findViewById(R.id.textInputDiscountNoteEt);

        layout_add_product_1 = findViewById(R.id.layout_add_product_1);
        layout_add_product_2 = findViewById(R.id.layout_add_product_2);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView textView = (TextView) toolbar.findViewById(R.id.toolbarTextView);
        textView.setText("Ecstasy Business");

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        productId = getIntent().getStringExtra("productId");

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        discountPriceEt.setVisibility(View.GONE);
        discountNoteEt.setVisibility(View.GONE);
        textInputDiscountPriceEt.setVisibility(View.GONE);
        textInputDiscountNoteEt.setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();

        layout1();
        checkUserStatus();


        pd = new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    discountPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);
                    textInputDiscountPriceEt.setVisibility(View.VISIBLE);
                    textInputDiscountNoteEt.setVisibility(View.VISIBLE);
                }
                else {
                    discountPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                    textInputDiscountPriceEt.setVisibility(View.GONE);
                    textInputDiscountNoteEt.setVisibility(View.GONE);
                }
            }
        });


        productIconIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePicDialog();
            }
        });

        categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        fabStep1Complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData1();

            }
        });

        updateProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputData2();
            }
        });

        fabStep2Prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout1();
            }
        });

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load Products From Database //

    private void loadProductDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        String productId = ""+snapshot.child("productId").getValue();
                        String productTitle = ""+snapshot.child("productTitle").getValue();
                        String productDescription = ""+snapshot.child("productDescription").getValue();
                        String productCategory = ""+snapshot.child("productCategory").getValue();
                        String productQuantity = ""+snapshot.child("productQuantity").getValue();
                        String productIcon = ""+snapshot.child("productIcon").getValue();
                        String originalPrice = ""+snapshot.child("originalPrice").getValue();
                        String discountPrice = ""+snapshot.child("discountPrice").getValue();
                        String discountNote = ""+snapshot.child("discountNote").getValue();
                        String discountAvailable = ""+snapshot.child("discountAvailable").getValue();
                        String timeStamp = ""+snapshot.child("timeStamp").getValue();
                        String uid = ""+snapshot.child("uid").getValue();

                        if (discountAvailable.equals("true")){
                            discountSwitch.setChecked(true);

                            discountPriceEt.setVisibility(View.VISIBLE);
                            discountNoteEt.setVisibility(View.VISIBLE);
                        }else {
                            discountSwitch.setChecked(false);

                            discountPriceEt.setVisibility(View.GONE);
                            discountNoteEt.setVisibility(View.GONE);
                        }

                        titleEt.setText(productTitle);
                        descriptionEt.setText(productDescription);
                        categoryTv.setText(productCategory);
                        discountNoteEt.setText(discountNote);
                        quantityEt.setText(productQuantity);
                        priceEt.setText(originalPrice);
                        discountPriceEt.setText(discountPrice);

                        try {
                            Picasso.get().load(productIcon).placeholder(R.drawable.add_product_image).into(productIconIv);
                        }catch (Exception e){
                            productIconIv.setImageResource(R.drawable.add_product_image);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add Products to Database //

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData1() {

        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(EditProductActivity.this, "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(EditProductActivity.this, "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        layout2();
    }

    private void inputData2() {

        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(EditProductActivity.this, "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(EditProductActivity.this, "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(EditProductActivity.this, "Price is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountAvailable) {
            discountPrice = discountPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(EditProductActivity.this, "Discount Price is required is required...", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            discountPrice = "0";
            discountNote = "";
        }

        updateProduct();

    }

    private void updateProduct() {
        pd.setMessage("Updating product");
        pd.show();

        if (image_uri==null){

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountAvailable", "" + discountAvailable);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                    .updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }else {

            String filePathAndName = "product_images/" +""+productId;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()){
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productIcon", "" + downloadImageUri);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(productId)
                                        .updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(EditProductActivity.this, "Updated...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(EditProductActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });

        }
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Category selection //

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Product Category")
                .setItems(Constants.productCategories, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String category = Constants.productCategories[i];

                        categoryTv.setText(category);
                    }
                }).show();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Camera & Storage permissions, requests and setting Data//

    private void showImagePicDialog() {

        String[] options = {"Camera", "Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            //camera clicked
                            if (checkCameraPermission()){
                                pickFromCamera();
                            }else {
                                requestCameraPermission();
                            }
                        }else {
                            //gallery clicked
                            if (checkStoragePermission()){
                                pickFromGallery();
                            }else {
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();

    }

    private void pickFromCamera(){
//        ContentValues cv = new ContentValues();
//        cv.put(MediaStore.Images.Media.TITLE,"Temp_Image Title");
//        cv.put(MediaStore.Images.Media.DESCRIPTION,"Temp_Image Description");
//
//        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
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

    private void pickFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Permission request and Activity result//


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){

            case CAMERA_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean cameraAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }else {
                        Toast.makeText(this, "Camera permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if(grantResults.length>0){
                    boolean storageAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }else {
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
                productIconIv.setImageURI(image_uri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception e = result.getError();
                Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                productIconIv.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "" + error, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void layout1() {
        layout_add_product_1.setVisibility(View.VISIBLE);
        layout_add_product_2.setVisibility(View.GONE);
    }

    private void layout2() {
        layout_add_product_2.setVisibility(View.VISIBLE);
        layout_add_product_1.setVisibility(View.GONE);
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            loadProductDetails();
            updateProductBtn.setText("Update Product");

        } else {
            startActivity(new Intent(EditProductActivity.this, LoginActivity.class));
            EditProductActivity.this.finish();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}