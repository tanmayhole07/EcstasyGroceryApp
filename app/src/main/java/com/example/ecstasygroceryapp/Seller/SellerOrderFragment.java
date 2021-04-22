package com.example.ecstasygroceryapp.Seller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.ecstasygroceryapp.CommonActivities.LoginActivity;
import com.example.ecstasygroceryapp.Models.ModelOrderShop;
import com.example.ecstasygroceryapp.Models.ModelProduct;
import com.example.ecstasygroceryapp.R;
import com.example.ecstasygroceryapp.Seller.Adapter.AdapterOrderShop;
import com.example.ecstasygroceryapp.Seller.Adapter.AdapterProductSeller;
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
 * Use the {@link SellerOrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SellerOrderFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SellerOrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment sellerOrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SellerOrderFragment newInstance(String param1, String param2) {
        SellerOrderFragment fragment = new SellerOrderFragment();
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

    TextView filteredOrdersTv;
    ImageButton filterOrderBtn;
    RecyclerView ordersRv;

    private ProgressDialog pd;

    private ArrayList<ModelProduct> productList;
    private AdapterProductSeller adapterProductSeller;

    private ArrayList<ModelOrderShop> orderShopArrayList;
    private AdapterOrderShop adapterOrderShop;

    //Firebase Variables
    private FirebaseAuth firebaseAuth;
    String mUID = "uid";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_seller_order, container, false);

        filteredOrdersTv = view.findViewById(R.id.filteredOrdersTv);
        filterOrderBtn = view.findViewById(R.id.filterOrderBtn);
        ordersRv = view.findViewById(R.id.ordersRv);

        firebaseAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(getActivity());
        pd.setTitle("Please Wait");
        pd.setCanceledOnTouchOutside(false);


        filterOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] options = {"All","In Progress", "Completed", "Canceled"};

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Filter Orders:")
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (i==0){
                                    filteredOrdersTv.setText("Showing All Orders");
                                    loadAllOrders();
                                }else {
                                    String optionClicked = options[i];
                                    filteredOrdersTv.setText("Showing "+optionClicked +" Orders");
                                    adapterOrderShop.getFilter().filter(optionClicked);
                                }
                            }
                        }).show();
            }
        });

        checkUserStatus();
        return (view);
    }

    private void loadAllOrders() {
        orderShopArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()).child("Orders")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderShopArrayList.clear();
                        for (DataSnapshot ds: snapshot.getChildren()){
                            ModelOrderShop modelOrderShop = ds.getValue(ModelOrderShop.class);
                            orderShopArrayList.add(modelOrderShop);
                        }

                        adapterOrderShop = new AdapterOrderShop(getActivity(), orderShopArrayList);
                        ordersRv.setAdapter(adapterOrderShop);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            mUID = user.getUid();
            loadAllOrders();

        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}