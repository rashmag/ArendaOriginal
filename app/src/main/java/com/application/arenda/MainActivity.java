package com.application.arenda;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.application.arenda.AddAds.AddAds;
import com.application.arenda.Chat.ChatUsersList;
import com.application.arenda.Lenta.Lenta;
import com.application.arenda.Lenta.SearchNearbyMapsFragment;
import com.application.arenda.Lenta.TabLenta;
import com.application.arenda.Save.Save;
import com.application.arenda.User.Profile;
import com.application.arenda.User.ProfileAuth;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    public String auth, placeIntent, fragmentSelect;
    public double latIntent, lonIntent;
    private BottomNavigationView navView;
    private SharedPreferences sharedPreferences;
    private FragmentManager fragmentManager;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(getResources().getColor(R.color.fragment_white));
        sharedPreferences =
                getSharedPreferences("Auth", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();
        constraintLayout = findViewById(R.id.constraintLayoutMain);
        fragmentManager.registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
                super.onFragmentResumed(fm, f);
                if (f instanceof SearchNearbyMapsFragment) {
                    if (navView.getVisibility() == View.VISIBLE) {
                        navView.setVisibility(View.GONE);
                    }
//                            Toast.makeText(MainActivity.this, "1", Toast.LENGTH_SHORT).show();
//                            MotionLayout motionLayout = findViewById(R.id.motionLayoutMainActivity);
//                            motionLayout.transitionToEnd();

                } else if (f instanceof Lenta) {
//                    MotionLayout motionLayout = findViewById(R.id.motionLayoutMainActivity);
//                    motionLayout.transitionToStart();
                    if (navView.getVisibility() == View.GONE) {
                        navView.setVisibility(View.VISIBLE);
                    }
                }
//                T
            }
        }, true);
        FacebookSdk.sdkInitialize(MainActivity.this);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().
                    getProviderData()) {
                //facebook - "facebook.com", email - "password",
                // github - "github.com", google - "google.com", phone - "phone",
                // playgames - "playgames.google.com", twitter - "twitter.com".
                if (user.getProviderId().equals("facebook.com")) {
                    Toast.makeText(this, "facebook", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString("providerAuth", "facebook").apply();
                } else if (user.getProviderId().equals("google.com")) {
                    Toast.makeText(this, "google", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString("providerAuth", "google").apply();
                } else if (user.getProviderId().equals("password")) {
                    Toast.makeText(this, "email", Toast.LENGTH_SHORT).show();
                    sharedPreferences.edit().putString("providerAuth", "password").apply();
                }
            }
        }
        navView = findViewById(R.id.nav_view);
        Intent intent = getIntent();
        fragmentSelect = intent.getStringExtra("fragmentSelect");
        placeIntent = intent.getStringExtra("place");
        latIntent = intent.getDoubleExtra("lat", 0);
        lonIntent = intent.getDoubleExtra("lon", 0);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            auth = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (fragmentSelect != null) {
                if (fragmentSelect.equals("AddAds")) {
                    addOrShowAddAdsFragment();
                    navView.setSelectedItemId(R.id.navigation_ads);
                } else if (fragmentSelect.equals("Lenta")) {
                    addOrShowTabLentaFragment();
                    navView.setSelectedItemId(R.id.navigation_lenta);
                }else {
                    addOrShowProfileFragment();
                    navView.setSelectedItemId(R.id.navigation_user);
                }
            } else {
                addOrShowProfileFragment();
                navView.setSelectedItemId(R.id.navigation_user);
            }
        }
        SharedPreferences sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        sharedPreferences.edit().putString("getUid", auth).apply();


        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                switch (item.getItemId()) {
                    case R.id.navigation_lenta:
                        addOrShowTabLentaFragment();
//                        selectedFragment = new TabLenta();
                        break;
                    case R.id.navigation_favorite:
                        addOrShowSaveFragment();
//                        selectedFragment = new Save();
                        break;
                    case R.id.navigation_ads:
                        addOrShowAddAdsFragment();
//                        selectedFragment = new AddAds();
                        break;
                    case R.id.navigation_chat:
                        addOrShowChatFragment();
//                        selectedFragment = new ChatUsersList();
                        break;
                    case R.id.navigation_user:
                        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                            addOrShowProfileFragment();
//                            selectedFragment = new Profile();
                        } else {
                            addOrShowProfileAuthFragment();
//                            selectedFragment = new ProfileAuth();
                        }
                        break;
                    default:
                        addOrShowTabLentaFragment();
