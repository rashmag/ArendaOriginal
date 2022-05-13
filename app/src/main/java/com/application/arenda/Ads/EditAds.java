package com.application.arenda.Ads;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.application.arenda.AddAds.MapsActivityAddAddress;
import com.application.arenda.AddAds.ModelPrice;
import com.application.arenda.AddAds.MultiPriceViewHolder;
import com.application.arenda.AddAds.MultiSelectImageViewHolder;
import com.application.arenda.Model.ModelAll;
import com.application.arenda.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gun0912.tedbottompicker.TedBottomPicker;
import gun0912.tedbottompicker.TedBottomSheetDialogFragment;

public class EditAds extends AppCompatActivity {
    private TextView addressAds;
    private Button btnEditAds;
    private List<Uri> uriListSend;
    private MultiSelectImageViewHolder multiSelectImageViewHolder;
    private ProgressBar progressBar_main;
    private List<ModelPrice> modelPricesList;
    private MultiPriceViewHolder multiPriceViewHolder;
    private EditText nameAds, directionAds;
    private String adsPhotoUrl, userPhotoUrl;
    private DatabaseReference dRefrrayPrice,dRefrrayImagesUrl;
    private List<Uri> imagesArrayList;
    private FirebaseDatabase database;
    private RecyclerView arrayPriceRVEditAds,arrayImagesRVEditAds;
    private ModelPrice modelPrice;
    private String postId, title, publisherId;
    private Button selectImageAds;
    private static final int PICK_VIDEO = 1;
    private Uri videoUri;
    private UploadTask uploadTask;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    public String placeIntent, userIdIntent;
    public double latIntent, lonIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ads);

        init();
        userIdIntent = FirebaseAuth.getInstance().getCurrentUser().getUid();
        addressAds.setTextColor(Color.BLUE);
        storageReference = FirebaseStorage.getInstance().getReference("Photo");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        placeIntent = intent.getStringExtra("place");
        latIntent = intent.getDoubleExtra("lat", 0);
        lonIntent = intent.getDoubleExtra("lon", 0);
        postId = intent.getExtras().getString("postId");
        publisherId = intent.getExtras().getString("publisherId");

        if (placeIntent != null) {
            addressAds.setText(placeIntent);
        }
        imagesArrayList = new ArrayList<>();
        loadingData();
        //Изображения
        multiSelectImageViewHolder = new MultiSelectImageViewHolder(EditAds.this,false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(EditAds.this,
                LinearLayoutManager.HORIZONTAL, false);
        arrayImagesRVEditAds.setLayoutManager(linearLayoutManager);
        arrayImagesRVEditAds.setFocusable(false);
        arrayImagesRVEditAds.setHasFixedSize(true);
        arrayImagesRVEditAds.setAdapter(multiSelectImageViewHolder);
        //Цены
        database = FirebaseDatabase.getInstance();
        modelPricesList = new ArrayList<>();
        modelPrice = new ModelPrice();
        LinearLayoutManager linearLayoutManagerPrice = new LinearLayoutManager(EditAds.this,
                LinearLayoutManager.HORIZONTAL, false);
        multiPriceViewHolder = new MultiPriceViewHolder(getApplicationContext(),false);
        arrayPriceRVEditAds.setLayoutManager(linearLayoutManagerPrice);
        arrayPriceRVEditAds.setFocusable(false);
        arrayPriceRVEditAds.setHasFixedSize(true);
        arrayPriceRVEditAds.setAdapter(multiPriceViewHolder);
        selectImageAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        addressAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(EditAds.this,
                        MapsActivityAddAddress.class);
                intent1.putExtra("parent", "editAds");
                intent1.putExtra("postId", postId);
                intent1.putExtra("adsPhotoUrl", adsPhotoUrl);
                startActivity(intent1);
            }
        });
        btnEditAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(nameAds.getText().toString())) {
                    nameAds.setError("Заполните название");
                } else if (TextUtils.isEmpty(directionAds.getText().toString())) {
                    directionAds.setError("Заполните описание");
                } else if (TextUtils.isEmpty(addressAds.getText().toString())) {
                    addressAds.setError("Добавьте адрес");
                } else {
                    UploadVideo(nameAds.getText().toString(), directionAds.getText().toString(),
                            addressAds.getText().toString());
                }
            }
        });
    }
    private void requestPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                openBorromPicker();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(EditAds.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }


        };

        TedPermission.with(EditAds.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }
    private void openBorromPicker() {
        TedBottomPicker.with(EditAds.this)
                .setPeekHeight(1600)
                .showTitle(false)
                .setSelectMaxCount(6)
                .setSelectMaxCountErrorText("Больше 6 нельзя :(")
                .setCompleteButtonText("Готово")
                .setEmptySelectionText("Не выбрано")
                .showMultiImage(new TedBottomSheetDialogFragment.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(List<Uri> uriList) {
                        if (uriList != null && !uriList.isEmpty()) {
                            uriListSend = uriList;
                            multiSelectImageViewHolder.setData(uriList,false);
                            if (uriList.size() != 0) {
                                selectImageAds.setText("Выбрать заново");
                            } else {
                                selectImageAds.setText("Выбрать изображение");
                            }
                        }
                        // here is selected image uri list
                    }
                });
    }

    private String getExt(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void UploadVideo(String name, String direction, String address) {
        progressBar_main.setVisibility(View.VISIBLE);
        if (videoUri != null) {
            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
            StorageReference photoRef = firebaseStorage.getReferenceFromUrl(adsPhotoUrl);
            photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    editData(name, direction, address);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    editData(name, direction, address);
                    Toast.makeText(EditAds.this, "Ошибка", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            editData(name, direction, address);
        }

    }

    private void loadingData() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ModelAll modelAuth = snapshot1.getValue(ModelAll.class);
                    if (modelAuth.getId() != null && publisherId != null) {
                        if (modelAuth.getId().equals(publisherId)) {
                            userPhotoUrl = modelAuth.getUserPhotoUri();
                            if (userPhotoUrl != null) {
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        DatabaseReference referencePost = FirebaseDatabase.getInstance().getReference("Post");
        referencePost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ModelAll modelAll = snapshot1.getValue(ModelAll.class);
                    if (modelAll.getPostId() != null && postId != null) {
                        if (modelAll.getPostId().equals(postId)) {
                            if (modelAll.getName() != null) {
                                nameAds.setText(modelAll.getName());
                            }
//                            if (modelAll.getAdsurl() != null) {
//                                Glide.with(EditAds.this).load(modelAll.getAdsurl()).
//                                        into(selectImageAds);
//                            }
                            if (modelAll.getDirection() != null) {
                                directionAds.setText(modelAll.getDirection());
                            }
                            if (modelAll.getAddress() != null && modelAll.getLat() != 0
                                    && modelAll.getLon() != 0 && placeIntent == null) {
                                addressAds.setText(modelAll.getAddress());
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
                                        imagesArrayList.add(Uri.parse(String.valueOf(snapshot1.
                                                child("adsurl").getValue())));
                                    }
                                    multiSelectImageViewHolder.setData(imagesArrayList,true);
                                    arrayImagesRVEditAds.setAdapter(multiSelectImageViewHolder);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void init() {
        arrayPriceRVEditAds = findViewById(R.id.arrayPriceRVEditAds);
        progressBar_main = findViewById(R.id.progressBarEditAds);
        btnEditAds = findViewById(R.id.btnEditAds);
        addressAds = findViewById(R.id.addressAdsEditAds);
        arrayImagesRVEditAds = findViewById(R.id.arrayImagesRVEditAds);
        selectImageAds = findViewById(R.id.selectImageAds);
        nameAds = findViewById(R.id.nameAdsEditAds);
        directionAds = findViewById(R.id.directionAdsEditAds);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_VIDEO) {
            if (resultCode == -1) {
                try {
                    videoUri = data.getData();
//                    imageAds.setImageURI(videoUri);
                } catch (Exception e) {
                    Toast.makeText(EditAds.this, "Изображение не выбрано",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void editData(String name, String direction, String address) {
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Post").
                child(postId);

        String nameAds = name;
        String directionAds = direction;
        if (videoUri != null) {
            final StorageReference reference = storageReference.child(System.currentTimeMillis()
                    + "." + getExt(videoUri));
            uploadTask = reference.putFile(videoUri);
            Task<Uri> urltask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return reference.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                progressBar_main.setVisibility(View.GONE);
                                Uri downloadUri = task.getResult();
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", nameAds);
                                hashMap.put("direction", directionAds);
                                hashMap.put("publisher", userIdIntent);
                                hashMap.put("address", address);
                                hashMap.put("search", nameAds);
                                hashMap.put("adsurl", downloadUri.toString());
                                reference1.updateChildren(hashMap);
                                adsPhotoUrl = downloadUri.toString();
                            } else {
                                progressBar_main.setVisibility(View.GONE);
                                Toast.makeText(EditAds.this, "Ошибка = " +
                                        task.getException(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", nameAds);
            hashMap.put("direction", directionAds);
            hashMap.put("publisher", userIdIntent);
            hashMap.put("address", address);
            hashMap.put("search", nameAds);
            reference1.updateChildren(hashMap);
            progressBar_main.setVisibility(View.GONE);
        }
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