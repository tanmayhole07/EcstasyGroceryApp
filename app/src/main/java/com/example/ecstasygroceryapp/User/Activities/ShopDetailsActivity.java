package com.example.ecstasygroceryapp.User.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ecstasygroceryapp.Constants;
import com.example.ecstasygroceryapp.Models.ModelCartItem;
import com.example.ecstasygroceryapp.Models.ModelProduct;
import com.example.ecstasygroceryapp.Models.ModelReview;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Adapter.AdapterCartItem;
import com.example.ecstasygroceryapp.User.Adapter.AdapterProductUser;
import com.example.ecstasygroceryapp.User.Adapter.AdapterReview;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class ShopDetailsActivity extends AppCompatActivity {

    public String deliveryFee;
    private ImageView shopIv, shopInfoIv;
    private TextView shopNameTv, phoneTv, emailTv, openCloseTv,
            deliveryFeeTv, addressTv, filteredProductTv, cartCountTv;
    private ImageButton callBtn, mapBtn, cartBtn, filterProductBtn, reviewBtn;
    private EditText searchProductEt;
    private RecyclerView productsRv;
    private RatingBar ratingBar;


    private String shopUid;
    private String myLatitude, myLongitude;
    private String shopLatitude, shopLongitude, shopAddress, shopName, shopEmail, shopPhone;

    //firebase Variables
    FirebaseAuth firebaseAuth;

    private ArrayList<ModelProduct> productsList;
    private AdapterProductUser adapterProductUser;

    private ArrayList<ModelCartItem> cartItems;
    private AdapterCartItem adapterCartItem;
    private String myPhone;

    private ArrayList<ModelReview> reviewArrayList;
    private AdapterReview adapterReview;

    private ProgressDialog pd;

    ImageView backBtn;

    private EasyDB easyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        shopIv = findViewById(R.id.shopIv);
        shopNameTv = findViewById(R.id.shopNameTv);
        phoneTv = findViewById(R.id.phoneTv);
        emailTv = findViewById(R.id.emailTv);
//        openCloseTv = findViewById(R.id.openClosedTv);
        deliveryFeeTv = findViewById(R.id.deliveryFeeTv);
//        addressTv = findViewById(R.id.addressTv);
        filteredProductTv = findViewById(R.id.filteredProductTv);
//        callBtn = findViewById(R.id.callBtn);
//        mapBtn = findViewById(R.id.mapBtn);
        cartBtn = findViewById(R.id.cartBtn);
//        backBtn = findViewById(R.id.backBtn);
        filterProductBtn = findViewById(R.id.filterProductBtn);
        searchProductEt = findViewById(R.id.searchProductEt);
        productsRv = findViewById(R.id.productsRv);
        cartCountTv = findViewById(R.id.cartCountTv);
//        reviewBtn = findViewById(R.id.reviewBtn);
        ratingBar = findViewById(R.id.ratingBar);
        shopInfoIv = findViewById(R.id.shopInfoIv);

        backBtn = findViewById(R.id.backBtn);

        pd = new ProgressDialog(this);
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);

        shopUid = getIntent().getStringExtra("shopUid");
        firebaseAuth = FirebaseAuth.getInstance();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loadMyInfo();
        loadShopInfo();
        loadShopProducts();
        loadReviews();


        easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        deleteCartData();
        cartCount();


        shopInfoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ShopDetailsActivity.this, ShopInfoActivity.class);
                intent.putExtra("shopUid", shopUid);
                startActivity(intent);
            }
        });

        cartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCartDailog();
            }
        });

