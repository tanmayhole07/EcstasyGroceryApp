package com.example.ecstasygroceryapp.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecstasygroceryapp.Models.ModelOrderedItem;
import com.example.ecstasygroceryapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AdapterOrderedItem extends RecyclerView.Adapter<AdapterOrderedItem.HolderOrderedItem>{

    private Context context;
    private ArrayList<ModelOrderedItem> orderedItemArrayList;
    private String ShopUid ;

    private FirebaseAuth firebaseAuth;

    public AdapterOrderedItem(Context context, ArrayList<ModelOrderedItem> orderedItemArrayList, String orderTo) {
        this.context = context;
        this.orderedItemArrayList = orderedItemArrayList;
        this.ShopUid = orderTo;
    }

    @NonNull
    @Override
    public HolderOrderedItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_ordereditems, parent, false);
        firebaseAuth = FirebaseAuth.getInstance();
        return new HolderOrderedItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderedItem holder, int position) {

        ModelOrderedItem modelOrderedItem = orderedItemArrayList.get(position);
        String getpId = modelOrderedItem.getpId();
        String name = modelOrderedItem.getName();
        String cost = modelOrderedItem.getCost();
        String price = modelOrderedItem.getPrice();
        String quantity = modelOrderedItem.getQuantity();

        holder.itemTitleTv.setText(name);
        holder.itemPriceTv.setText("$"+price);
        holder.itemPriceEachTv.setText("Total Price : $"+cost);
        holder.itemQuantityTv.setText("Quantity : " + quantity);

        loadProductInfo(getpId, holder);

    }

    private void loadProductInfo(String getpId, HolderOrderedItem holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(ShopUid).child("Products");
        ref.child(getpId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String productImage = ""+snapshot.child("productIcon").getValue();
//
//                try {
//                    Picasso.get().load(productImage).placeholder(R.drawable.add_product_image).into(holder.productIv);
//                }catch (Exception e){
//                    holder.productIv.setImageResource(R.drawable.add_product_image);
//                }

                try {
                    Picasso.get().load(productImage).into(holder.productIv);
                }catch (Exception e){

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return orderedItemArrayList.size();
    }

    class HolderOrderedItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv;
        ImageView productIv;
        public HolderOrderedItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            productIv = itemView.findViewById(R.id.productIv);

        }
    }

}
