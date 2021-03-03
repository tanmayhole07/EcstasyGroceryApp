package com.example.ecstasygroceryapp.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.ecstasygroceryapp.CommonActivities.LoginActivity;
import com.example.ecstasygroceryapp.Models.ModelShop;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.User.Adapter.AdapterShop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ShopsNearbyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShopsNearbyFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ShopsNearbyFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShopsNearbyFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShopsNearbyFragment newInstance(String param1, String param2) {
        ShopsNearbyFragment fragment = new ShopsNearbyFragment();
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

    private RecyclerView shopsRv;

    private ProgressDialog pd;

    String mUID = "uid";

    //Firebase Variables
    private FirebaseAuth firebaseAuth;

    private ArrayList<ModelShop> shopList;
    private AdapterShop adapterShop;

    private EditText searchProductEt;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shops_nearby, container, false);
        shopsRv = view.findViewById(R.id.shopsRv);
        searchProductEt = view.findViewById(R.id.searchProductEt);




        //checkUserStatus();
        loadShops("Mumbai");

        return (view);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadShops(final String myCity) {

        shopList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("accountTye").equalTo("Seller")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        shopList.clear();
                        for (DataSnapshot ds : snapshot.getChildren()){
                            ModelShop modelShop = ds.getValue(ModelShop.class);
                            String shopCity = ""+ds.child("city").getValue();
//                            if (shopCity.equals(myCity)){
//                                shopList.add(modelShop);
//                            }
                            // If want to display all shops, skip iff statement
                            shopList.add(modelShop);

                        }
                        adapterShop = new AdapterShop(getActivity(), shopList);
                        shopsRv.setAdapter(adapterShop);
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
            loadShops("Mumbai");

        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}