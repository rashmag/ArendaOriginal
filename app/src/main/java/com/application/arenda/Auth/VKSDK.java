package com.application.arenda.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.application.arenda.MainActivity;
import com.application.arenda.R;
import com.vk.api.sdk.VK;
import com.vk.api.sdk.auth.VKAccessToken;
import com.vk.api.sdk.auth.VKAuthCallback;
import com.vk.api.sdk.auth.VKScope;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class VKSDK extends AppCompatActivity {
    ArrayList<VKScope> arrayList = new ArrayList<>();
    VKScope[] scope = new VKScope[]{VKScope.WALL, VKScope.PHOTOS};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_v_k_s_d_k);

        VK.login(VKSDK.this, Arrays.asList(scope));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(!VK.onActivityResult(requestCode, resultCode, data, new VKAuthCallback() {
            @Override
            public void onLogin(@NotNull VKAccessToken vkAccessToken) {
                startActivity(new Intent(VKSDK.this, MainActivity.class));
            }

            @Override
            public void onLoginFailed(int i) {
                Toast.makeText(VKSDK.this, "Fall", Toast.LENGTH_SHORT).show();
            }
        }));
    }
}