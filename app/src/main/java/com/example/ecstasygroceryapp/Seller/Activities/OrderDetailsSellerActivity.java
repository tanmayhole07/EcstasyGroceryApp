package com.example.ecstasygroceryapp.Seller.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ecstasygroceryapp.Constants;
import com.example.ecstasygroceryapp.Models.ModelOrderedItem;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Adapter.AdapterOrderedItem;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsSellerActivity extends AppCompatActivity {

    private ImageButton mapBtn;
    private TextView editBtn, orderIdTv, dateTv, orderStatusTv, emailTv, phoneTv, totalItemsTv, amountTv, addressTv;
    private RecyclerView itemsRv;
    ImageView backBtn;

    String orderId, orderBy;

    String sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;

    TextView tabOrderDetailsTv, tabOrderedProductsTv;
    RelativeLayout orderDetailsRv, orderedProductsRv, editBtnTextRv;

    RelativeLayout tabDetailsRv, tabItemsRv, detailsBorderRv, itemsBorderRv;
    TextView detailsText, itemsText, editBtnTextTv;

    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private AdapterOrderedItem adapterOrderedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details_seller);

        backBtn = findViewById(R.id.backBtn);
        editBtnTextTv = findViewById(R.id.editBtnTextTv);
//        mapBtn = findViewById(R.id.mapBtn);
        orderIdTv = findViewById(R.id.orderIdTv);
        dateTv = findViewById(R.id.dateTv);
        orderStatusTv = findViewById(R.id.orderStatusTv);
        emailTv = findViewById(R.id.emailTv);
        phoneTv = findViewById(R.id.phoneTv);
        totalItemsTv = findViewById(R.id.totalItemsTv);
        amountTv = findViewById(R.id.amountTv);
        addressTv = findViewById(R.id.addressTv);
        itemsRv = findViewById(R.id.itemsRv);

        tabDetailsRv = findViewById(R.id.tabDetailsRv);
        tabItemsRv = findViewById(R.id.tabItemsRv);

        detailsBorderRv = findViewById(R.id.detailsBorderRv);
        itemsBorderRv = findViewById(R.id.itemsBorderRv);
        detailsText = findViewById(R.id.detailsText);
        itemsText = findViewById(R.id.itemsText);

        orderDetailsRv = findViewById(R.id.orderDetailsRv);
        orderedProductsRv = findViewById(R.id.orderedProductsRv);

        orderId = getIntent().getStringExtra("orderId");
        orderBy = getIntent().getStringExtra("orderBy");

        firebaseAuth = FirebaseAuth.getInstance();
        loadMyInfo();
        loadBuyerInfo();
        loadOrderDetails();
        loadOrderedItems();
        showOrderDetailsUI();

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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