//        callBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                dialPhone();
//            }
//        });
//
//        mapBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                OpenMap();
//            }
//        });

        filterProductBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ShopDetailsActivity.this);
                builder.setTitle("Choose Category")
                        .setItems(Constants.productCategories1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String selected = Constants.productCategories1[i];
                                filteredProductTv.setText(selected);
                                if (selected.equals("All")) {
                                    loadShopProducts();
                                } else {
                                    adapterProductUser.getFilter().filter(selected);
                                }
                            }
                        }).show();
            }
        });

        searchProductEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

                try {
                    adapterProductUser.getFilter().filter(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        reviewBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                Intent intent = new Intent(ShopDetailsActivity.this, ShopReviewActivity.class);
////                intent.putExtra("shopUid", shopUid);
////                startActivity(intent);
//            }
//        });

    }

    private float ratimgSum = 0;

    private void loadReviews() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ratimgSum = 0;
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            float rating = Float.parseFloat("" + ds.child("ratings").getValue());
                            ratimgSum = ratimgSum + rating;


                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratimgSum / numberOfReviews;

//                        ratingBar.setRating(avgRating);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void deleteCartData() {

        easyDB.deleteAllDataFromTable();
    }

    public void cartCount() {
        int count = easyDB.getAllData().getCount();
        if (count <= 0) {
            cartCountTv.setVisibility(View.GONE);
        } else {
            cartCountTv.setVisibility(View.VISIBLE);
            cartCountTv.setText("" + count);
        }
    }

    public double allTotalPrice = 0.00;
    public TextView sTotalTv, dFeeTv, allTotalPriceTv;

    public EditText promoCodeEt;
    public FloatingActionButton validateBtn;
    public TextView promoDescriptionTv, discountTv;
    public Button applyBtn;

    private void showCartDailog() {

        cartItems = new ArrayList<>();

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cart, null);
        TextView shopNameTv = view.findViewById(R.id.shopNameTv);
        RecyclerView cartItemsRv = view.findViewById(R.id.cartItemsRv);
        sTotalTv = view.findViewById(R.id.sTotalTv);
        dFeeTv = view.findViewById(R.id.dFeeTv);
        allTotalPriceTv = view.findViewById(R.id.totalTv);
        TextView checkoutBtn = view.findViewById(R.id.checkoutBtn);


        promoCodeEt = view.findViewById(R.id.promoCodeEt);
        validateBtn = view.findViewById(R.id.validateBtn);
        promoDescriptionTv = view.findViewById(R.id.promoDescriptionTv);
        applyBtn = view.findViewById(R.id.applyBtn);
        discountTv = view.findViewById(R.id.discountTv);

        if (isPromoCodeApplied) {

            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            applyBtn.setText("Applied");
            promoCodeEt.setText(promoCode);
            promoDescriptionTv.setText(promoDescription);

        } else {
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("Apply");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        shopNameTv.setText(shopName);

        EasyDB easyDB = EasyDB.init(this, "ITEMS_DB")
                .setTableName("ITEMS_TABLE")
                .addColumn(new Column("Item_Id", new String[]{"text", "unique"}))
                .addColumn(new Column("Item_PID", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Name", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price_Each", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Price", new String[]{"text", "not null"}))
                .addColumn(new Column("Item_Quantity", new String[]{"text", "not null"}))
                .doneTableColumn();

        Cursor res = easyDB.getAllData();
        while (res.moveToNext()) {
            String id = res.getString(1);
            String pId = res.getString(2);
            String name = res.getString(3);
            String price = res.getString(4);
            String cost = res.getString(5);
            String quantity = res.getString(6);

            allTotalPrice = allTotalPrice + Double.parseDouble(cost);

            ModelCartItem modelCartItem = new ModelCartItem("" + id, "" + pId, "" + name, "" + price, "" + cost, "" + quantity);

            cartItems.add(modelCartItem);


        }

        adapterCartItem = new AdapterCartItem(this, cartItems, shopUid);

        cartItemsRv.setAdapter(adapterCartItem);

        if (isPromoCodeApplied) {
            priceWithDiscount();
        } else {
            priceWithoutDiscount();
        }

//        dFeeTv.setText("$" + deliveryFee);
//        sTotalTv.setText("$" + String.format("%.2f", allTotalPrice));
//        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                allTotalPrice = 0.00;
            }
        });


        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myLatitude.equals("") || myLatitude.equals("null") || myLongitude.equals("") || myLongitude.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (myPhone.equals("") || myPhone.equals("null")) {
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter your address in your profile before placing order...", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (cartItems.size() == 0) {
                    Toast.makeText(ShopDetailsActivity.this, "No item in cart", Toast.LENGTH_SHORT).show();
                    return;
                }

                submtOrder();
            }
        });

        validateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String promotionCode = promoCodeEt.getText().toString().trim();
                if (TextUtils.isEmpty(promotionCode)) {
                    Toast.makeText(ShopDetailsActivity.this, "Please Enter Promo code", Toast.LENGTH_SHORT).show();
                } else {
                    checkCodeAvailablity(promotionCode);
                }
            }
        });

        applyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPromoCodeApplied = true;
                applyBtn.setText("Applied");
                priceWithDiscount();
            }
        });

    }

    public boolean isPromoCodeApplied = false;
    public String promoId, promoTimestamp, promoCode, promoDescription, promoExpDate, promoMinimumOrderPrice, promoPrice;

    private void checkCodeAvailablity(String promotionCode) {

        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please wait");
        pd.setMessage("Checking Promo Code");
        pd.setCanceledOnTouchOutside(false);

        isPromoCodeApplied = false;
        applyBtn.setText("Apply");
        priceWithoutDiscount();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Promotions").orderByChild("promoCode").equalTo(promotionCode)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            pd.dismiss();
                            for (DataSnapshot ds : snapshot.getChildren()) {

                                promoId = "" + ds.child("id").getValue();
                                promoTimestamp = "" + ds.child("timestamp").getValue();
                                promoCode = "" + ds.child("promoCode").getValue();
                                promoDescription = "" + ds.child("description").getValue();
                                promoExpDate = "" + ds.child("expireDate").getValue();
                                promoMinimumOrderPrice = "" + ds.child("minimumOrderPrice").getValue();
                                promoPrice = "" + ds.child("promoPrice").getValue();

                                checkCodeExpireDate();
                            }
                        } else {
                            pd.dismiss();
                            Toast.makeText(ShopDetailsActivity.this, "Invalid promo code", Toast.LENGTH_SHORT).show();
                            applyBtn.setVisibility(View.GONE);
                            promoDescriptionTv.setVisibility(View.GONE);
                            promoDescriptionTv.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkCodeExpireDate() {

        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        String todayDate = mDay + "/" + mMonth + "/" + mYear;


        try {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            Date currentDate = simpleDateFormat.parse(todayDate);
            Date expireDate = simpleDateFormat.parse(promoExpDate);
            if (expireDate.compareTo(currentDate) > 0) {
                checkMinimumPrice();
            } else if (expireDate.compareTo(currentDate) < 0) {
                Toast.makeText(this, "Promotion Code expired on " + promoExpDate, Toast.LENGTH_SHORT).show();
                applyBtn.setVisibility(View.GONE);
                promoDescriptionTv.setVisibility(View.GONE);
                promoDescriptionTv.setText("");
            } else if (expireDate.compareTo(currentDate) > 0) {
                checkMinimumPrice();
            }

        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        }
    }

    private void checkMinimumPrice() {
        if (Double.parseDouble(String.format("%.2f", allTotalPrice)) < Double.parseDouble(promoMinimumOrderPrice)) {
            Toast.makeText(this, "This code is valid for order with minimum amount : $" + promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
            applyBtn.setVisibility(View.GONE);
            promoDescriptionTv.setVisibility(View.GONE);
            promoDescriptionTv.setText("");
        } else {
            applyBtn.setVisibility(View.VISIBLE);
            promoDescriptionTv.setVisibility(View.VISIBLE);
            promoDescriptionTv.setText(promoDescription);
        }
    }

    private void priceWithDiscount() {
        discountTv.setText("$" + promoPrice);
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$ " + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", "")) - Double.parseDouble(promoPrice)));

    }

    private void priceWithoutDiscount() {
        discountTv.setText("$0");
        dFeeTv.setText("$" + deliveryFee);
        sTotalTv.setText("$ " + String.format("%.2f", allTotalPrice));
        allTotalPriceTv.setText("$" + (allTotalPrice + Double.parseDouble(deliveryFee.replace("$", ""))));

    }

    private void submtOrder() {
        pd.setMessage("Placing order");
        pd.show();

        final String timestamp = "" + System.currentTimeMillis();
        String cost = allTotalPriceTv.getText().toString().trim().replace("$", "");

        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("orderId", "" + timestamp);
        hashMap.put("orderTime", "" + timestamp);
        hashMap.put("orderStatus", "In Progress");
        hashMap.put("orderCost", "" + cost);
        hashMap.put("orderBy", "" + firebaseAuth.getUid());
        hashMap.put("orderTo", "" + shopUid);
        hashMap.put("latitude", "" + myLatitude);
        hashMap.put("deliveryFee", deliveryFee);
        hashMap.put("longitude", "" + myLongitude);


        if (isPromoCodeApplied) {
            hashMap.put("discount", "" + promoPrice);
        } else {
            hashMap.put("discount", "0");
        }

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(shopUid).child("Orders");
        ref.child(timestamp).setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        for (int i = 0; i < cartItems.size(); i++) {
                            String productId = cartItems.get(i).getpId();
                            String id = cartItems.get(i).getId();
                            String cost = cartItems.get(i).getCost();
                            String name = cartItems.get(i).getName();
                            String price = cartItems.get(i).getPrice();
                            String quantity = cartItems.get(i).getQuantity();

                            HashMap<String, String> hashMap1 = new HashMap<>();
                            hashMap1.put("pId", productId);
                            hashMap1.put("name", name);
                            hashMap1.put("cost", cost);
                            hashMap1.put("price", price);
                            hashMap1.put("quantity", quantity);

                            ref.child(timestamp).child("Items").child(productId).setValue(hashMap1);

                        }
                        pd.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "Order Placed successfully", Toast.LENGTH_SHORT).show();

                        prepareNotificationMessage(timestamp);


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(ShopDetailsActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load Map and Phone //

    private void OpenMap() {
        String address = "https://maps.google.com/maps?saar=" + myLatitude + "," + myLongitude + "&daddr=" + shopLatitude + "," + shopLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void dialPhone() {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(shopPhone))));
        Toast.makeText(this, "" + shopPhone, Toast.LENGTH_SHORT).show();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load User , Shop and Shop Products Info //

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            myPhone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountTye = "" + ds.child("accountTye").getValue();
                            String city = "" + ds.child("city").getValue();
                            myLatitude = "" + ds.child("latitude").getValue();
                            myLongitude = "" + ds.child("longitude").getValue();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadShopInfo() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String name = "" + snapshot.child("name").getValue();
                shopName = "" + snapshot.child("shopName").getValue();
                shopEmail = "" + snapshot.child("email").getValue();
                shopPhone = "" + snapshot.child("phone").getValue();
                shopAddress = "" + snapshot.child("address").getValue();
                shopLatitude = "" + snapshot.child("latitude").getValue();
                shopLongitude = "" + snapshot.child("longitude").getValue();
                deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                String profileImage = "" + snapshot.child("profileImage").getValue();
                String shopOpen = "" + snapshot.child("shopOpen").getValue();

                shopNameTv.setText(shopName);