//                        selectedFragment = new TabLenta();
                        break;
                }
//                fragmentManager.beginTransaction().replace(R.id.fragmentContainer,
//                        selectedFragment).commit();
                return true;
            }
        });
    }

    private void status(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(auth);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }
    private void addOrShowTabLentaFragment(){
        if (fragmentManager.findFragmentByTag("TabLenta") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("TabLenta"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new TabLenta(),
                    "TabLenta").commit();
        }
        hideTwo(fragmentManager);
        hideThree(fragmentManager);
        hideFour(fragmentManager);
        hideFive(fragmentManager);
        hideSix(fragmentManager);
    }
    private void addOrShowSaveFragment(){
        if (fragmentManager.findFragmentByTag("Save") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("Save"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new Save(),
                    "Save").commit();
        }
        hideOne(fragmentManager);
        hideThree(fragmentManager);
        hideFour(fragmentManager);
        hideFive(fragmentManager);
        hideSix(fragmentManager);
    }
    private void addOrShowAddAdsFragment(){
        if (fragmentManager.findFragmentByTag("AddAds") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("AddAds"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new AddAds(),
                    "AddAds").commit();
        }
        hideTwo(fragmentManager);
        hideOne(fragmentManager);
        hideFour(fragmentManager);
        hideFive(fragmentManager);
        hideSix(fragmentManager);
    }
    private void addOrShowChatFragment(){
        if (fragmentManager.findFragmentByTag("ChatUsersList") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("ChatUsersList"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new ChatUsersList(),
                    "ChatUsersList").commit();
        }
        hideTwo(fragmentManager);
        hideThree(fragmentManager);
        hideOne(fragmentManager);
        hideFive(fragmentManager);
        hideSix(fragmentManager);
    }
    private void addOrShowProfileFragment(){
        if (fragmentManager.findFragmentByTag("Profile") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("Profile"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new Profile(),
                    "Profile").commit();
        }
        hideTwo(fragmentManager);
        hideThree(fragmentManager);
        hideFour(fragmentManager);
        hideOne(fragmentManager);
        hideSix(fragmentManager);
    }
    private void addOrShowProfileAuthFragment(){
        if (fragmentManager.findFragmentByTag("ProfileAuth") != null) {
            //if the fragment exists, show it.
            fragmentManager.beginTransaction().show(fragmentManager.
                    findFragmentByTag("ProfileAuth"))
                    .commit();
        } else {
            //if the fragment does not exist, add it to fragment manager.
            fragmentManager.beginTransaction().add(R.id.fragmentContainer,
                    new ProfileAuth(),
                    "ProfileAuth").commit();
        }
        hideOne(fragmentManager);
        hideTwo(fragmentManager);
        hideThree(fragmentManager);
        hideFour(fragmentManager);
        hideFive(fragmentManager);
    }
    private void hideOne(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("TabLenta") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("TabLenta")).commit();
        }

    }
    private void hideTwo(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("Save") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("Save")).commit();
        }
    }
    private void hideThree(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("AddAds") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("AddAds")).commit();
        }
    }
    private void hideFour(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("ChatUsersList") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("ChatUsersList")).commit();
        }
    }
    private void hideFive(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("Profile") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("Profile")).commit();
        }
    }
    private void hideSix(FragmentManager fragmentManager){
        if (fragmentManager.findFragmentByTag("ProfileAuth") != null) {
            //if the other fragment is visible, hide it.
            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("ProfileAuth")).commit();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            status("online");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            status("offline");
        }
    }
}