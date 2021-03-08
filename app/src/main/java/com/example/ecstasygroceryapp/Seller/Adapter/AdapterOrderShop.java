package com.example.ecstasygroceryapp.Seller.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ecstasygroceryapp.FilterOrderShop;
import com.example.ecstasygroceryapp.Models.ModelOrderShop;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.Seller.Activities.OrderDetailsSellerActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class AdapterOrderShop extends RecyclerView.Adapter<AdapterOrderShop.HolderOrderShop> implements Filterable {

    public ArrayList<ModelOrderShop> orderShopArrayList, filterList;
    private Context context;
    private FilterOrderShop filter;

    public AdapterOrderShop(Context context, ArrayList<ModelOrderShop> orderShopArrayList) {
        this.context = context;
        this.orderShopArrayList = orderShopArrayList;
        this.filterList = orderShopArrayList;
    }

    @NonNull
    @Override
    public HolderOrderShop onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_order_seller, parent, false);
        return new HolderOrderShop(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderOrderShop holder, int position) {

        ModelOrderShop modelOrderShop = orderShopArrayList.get(position);
        final String orderId = modelOrderShop.getOrderId();
        final String orderBy = modelOrderShop.getOrderBy();
        String orderCost = modelOrderShop.getOrderCost();
        String orderStatus = modelOrderShop.getOrderStatus();
        final String orderTime = modelOrderShop.getOrderTime();
        final String orderTo = modelOrderShop.getOrderTo();

        loadUserInfo(modelOrderShop, holder);

        holder.amountTv.setText("Amount: $" + orderCost);
        holder.statusTv.setText(orderStatus);


        if (orderStatus.equals("In Progress")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorPrimary));
        }else if (orderStatus.equals("Completed")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorGreen));
        }else  if (orderStatus.equals("Cancelled")){
            holder.statusTv.setTextColor(context.getResources().getColor(R.color.colorRed));
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(orderTime));
        String formatDate = DateFormat.format("dd/MM/yyyy", calendar).toString();

        holder.orderDateTv.setText(formatDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, OrderDetailsSellerActivity.class);
                intent.putExtra("orderId", orderId);
                intent.putExtra("orderBy", orderBy);
                context.startActivity(intent);
            }
        });

    }

    private void loadUserInfo(ModelOrderShop modelOrderShop, final HolderOrderShop holder) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(modelOrderShop.getOrderBy())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = ""+snapshot.child("email").getValue();
                        String personName = ""+snapshot.child("name").getValue();
                        String city = ""+snapshot.child("city").getValue();
                        String postalCode = ""+snapshot.child("postalCode").getValue();


//                        holder.emailTv.setText(email);
                        holder.personNameTv.setText(personName);
                        holder.deliverAtTv.setText("Deliver at "+city +", "+ postalCode);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return orderShopArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterOrderShop(this, filterList);
        }
        return filter;
    }

    class HolderOrderShop extends RecyclerView.ViewHolder{

        private TextView  orderDateTv,  amountTv, statusTv, personNameTv, deliverAtTv;
        public HolderOrderShop(@NonNull View itemView) {
            super(itemView);

//            orderIdTv = itemView.findViewById(R.id.orderIdTv);

            orderDateTv = itemView.findViewById(R.id.orderDateTv);
            amountTv = itemView.findViewById(R.id.amountTv);
            statusTv = itemView.findViewById(R.id.statusTv);
            deliverAtTv = itemView.findViewById(R.id.deliverAtTv);
            personNameTv = itemView.findViewById(R.id.personNameTv);



        }
    }


}