//                emailTv.setText(shopEmail);
//                deliveryFeeTv.setText("Delivery Fee: $" + deliveryFee);
//                addressTv.setText(shopAddress);
//                phoneTv.setText(shopPhone);
//                if (shopOpen.equals("true")){
//                    openCloseTv.setText("Open");
//                }else {
//                    openCloseTv.setText("Closed");
//                }
//
                try {
                    Picasso.get().load(profileImage).into(shopIv);
                } catch (Exception e) {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void loadShopProducts() {

        productsList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Products")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        productsList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelProduct modelProduct = ds.getValue(ModelProduct.class);
                            productsList.add(modelProduct);
                        }
                        adapterProductUser = new AdapterProductUser(ShopDetailsActivity.this, productsList);
                        productsRv.setAdapter(adapterProductUser);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void prepareNotificationMessage(String orderId) {

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "New Order " + orderId;
        String NOTIFICATION_MESSAGE = "Congratulations...! You have new order";
        String NOTIFICATION_TYPE = "NewOrder";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", firebaseAuth.getUid());
            notificationBodyJo.put("sellerUid", shopUid);
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);

        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo, orderId);
    }

    private void sendFcmNotification(JSONObject notificationJo, String orderId) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                // intent.putExtra("pId", productId);
                startActivity(intent);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Intent intent = new Intent(ShopDetailsActivity.this, OrderDetailsUserActivity.class);
                intent.putExtra("orderTo", shopUid);
                intent.putExtra("orderId", orderId);
                // intent.putExtra("pId", productId);
                startActivity(intent);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return headers;
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}