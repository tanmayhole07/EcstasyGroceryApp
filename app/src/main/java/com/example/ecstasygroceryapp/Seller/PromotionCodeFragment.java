package com.example.ecstasygroceryapp.Seller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ecstasygroceryapp.Models.ModelPromotion;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.Seller.Activities.AddPromotionCodeActivity;
import com.example.ecstasygroceryapp.Seller.Adapter.AdapterPromotionShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PromotionCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PromotionCodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PromotionCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddPromotionCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PromotionCodeFragment newInstance(String param1, String param2) {
        PromotionCodeFragment fragment = new PromotionCodeFragment();
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

    private Button addPromoBtn;
    private TextView filteredCodeTv;
    private ImageButton filterCodeBtn;
    private RecyclerView promoRv;

    private ArrayList<ModelPromotion> promotionArrayList;
    private AdapterPromotionShop adapterPromotionShop;

    FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_promotion_code, container, false);

        addPromoBtn = view.findViewById(R.id.addPromoBtn);
        filteredCodeTv = view.findViewById(R.id.filteredCodeTv);
        filterCodeBtn = view.findViewById(R.id.filterCodeBtn);
        promoRv = view.findViewById(R.id.promoRv);

        firebaseAuth = FirebaseAuth.getInstance();

        addPromoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddPromotionCodeActivity.class));
            }
        });

        filterCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterDialog();
            }
        });

        loadAllPromoCodes();

        return view;
    }

    private void filterDialog() {

        String[] options = {"All", "Expired", "Not Expired"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter Promotion Codes")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i==0){
                            filteredCodeTv.setText("All Promotion Codes");
                            loadAllPromoCodes();
                        }else if (i == 1){
                            filteredCodeTv.setText("Expired Promotion Codes");
                            loadExpiredPromoCodes();
                        }else if(i ==2){
                            filteredCodeTv.setText("Not Expired Promotion Codes");
                            loadNotExpiredPromoCodes();
                        }
                    }
                }).show();
    }

    private void loadAllPromoCodes(){

        promotionArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);
                            promotionArrayList.add(modelPromotion);
                        }
                        adapterPromotionShop = new AdapterPromotionShop(getActivity(), promotionArrayList);
                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void loadExpiredPromoCodes(){

        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        String todayDate = mDay + "/" + mMonth + "/" + mYear;

        promotionArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            try {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = simpleDateFormat.parse(todayDate);
                                Date expireDate = simpleDateFormat.parse(expDate);
                                if (expireDate.compareTo(currentDate)>0){

                                }
                                else if (expireDate.compareTo(currentDate)<0){
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate) == 0){

                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }
                        adapterPromotionShop = new AdapterPromotionShop(getActivity(), promotionArrayList);


                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void loadNotExpiredPromoCodes(){

        DecimalFormat mFormat = new DecimalFormat("00");
        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        String todayDate = mDay + "/" + mMonth + "/" + mYear;

        promotionArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Promotions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        promotionArrayList.clear();
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ModelPromotion modelPromotion = ds.getValue(ModelPromotion.class);

                            String expDate = modelPromotion.getExpireDate();

                            try {
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                                Date currentDate = simpleDateFormat.parse(todayDate);
                                Date expireDate = simpleDateFormat.parse(expDate);
                                if (expireDate.compareTo(currentDate)>0){
                                    promotionArrayList.add(modelPromotion);
                                }
                                else if (expireDate.compareTo(currentDate)<0){

                                }
                                else if (expireDate.compareTo(currentDate) == 0){
                                    promotionArrayList.add(modelPromotion);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        }
                        adapterPromotionShop = new AdapterPromotionShop(getActivity(), promotionArrayList);


                        promoRv.setAdapter(adapterPromotionShop);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}