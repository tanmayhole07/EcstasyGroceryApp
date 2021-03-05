package com.example.ecstasygroceryapp.User.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecstasygroceryapp.Models.ModelShop;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Activities.ShopDetailsActivity;
import com.example.ecstasygroceryapp.User.Activities.ShopReviewActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterShop extends RecyclerView.Adapter<AdapterShop.HolderShop>{

    private Context context;
    public ArrayList<ModelShop> shopsList;

    public AdapterShop(Context context, ArrayList<ModelShop> shopsList) {
        this.context = context;
        this.shopsList = shopsList;
    }

    @NonNull
    @Override
    public HolderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_shop, parent, false);
        return new HolderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderShop holder, int position) {

        ModelShop modelShop = shopsList.get(position);
        String accountTye = modelShop.getAccountTye();
        String address = modelShop.getAddress();
        String city = modelShop.getCity();
        String country = modelShop.getCountry();
        String delivery = modelShop.getDeliveryFee();
        String email = modelShop.getEmail();
        String latitude = modelShop.getLatitude();
        String longitude = modelShop.getLongitude();
        String online = modelShop.getOnline();
        String name = modelShop.getName();
        String phone = modelShop.getPhone();
        final String uid = modelShop.getUid();
        String timeStamp = modelShop.getTimeStamp();
        String shopOpen = modelShop.getShopOpen();
        String postalCode = modelShop.getPostalCode();
        String profileImage = modelShop.getProfileImage();
        String shopName = modelShop.getShopName();

        loadReviews(modelShop, holder);

        holder.shopNameTv.setText(shopName);
        holder.phoneTv.setText(phone);
        holder.cityTv.setText(city);
        holder.postalCodeTv.setText(postalCode);

//        if (online.equals("true")){
//            holder.onlineIv.setVisibility(View.VISIBLE);
//        }else {
//            holder.onlineIv.setVisibility(View.GONE);
//
//        }


        if (shopOpen.equals("true")){
            holder.shopClosedTv.setVisibility(View.GONE);
        }else {
            holder.shopClosedTv.setVisibility(View.VISIBLE);
        }

        try {
            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_grey).into(holder.shopIv);
        }catch (Exception e){
            holder.shopIv.setImageResource(R.drawable.ic_store_grey);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopDetailsActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });

        holder.ratingBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ShopReviewActivity.class);
                intent.putExtra("shopUid", uid);
                context.startActivity(intent);
            }
        });

    }



    private float ratimgSum = 0;
    private void loadReviews(ModelShop modelShop, final HolderShop holder) {

        String shopUid = modelShop.getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(shopUid).child("Ratings")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        ratimgSum = 0;
                        for (DataSnapshot ds: snapshot.getChildren()){
                            float rating = Float.parseFloat(""+ds.child("ratings").getValue());
                            ratimgSum = ratimgSum + rating;


                        }

                        long numberOfReviews = snapshot.getChildrenCount();
                        float avgRating = ratimgSum/numberOfReviews;

                        holder.ratingBar.setRating(avgRating);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
    @Override
    public int getItemCount() {
        return shopsList.size();
    }

    class HolderShop extends RecyclerView.ViewHolder{

        private ImageView shopIv, onlineIv;
        private TextView shopClosedTv, shopNameTv, phoneTv, cityTv, postalCodeTv;
        private RatingBar ratingBar;

        public HolderShop(@NonNull View itemView) {
            super(itemView);

            shopIv = itemView.findViewById(R.id.shopIv);
//            onlineIv = itemView.findViewById(R.id.onlineIv);
            shopClosedTv = itemView.findViewById(R.id.shopClosedTv);
            shopNameTv = itemView.findViewById(R.id.shopNameTv);
            cityTv = itemView.findViewById(R.id.cityTv);
            postalCodeTv = itemView.findViewById(R.id.postalCodeTv);
            phoneTv = itemView.findViewById(R.id.phoneTv);
            ratingBar = itemView.findViewById(R.id.ratingBar);

        }
    }
}

