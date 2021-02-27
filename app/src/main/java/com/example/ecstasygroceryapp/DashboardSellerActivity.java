package com.example.ecstasygroceryapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ecstasygroceryapp.Activities.LoginActivity;
import com.example.ecstasygroceryapp.Fragments.AboutUsFragment;
import com.example.ecstasygroceryapp.Fragments.Seller.SellerAccountkFragment;
import com.example.ecstasygroceryapp.Fragments.Seller.SellerAddProductFragment;
import com.example.ecstasygroceryapp.Fragments.Seller.SellerOrderFragment;
import com.example.ecstasygroceryapp.Fragments.Seller.StoreProductsFragment;
import com.example.ecstasygroceryapp.Fragments.User.ShopsNearbyFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class DashboardSellerActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    String mUID = "uid";
    ActionBar actionBar;

    private ProgressDialog pd;

    DrawerLayout drawer;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_seller);

        pd = new ProgressDialog(this);
        pd.setTitle("Please Title");
        pd.setCanceledOnTouchOutside(false);
        firebaseAuth = FirebaseAuth.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView textView = (TextView) toolbar.findViewById(R.id.toolbarTextView);
        textView.setText("Ecstasy");

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawer = findViewById(R.id.drawer_layout_seller);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ShopsNearbyFragment()).commit();
        navigationView = findViewById(R.id.nav_view_seller);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                Fragment fragment = null;
                switch (id) {
                    case R.id.userAccount:
                        fragment = new SellerAccountkFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.storeProducts:
                        fragment = new StoreProductsFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.sellerOrders:
                        fragment = new SellerOrderFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.addProduct:
                        fragment = new SellerAddProductFragment();
                        loadFragment(fragment);
                        break;
                    case R.id.loginUser:
                        startActivity(new Intent(DashboardSellerActivity.this, LoginActivity.class));
                        break;

                    case R.id.aboutUs:
                        fragment = new AboutUsFragment();
                        loadFragment(fragment);
                        break;

                    case R.id.logout:
                        firebaseAuth.signOut();
                        makeMeOffline();

                    default:
                        return true;
                }
                return true;
            }
        });

        checkUserStatus();
        loadUserName();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();
        drawer.closeDrawer(GravityCompat.START);
        fragmentTransaction.addToBackStack(null);
    }

    private void loadUserName() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.orderByChild("uid").equalTo(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String name = "" + ds.child("name").getValue();
                            String email = "" + ds.child("email").getValue();
                            String phone = "" + ds.child("phone").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            String accountTye = "" + ds.child("accountTye").getValue();
                            String city = "" + ds.child("city").getValue();

                            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_seller);
                            View headerView = navigationView.getHeaderView(0);
                            TextView navUsername = (TextView) headerView.findViewById(R.id.nameTv);
                            ImageView navProfileImage = (ImageView) headerView.findViewById(R.id.profileIv);
                            navUsername.setText(name);

                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.logo1).into(navProfileImage);
                            }catch (Exception e){
                                navProfileImage.setImageResource(R.drawable.logo1);
                            }

                        }
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

        } else {
            startActivity(new Intent(DashboardSellerActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void makeMeOffline() {
        pd.setMessage("Logging Out");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("online", "false");

        FirebaseUser user = firebaseAuth.getCurrentUser();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid()+"").child("online").setValue("false");
        checkUserStatus();
                
                
//                updateChildren(hashMap)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        checkUserStatus();
//                        finish();
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        pd.dismiss();
//                        Toast.makeText(DashboardSellerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    @Override
    protected void onResume() {
        checkUserStatus();
        super.onResume();
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        super.onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            finishAndRemoveTask();
            super.onBackPressed();
        }
    }

}