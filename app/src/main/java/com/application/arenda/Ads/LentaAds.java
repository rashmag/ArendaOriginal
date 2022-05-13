package com.application.arenda.Ads;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.application.arenda.AddAds.ModelPrice;
import com.application.arenda.AddAds.MultiPriceViewHolder;
import com.application.arenda.Chat.MessageActivity;
import com.application.arenda.MainActivity;
import com.application.arenda.Model.ModelAll;
import com.application.arenda.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class LentaAds extends AppCompatActivity {
    private TextView nameAds, nameUser, writeMessage, textViewAddress,
            textViewDirectionAddAds,arrow_back;
    private ImageView favoriteBtnAds, editBtnAds;
    private ConstraintLayout constraintLayoutLentaAds;

    private SliderView sliderView;
    private List<ModelPrice> modelPricesList;
    private ModelPrice modelPrice;
    private MultiPriceViewHolder multiPriceViewHolder;
    private RecyclerView arrayPriceRVLentaAds;
    public FrameLayout frameLayoutLentaAds;
    private CircleImageView userPhoto;
    private String postId, title, publisherId,fragment;
    private String firebaseAuth;
    private Button btnRent;
    private SliderAdpActivity sliderAdp;
    public static final String REQUEST_RENT = "request";
    public static final String EMPTY_RENT = "empty";
    public static final String REFUSE_RENT = "refuse";
    public static final String ALLOW_RENT = "allow";
    public static final String RENT_KEY = "rent";
    private List<String> remoteImages2;
    private FirebaseDatabase database;
    private DatabaseReference dRefrrayImagesUrl,dRefrrayPrice, referencePost, referenceUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lenta_ads);
        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        //Инициализируем
        init();

        Intent intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance().getCurrentUser().getUid();
        postId = intent.getExtras().getString("postId");
        publisherId = intent.getExtras().getString("publisherId");
        fragment = intent.getExtras().getString("fragment");
        clickBtnRent();
        checkRent();
        isSaved(postId);
        remoteImages2 = new ArrayList<>();
        textViewAddress.setTextColor(Color.BLUE);
        database = FirebaseDatabase.getInstance();
        if (publisherId.equals(firebaseAuth)) {
            editBtnAds.setVisibility(View.VISIBLE);
            favoriteBtnAds.setVisibility(View.GONE);
            writeMessage.setVisibility(View.GONE);
        }
        //Цены
        modelPricesList = new ArrayList<>();
        modelPrice = new ModelPrice();
        LinearLayoutManager linearLayoutManagerPrice = new LinearLayoutManager(LentaAds.this,
                LinearLayoutManager.HORIZONTAL, false);
        multiPriceViewHolder = new MultiPriceViewHolder(getApplicationContext(),true);
        arrayPriceRVLentaAds.setLayoutManager(linearLayoutManagerPrice);
        arrayPriceRVLentaAds.setFocusable(false);
        arrayPriceRVLentaAds.setHasFixedSize(true);
        arrayPriceRVLentaAds.setAdapter(multiPriceViewHolder);


        editBtnAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(LentaAds.this, EditAds.class);
                intent1.putExtra("postId", postId);
                intent1.putExtra("publisherId", publisherId);
                startActivity(intent1);
            }
        });
        arrow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LentaAds.this, MainActivity.class);
                intent.putExtra("fragmentSelect", fragment);
                startActivity(intent);
            }
        });

        favoriteBtnAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String auth = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (favoriteBtnAds.getTag().equals("save")) {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(auth)
                            .child(postId).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(auth)
                            .child(postId).removeValue();
                }
            }
        });

        writeMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LentaAds.this, MessageActivity.class);
                intent.putExtra("userId", publisherId);
                intent.putExtra("postId", postId);
                startActivity(intent);
            }
        });
        loadData();
    }

    private void checkRent() {
        FirebaseDatabase.getInstance().getReference("Chat").child(postId).child("rent")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("test1","0");
                            String rent = snapshot.getValue(String.class);
                            if(rent == null){
                                rent = EMPTY_RENT;
                            }
                                Log.d("test1","1");
                                if (rent.equals(REQUEST_RENT)) {
                                    btnRent.setBackgroundColor(Color.GREEN);
                                    btnRent.setText("Запрос отправлен");
                                    Log.d("test1","2");
                                } else if(rent.equals(ALLOW_RENT)){
                                    btnRent.setBackgroundColor(Color.BLUE);
                                    btnRent.setText("Приняли заявку");
                                    Log.d("test1","3");
                                }else {
                                    Log.d("test1","4");
                                    btnRent.setBackgroundColor(getResources().getColor(R.color.main_popular));
                                    btnRent.setText(R.string.rent);
                                }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void clickBtnRent() {
        btnRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!FirebaseDatabase
                        .getInstance().getReference("Chat")
                        .child(postId).child(RENT_KEY).getKey().equals(REQUEST_RENT)){
                    sendMessage(FirebaseAuth.getInstance().getCurrentUser().getUid(),publisherId,
                            FirebaseAuth.getInstance().getCurrentUser().getUid(), REQUEST_RENT, postId);
                }
            }
        });
    }
    private void sendMessage(String sender, String receiver,String userId, String message,
                             String postId) {
        DatabaseReference reference = FirebaseDatabase
                .getInstance().getReference("Chat").child(postId);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", postId);
        hashMap.put("message", message);
        hashMap.put("userId", receiver);
        hashMap.put("postId", postId);
        hashMap.put(RENT_KEY, REQUEST_RENT);
        hashMap.put("isseen", false);
        reference.updateChildren(hashMap);
    }

    public void setSliderPosition(int positionImageSlider) {
        constraintLayoutLentaAds.setPadding(100,0,100,0);
        frameLayoutLentaAds.setVisibility(View.GONE);
        sliderView.setCurrentPagePosition(positionImageSlider);
    }

    public void callFramentCropImages(int positionImageSlider) {
        constraintLayoutLentaAds.setPadding(0,0,0,0);
        frameLayoutLentaAds.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameLayoutLentaAds,
                new CropImages(remoteImages2, positionImageSlider)).commit();
    }

    private void loadData() {
        referenceUsers = database.getReference("Users");
        referenceUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ModelAll modelAuth = snapshot1.getValue(ModelAll.class);
                    if (modelAuth.getId() != null) {
                        if (modelAuth.getId().equals(publisherId)) {
                            nameUser.setText(modelAuth.getUserName());
                            if (modelAuth.getUserPhotoUri() != null) {
                                Glide.with(getApplicationContext()).load(modelAuth.getUserPhotoUri()).into(userPhoto);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        referencePost = database.getReference("Post");
        referencePost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ModelAll modelAll = snapshot1.getValue(ModelAll.class);
                    if (modelAll.getPostId() != null) {
                        if (modelAll.getPostId().equals(postId)) {
                            if (modelAll.getName() != null) {
                                nameAds.setText(modelAll.getName());
                            }
                            //Получаем массив цен
                            dRefrrayPrice = database.getReference("Post").
                                    child(modelAll.getPostId()).child("arrayprice");
                            dRefrrayPrice.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        modelPrice = new ModelPrice(String.valueOf(snapshot1.
                                                child("price").getValue()),String.valueOf(snapshot1.
                                                child("time").getValue()));
                                        modelPricesList.add(modelPrice);
                                    }
                                    multiPriceViewHolder.setData(modelPricesList);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                            //Получаем массив изображений
                            dRefrrayImagesUrl = database.getReference("Post").
                                    child(modelAll.getPostId()).child("arrayimagesurl");
                            dRefrrayImagesUrl.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                        remoteImages2.add(String.valueOf(snapshot1.
                                                child("adsurl").getValue()));
                                    }
                                    sliderAdp = new SliderAdpActivity(remoteImages2, LentaAds.this);
                                    sliderView.setSliderAdapter(sliderAdp);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                            if (modelAll.getDirection() != null) {
                                textViewDirectionAddAds.setText(modelAll.getDirection());
                            }
                            if (modelAll.getAddress() != null && modelAll.getLat() != 0
                                    && modelAll.getLon() != 0) {
                                textViewAddress.setText(modelAll.getAddress());
                                textViewAddress.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent1 = new Intent(LentaAds.this,
                                                MapsActivityProduct.class);
                                        intent1.putExtra("address", textViewAddress.getText().toString());
                                        intent1.putExtra("lat", modelAll.getLat());
                                        intent1.putExtra("lon", modelAll.getLon());
                                        startActivity(intent1);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LentaAds.this, MainActivity.class);
        intent.putExtra("fragmentSelect", fragment);
        startActivity(intent);
    }

    private void init() {
        arrayPriceRVLentaAds = findViewById(R.id.arrayPriceRVEditAds);
        btnRent = findViewById(R.id.btnRent);
        arrow_back = findViewById(R.id.return_lent);
        textViewDirectionAddAds = findViewById(R.id.textViewDirectionAddAds);
        favoriteBtnAds = findViewById(R.id.favoriteBtnAds);
        editBtnAds = findViewById(R.id.editBtnAds);
        constraintLayoutLentaAds = findViewById(R.id.constraintLayoutLentaAds);
        textViewAddress = findViewById(R.id.textViewAddress);
        nameUser = findViewById(R.id.nameUser);
        writeMessage = findViewById(R.id.writeMessage);
        userPhoto = findViewById(R.id.userPhoto);
        nameAds = findViewById(R.id.nameAds);
        frameLayoutLentaAds = findViewById(R.id.frameLayoutLentaAds);
        sliderView = findViewById(R.id.imageSlider);
    }

    private void isSaved(String postId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves")
                .child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (postId != null) {
                    if (snapshot.child(postId).exists()) {
                        favoriteBtnAds.setImageResource(R.drawable.favorite);
                        favoriteBtnAds.setTag("saved");
                    } else {
                        favoriteBtnAds.setImageResource(R.drawable.favorite_border);
                        favoriteBtnAds.setTag("save");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void status(String status) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
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