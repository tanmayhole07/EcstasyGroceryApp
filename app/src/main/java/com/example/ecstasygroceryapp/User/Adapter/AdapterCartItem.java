package com.example.ecstasygroceryapp.User.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecstasygroceryapp.Models.ModelCartItem;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Activities.ShopDetailsActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{


    private Context context;
    private ArrayList<ModelCartItem> cartItems;
    private String ShopUid ;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems, String shopUid) {
        this.context = context;
        this.cartItems = cartItems;
        this.ShopUid = shopUid;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_cartitem, parent, false);
        return new HolderCartItem(view);

    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, final int position) {

        ModelCartItem modelCartItem = cartItems.get(position);
        final String id = modelCartItem.getId();
        String getpId = modelCartItem.getpId();
        String title = modelCartItem.getName();
        final String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

        holder.itemTitleTv.setText(""+title);
        holder.itemPriceTv.setText("Total Price : $"+cost);
        holder.itemQuantityTv.setText("Quantity : "+quantity);
        holder.itemPriceEachTv.setText("$"+price);

        holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                EasyDB easyDB = EasyDB.init(context, "ITEMS_DB")
                        .setTableName("ITEMS_TABLE")
                        .addColumn(new Column("Item_Id", new String[]{"text","unique"}))
                        .addColumn(new Column("Item_PID", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Name", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price_Each", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Price", new String[]{"text","not null"}))
                        .addColumn(new Column("Item_Quantity", new String[]{"text","not null"}))
                        .doneTableColumn();

                easyDB.deleteRow(1, id);
                Toast.makeText(context, "Removed from cart...", Toast.LENGTH_SHORT).show();

                cartItems.remove(position);
                notifyItemChanged(position);
                notifyDataSetChanged();

                double subTotalWithoutDiscount = ((ShopDetailsActivity)context).allTotalPrice;
                double subTotalAfterProductRemove = subTotalWithoutDiscount = Double.parseDouble(cost.replace("$", ""));
                ((ShopDetailsActivity)context).allTotalPrice = subTotalAfterProductRemove;
                ((ShopDetailsActivity)context).sTotalTv.setText("$" + String.format("%.2f",((ShopDetailsActivity)context).allTotalPrice));

                double promoPrice = Double.parseDouble(((ShopDetailsActivity)context).promoPrice);
                double deliveryFee = Double.parseDouble(((ShopDetailsActivity)context).deliveryFee.replace("$",""));

                if(((ShopDetailsActivity)context).isPromoCodeApplied){

                    if (subTotalAfterProductRemove < Double.parseDouble(((ShopDetailsActivity)context).promoMinimumOrderPrice)){
                        Toast.makeText(context, "This code is valid for minimum order : $" + ((ShopDetailsActivity)context).promoMinimumOrderPrice, Toast.LENGTH_SHORT).show();
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).discountTv.setVisibility(View.GONE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText("");
                        ((ShopDetailsActivity)context).isPromoCodeApplied = false;

                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee))));

                    }else{
                        ((ShopDetailsActivity)context).applyBtn.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setVisibility(View.VISIBLE);
                        ((ShopDetailsActivity)context).promoDescriptionTv.setText(((ShopDetailsActivity)context).promoDescription);

                        ((ShopDetailsActivity)context).isPromoCodeApplied = true;
                        ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee - promoPrice))));

                    }

                }else{

                    ((ShopDetailsActivity)context).allTotalPriceTv.setText("$" + String.format("%.2f", Double.parseDouble(String.format("%.2f", subTotalAfterProductRemove + deliveryFee ))));


                }


//                double tx = Double.parseDouble((((ShopDetailsActivity)context).allTotalPriceTv.getText().toString().trim().replace("$","")));
//                double totalPrice = tx - Double.parseDouble(cost.replace("$",""));
//                double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context).deliveryFee.replace("$","")));
//                double sTotalPrice = Double.parseDouble(String.format("%.2f", totalPrice))- Double.parseDouble(String.format("%.2f", deliveryFee));
//                ((ShopDetailsActivity)context).allTotalPrice=0.00;
//                ((ShopDetailsActivity)context).sTotalTv.setText("$"+String.format("%.2f", sTotalPrice));
//                ((ShopDetailsActivity)context).allTotalPriceTv.setText("$"+String.format("%.2f", Double.parseDouble(String.format("%.2f", totalPrice))));

                ((ShopDetailsActivity)context).cartCount();

            }
        });

        loadProductInfo(getpId, holder);

    }

    private void loadProductInfo(String getpId, HolderCartItem holder) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(ShopUid).child("Products");
        ref.child(getpId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String productImage = ""+snapshot.child("productIcon").getValue();


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
        return cartItems.size();
    }


    class HolderCartItem extends RecyclerView.ViewHolder{

        private TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;
        ImageView productIv;
        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv = itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv = itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv = itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv = itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv = itemView.findViewById(R.id.itemRemoveTv);
            productIv = itemView.findViewById(R.id.productIv);


        }
    }

}
