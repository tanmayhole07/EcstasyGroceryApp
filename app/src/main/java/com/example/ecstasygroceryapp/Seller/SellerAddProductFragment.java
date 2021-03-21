package com.example.ecstasygroceryapp.Seller;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SellerAddProductFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SellerAddProductFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SellerAddProductFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment sellerAddProductFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SellerAddProductFragment newInstance(String param1, String param2) {
        SellerAddProductFragment fragment = new SellerAddProductFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    ImageView productIconIv;
    EditText titleEt, descriptionEt, categoryTv, quantityEt, priceEt, discountPriceEt, discountNoteEt;
    FloatingActionButton fabStep1Complete, fabStep2Prev;
    Button addProductBtn;
    SwitchCompat discountSwitch;

    ConstraintLayout layout_add_product_1, layout_add_product_2;

    TextInputLayout textInputDiscountPriceEt, textInputDiscountNoteEt;

    FirebaseAuth firebaseAuth;
    String mUID = "uid";
    private ProgressDialog pd;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_add_product, container, false);

        pd = new ProgressDialog(getActivity());
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        productIconIv = view.findViewById(R.id.productIconIv);
        titleEt = view.findViewById(R.id.titleEt);
        descriptionEt = view.findViewById(R.id.descriptionEt);
        categoryTv = view.findViewById(R.id.categoryTv);
        quantityEt = view.findViewById(R.id.quantityEt);
        priceEt = view.findViewById(R.id.priceEt);
        discountPriceEt = view.findViewById(R.id.discountPriceEt);
        discountNoteEt = view.findViewById(R.id.discountNoteEt);
        fabStep1Complete = view.findViewById(R.id.fabStep1Complete);
        fabStep2Prev = view.findViewById(R.id.fabStep2Prev);
        addProductBtn = view.findViewById(R.id.addProductBtn);
        discountSwitch = view.findViewById(R.id.discountSwitch);
        textInputDiscountPriceEt = view.findViewById(R.id.textInputDiscountPriceEt);
        textInputDiscountNoteEt = view.findViewById(R.id.textInputDiscountNoteEt);

        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        layout_add_product_1 = view.findViewById(R.id.layout_add_product_1);
        layout_add_product_2 = view.findViewById(R.id.layout_add_product_2);

        discountPriceEt.setVisibility(View.GONE);
        discountNoteEt.setVisibility(View.GONE);
        textInputDiscountPriceEt.setVisibility(View.GONE);
        textInputDiscountNoteEt.setVisibility(View.GONE);

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

        addProductBtn.setOnClickListener(new View.OnClickListener() {
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

        discountSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    discountPriceEt.setVisibility(View.VISIBLE);
                    discountNoteEt.setVisibility(View.VISIBLE);
                    textInputDiscountPriceEt.setVisibility(View.VISIBLE);
                    textInputDiscountNoteEt.setVisibility(View.VISIBLE);

                } else {
                    discountPriceEt.setVisibility(View.GONE);
                    discountNoteEt.setVisibility(View.GONE);
                    textInputDiscountPriceEt.setVisibility(View.GONE);
                    textInputDiscountNoteEt.setVisibility(View.GONE);
                }
            }
        });

        layout1();
        checkUserStatus();
        return (view);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String productTitle, productDescription, productCategory, productQuantity, originalPrice, discountPrice, discountNote;
    private boolean discountAvailable = false;

    private void inputData1() {

        productTitle = titleEt.getText().toString().trim();
        productDescription = descriptionEt.getText().toString().trim();
        productCategory = categoryTv.getText().toString().trim();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(getActivity(), "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(getActivity(), "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        layout2();
    }

    private void inputData2() {

        productQuantity = quantityEt.getText().toString().trim();
        originalPrice = priceEt.getText().toString().trim();
        discountAvailable = discountSwitch.isChecked();

        if (TextUtils.isEmpty(productTitle)) {
            Toast.makeText(getActivity(), "Title is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(productCategory)) {
            Toast.makeText(getActivity(), "Category is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(originalPrice)) {
            Toast.makeText(getActivity(), "Price is required...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (discountAvailable) {
            discountPrice = discountPriceEt.getText().toString().trim();
            discountNote = discountNoteEt.getText().toString().trim();

            if (TextUtils.isEmpty(discountPrice)) {
                Toast.makeText(getActivity(), "Discount Price is required is required...", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            discountPrice = "0";
            discountNote = "";
        }

        addProduct();

    }

    private void addProduct() {

        pd.setMessage("Adding Product");
        pd.show();

        final String timeStamp = "" + System.currentTimeMillis();

        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();

            hashMap.put("productId", "" + timeStamp);
            hashMap.put("productTitle", "" + productTitle);
            hashMap.put("productDescription", "" + productDescription);
            hashMap.put("productCategory", "" + productCategory);
            hashMap.put("productQuantity", "" + productQuantity);
            hashMap.put("productIcon", ""); // no image, set empty
            hashMap.put("originalPrice", "" + originalPrice);
            hashMap.put("discountPrice", "" + discountPrice);
            hashMap.put("discountNote", "" + discountNote);
            hashMap.put("discountAvailable", "" + discountAvailable);
            hashMap.put("timeStamp", "" + timeStamp);
            hashMap.put("uid", "" + "" + firebaseAuth.getUid());

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
            ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Product added...", Toast.LENGTH_SHORT).show();
                            clearData();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            String filePathAndName = "product_images/" + "" + timeStamp;

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

                                hashMap.put("productId", "" + timeStamp);
                                hashMap.put("productTitle", "" + productTitle);
                                hashMap.put("productDescription", "" + productDescription);
                                hashMap.put("productCategory", "" + productCategory);
                                hashMap.put("productQuantity", "" + productQuantity);
                                hashMap.put("productIcon", "" + downloadImageUri); // no image, set empty
                                hashMap.put("originalPrice", "" + originalPrice);
                                hashMap.put("discountPrice", "" + discountPrice);
                                hashMap.put("discountNote", "" + discountNote);
                                hashMap.put("discountAvailable", "" + discountAvailable);
                                hashMap.put("timeStamp", "" + timeStamp);
                                hashMap.put("uid", "" + "" + firebaseAuth.getUid());

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                                ref.child(firebaseAuth.getUid()).child("Products").child(timeStamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "Product added...", Toast.LENGTH_SHORT).show();
                                                clearData();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                pd.dismiss();
                                                Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getActivity(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void clearData() {
        titleEt.setText("");
        titleEt.setText("");
        descriptionEt.setText("");
        categoryTv.setText("");
        quantityEt.setText("");
        priceEt.setText("");
        discountNoteEt.setText("");
        productIconIv.setImageResource(R.drawable.add_product_image);
        titleEt.setText("");
        image_uri = null;
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
            //loadUserData();

        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Category selection //

    private void categoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
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
//        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
//        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
//                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .setAspectRatio(100, 100)
                .setActivityTitle("Crop Image")
                .setFixAspectRatio(true)
                .setCropMenuCropButtonTitle("Done")
                .start(getContext(), this);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(getActivity(), storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean result = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(getActivity(), cameraPermissions, CAMERA_REQUEST_CODE);
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
                        Toast.makeText(getActivity(), "Camera permission is required...", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Storage permission is required...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

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
                        .start(getContext(), this);


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
                Toast.makeText(getActivity(), "" + e, Toast.LENGTH_SHORT).show();
            }

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                productIconIv.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getActivity(), "" + error, Toast.LENGTH_SHORT).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


}