//        mapBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                OpenMap();
//            }
//        });
//
        editBtnTextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editOrderStatusDialog();
            }
        });
    }

    private void showOrderedItemsUI() {

        orderDetailsRv.setVisibility(View.GONE);
        orderedProductsRv.setVisibility(View.VISIBLE);
        editBtnTextTv.setVisibility(View.GONE);

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
        editBtnTextTv.setVisibility(View.VISIBLE);

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

    private void editOrderStatusDialog() {
        final String[] options = {"In Progress", "Completed", "Cancelled"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Order Status")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String selectedOption = options[i];
                        editOrderStatus(selectedOption);
                    }
                }).show();
    }

    private void editOrderStatus(final String selectedOption) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("orderStatus", "" + selectedOption);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String message = "Order is now " + selectedOption;
                        Toast.makeText(OrderDetailsSellerActivity.this, message, Toast.LENGTH_SHORT).show();
                        prepareNotificationMessage(orderId, message);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(OrderDetailsSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadOrderDetails() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String orderBy = "" + snapshot.child("orderBy").getValue();
                        String orderCost = "" + snapshot.child("orderCost").getValue();
                        String orderId = "" + snapshot.child("orderId").getValue();
                        String orderStatus = "" + snapshot.child("orderStatus").getValue();
                        String orderTime = "" + snapshot.child("orderTime").getValue();
                        String orderTo = "" + snapshot.child("orderTo").getValue();
                        String deliveryFee = "" + snapshot.child("deliveryFee").getValue();
                        String latitude = "" + snapshot.child("latitude").getValue();
                        String longitude = "" + snapshot.child("longitude").getValue();

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(orderTime));
                        String formatedDate = DateFormat.format("dd/MM/yyyy hh:mm a", calendar).toString();

                        if (orderStatus.equals("In Progress")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        } else if (orderStatus.equals("Completed")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorGreen));
                        } else if (orderStatus.equals("Cancelled")) {
                            orderStatusTv.setTextColor(getResources().getColor(R.color.colorRed));
                        }

                        orderIdTv.setText(orderId);
                        orderStatusTv.setText(orderStatus);
                        amountTv.setText("$" + orderCost + "[Including delivery fee $" + deliveryFee + "]");
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
            addresses = geocoder.getFromLocation(lat, lon, 1);
            String address = addresses.get(0).getAddressLine(0);
            addressTv.setText(address);
        } catch (Exception e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void OpenMap() {
        String address = "https://maps.google.com/maps?saar=" + sourceLatitude + "," + sourceLongitude + "&daddr=" + destinationLatitude + "," + destinationLongitude;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(address));
        startActivity(intent);
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        sourceLatitude = "" + snapshot.child("latitude").getValue();
                        sourceLongitude = "" + snapshot.child("longitude").getValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadBuyerInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(orderBy)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        destinationLatitude = "" + snapshot.child("latitude").getValue();
                        destinationLongitude = "" + snapshot.child("longitude").getValue();
                        String email = "" + snapshot.child("email").getValue();
                        String phone = "" + snapshot.child("phone").getValue();

                        emailTv.setText(email);
                        phoneTv.setText(phone);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void loadOrderedItems() {
        orderedItemArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders").child(orderId).child("Items")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        orderedItemArrayList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            ModelOrderedItem modelOrderedItem = ds.getValue(ModelOrderedItem.class);
                            orderedItemArrayList.add(modelOrderedItem);
                        }

                        adapterOrderedItem = new AdapterOrderedItem(OrderDetailsSellerActivity.this, orderedItemArrayList, firebaseAuth.getUid());
                        itemsRv.setAdapter(adapterOrderedItem);

                        totalItemsTv.setText("" + snapshot.getChildrenCount());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    private void prepareNotificationMessage(String orderId, String message) {

        String NOTIFICATION_TOPIC = "/topics/" + Constants.FCM_TOPIC;
        String NOTIFICATION_TITLE = "Your Order " + orderId;
        String NOTIFICATION_MESSAGE = "" + message;
        String NOTIFICATION_TYPE = "OrderStatusChanged";

        JSONObject notificationJo = new JSONObject();
        JSONObject notificationBodyJo = new JSONObject();
        try {
            notificationBodyJo.put("notificationType", NOTIFICATION_TYPE);
            notificationBodyJo.put("buyerUid", orderBy);
            notificationBodyJo.put("sellerUid", firebaseAuth.getUid());
            notificationBodyJo.put("orderId", orderId);
            notificationBodyJo.put("notificationTitle", NOTIFICATION_TITLE);
            notificationBodyJo.put("notificationMessage", NOTIFICATION_MESSAGE);

            notificationJo.put("to", NOTIFICATION_TOPIC);
            notificationJo.put("data", notificationBodyJo);

        } catch (JSONException e) {
            Toast.makeText(this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        sendFcmNotification(notificationJo);
    }

    private void sendFcmNotification(JSONObject notificationJo) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest("https://fcm.googleapis.com/fcm/send", notificationJo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {


            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "key=" + Constants.FCM_KEY);

                return super.getHeaders();
            }
        };

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

}