package com.example.ecstasygroceryapp.User.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.ecstasygroceryapp.CommonActivities.LoginActivity;
import com.example.ecstasygroceryapp.Models.ModelOrderedItem;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Adapter.AdapterOrderedItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OrderDetailsUserActivity extends AppCompatActivity {

    private String orderTo, orderId, pId;

    private TextView orderIdTv, dateTv, orderStatusTv, shopNameTv, totalItemsTv, amountTv, addressTv, writeReviewBtn ;
    private RecyclerView itemsRv;
    ImageView backBtn;

    RelativeLayout tabDetailsRv, tabItemsRv, detailsBorderRv, itemsBorderRv;
    TextView detailsText, itemsText;

    private FirebaseAuth firebaseAuth;
    String mUID = "uid";
    private ProgressDialog pd;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    TextView tabOrderDetailsTv, tabOrderedProductsTv, editBtnTextRv;
    RelativeLayout orderDetailsRv, orderedProductsRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_user);

        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        shopNameTv = findViewById(R.id.shopNameTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);
        writeReviewBtn = findViewById(R.id.writeReviewBtn);
        backBtn = findViewById(R.id.backBtn);

        tabDetailsRv = findViewById(R.id.tabDetailsRv);
        tabItemsRv = findViewById(R.id.tabItemsRv);

        detailsBorderRv = findViewById(R.id.detailsBorderRv);
        itemsBorderRv = findViewById(R.id.itemsBorderRv);
        detailsText = findViewById(R.id.detailsText);
        itemsText = findViewById(R.id.itemsText);

        tabOrderDetailsTv = findViewById(R.id.tabOrderDetailsTv);
        tabOrderedProductsTv = findViewById(R.id.tabOrderedProductsTv);
        orderDetailsRv = findViewById(R.id.orderDetailsRv);
        orderedProductsRv = findViewById(R.id.orderedProductsRv);

        Intent intent = getIntent();
        orderTo = intent.getStringExtra("orderTo");
        orderId = intent.getStringExtra("orderId");


        pId = intent.getStringExtra("pId");

        firebaseAuth = FirebaseAuth.getInstance();
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        writeReviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(OrderDetailsUserActivity.this, WriteReviewActivity.class);
                intent1.putExtra("shopUid", orderTo);
                startActivity(intent1);
            }
        });

        tabDetailsRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderDetailsUI();
            }
        });

        tabItemsRv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showOrderedItemsUI();
            }
        });

        checkUserStatus();

    }

    private void showOrderedItemsUI() {

        orderDetailsRv.setVisibility(View.GONE);
        orderedProductsRv.setVisibility(View.VISIBLE);

        detailsBorderRv.setVisibility(View.INVISIBLE);
        itemsBorderRv.setVisibility(View.VISIBLE);

        detailsText.setTextColor(getResources().getColor(R.color.white));
        itemsText.setTextColor(getResources().getColor(R.color.primaryTextColor));


//        tabOrderDetailsTv.setTextColor(getResources().getColor(R.color.colorWhite));
//        tabOrderDetailsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//
//        tabOrderedProductsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        detailsText.setBackgroundResource(R.drawable.shape_rect04);

    }

    private void showOrderDetailsUI() {

        orderDetailsRv.setVisibility(View.VISIBLE);
        orderedProductsRv.setVisibility(View.GONE);

        detailsBorderRv.setVisibility(View.VISIBLE);
        itemsBorderRv.setVisibility(View.INVISIBLE);

        itemsText.setTextColor(getResources().getColor(R.color.white));
        detailsText.setTextColor(getResources().getColor(R.color.primaryTextColor));

//        tabOrderDetailsTv.setTextColor(getResources().getColor(R.color.colorBlack));
        itemsText.setBackgroundResource(R.drawable.shape_rect04);
//
//        tabOrderedProductsTv.setTextColor(getResources().getColor(R.color.colorWhite));
//        tabOrderedProductsTv.setBackgroundColor(getResources().getColor(android.R.color.transparent));

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Show Ordered Details to User//

    private void loadShopInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String shopName = ""+snapshot.child("shopName").getValue();
                        shopNameTv.setText(shopName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderDetails() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderBy = ""+snapshot.child("orderBy").getValue();
                        String orderCost = ""+snapshot.child("orderCost").getValue();
                        String orderId = ""+snapshot.child("orderId").getValue();
                        String orderStatus = ""+snapshot.child("orderStatus").getValue();
                        String orderTime = ""+snapshot.child("orderTime").getValue();
                        String orderTo = ""+snapshot.child("orderTo").getValue();
                        String deliveryFee = ""+snapshot.child("deliveryFee").getValue();
                        String latitude = ""+snapshot.child("latitude").getValue();
                        String longitude = ""+snapshot.child("longitude").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if (orderStatus.equals("In Progress")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        }else if (orderStatus.equals("Completed")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        }else if (orderStatus.equals("Cancelled")){
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$" + orderCost + "[Including delivery fee $"+ deliveryFee +"]");
                        dateTv.setText(formatedDate);

                        findAddress(latitude, longitude);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void findAddress(String latitude, String longitude) {

        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(lat,lon,1);
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);
        }catch (Exception e){

        }

    }

    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderTo).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderedItemArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            orderedItemArrayList.add(modelOrderedItem);
                        }

                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsUserActivity.this, orderedItemArrayList, orderTo);
                        itemsRv.setAdapter(adapterOrderedItem);

                        totalItemsTv.setText(""+snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            loadShopInfo();
            loadOrderDetails();
            loadOrderedItems();
            showOrderDetailsUI();



        } else {
            startActivity(new Intent(OrderDetailsUserActivity.this, LoginActivity.class));
            OrderDetailsUserActivity.this.finish();
        }
    }
}