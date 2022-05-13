package com.application.arenda.Chat;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.arenda.Model.ModelAll;
import com.application.arenda.R;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolderChat extends RecyclerView.Adapter<ViewHolderChat.RecyclerViewHolder> {
    private View itemView;
    private ChatUsersList chatUsersList;
    private List<ModelAll> modelAuths;
    String theLastMessage, postId;

    public ViewHolderChat(ChatUsersList chatUsersList, List<ModelAll> modelAuths) {
        this.chatUsersList = chatUsersList;
        this.modelAuths = modelAuths;
    }


    public ViewHolderChat() {
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imageViewChatAds, imageViewChatUser;
        TextView tvItemNameChat, last_msg;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewChatUser = itemView.findViewById(R.id.imageViewChatUser);
            imageViewChatAds = itemView.findViewById(R.id.imageViewChatAds);
            tvItemNameChat = itemView.findViewById(R.id.tvItemNameChat);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    @NonNull
    @Override
    public RecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat,
                parent, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
        ModelAll modelAuth = modelAuths.get(position);
        holder.tvItemNameChat.setText(modelAuth.getName());
        if (modelAuth.getPublisher().equals(FirebaseAuth.getInstance().
                getCurrentUser().getUid())) {
            holder.imageViewChatUser.setVisibility(View.GONE);
        } else {
            String urlImgUser = modelAuth.getPublisher();
            if (urlImgUser != null) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ModelAll user = snapshot.getValue(ModelAll.class);
                            if (urlImgUser.equals(user.getId())) {
                                if (user.getUserPhotoUri() != null) {
                                    if (chatUsersList.getActivity() != null) {
                                        Glide.with(chatUsersList).load(user.getUserPhotoUri())
                                                .into(holder.imageViewChatUser);
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }
        lastMessage(holder.last_msg, holder.imageViewChatAds, holder, modelAuth);
        getAdsPhoto(modelAuth.getPostId(), holder.imageViewChatAds);
    }

    @Override
    public int getItemCount() {
        return modelAuths.size();
    }

    private void lastMessage(TextView last_msg, ImageView imageViewChatAds,
                             RecyclerView.ViewHolder viewHolder, ModelAll modelAuth) {
        final String[] postIdIntent = {null};
        final String[] publisher = {null};
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(chatUsersList.getActivity(), MessageActivity.class);
                intent.putExtra("postId", modelAuth.getPostId());
                intent.putExtra("userId", publisher[0]);
                if (modelAuth.getPublisher().equals(FirebaseAuth.getInstance().
                        getCurrentUser().getUid())) {
                    intent.putExtra("myPost", true);
                } else {
                    intent.putExtra("myPost", false);
                }
                chatUsersList.getActivity().startActivity(intent);
            }
        });
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chat");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int testP = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    ModelAll modelMessage = dataSnapshot.getValue(ModelAll.class);
                    Log.d("test23", "Вошли0");
                    if (modelMessage.getUserId().equals(firebaseUser.getUid()) && modelMessage
                            .getReceiver().equals(modelAuth.getPostId()) ||
                            modelMessage.getReceiver().equals(modelAuth.getPostId()) &&
                                    modelMessage.getSender().equals(firebaseUser.getUid())) {
                        Log.d("test23", "Вошли1");
                        postId = modelMessage.getPostId();
                        testP++;
                        if(testP == 1){
                            if(!modelMessage.getSender().equals(firebaseUser.getUid())){
                                publisher[0] = modelMessage.getSender();
                                Log.d("testMes","publisher0 = " + publisher[0] + " userId = " +
                                        firebaseUser.getUid());
                            }else if(!modelMessage.getUserId().equals(firebaseUser.getUid())){
                                publisher[0] = modelMessage.getUserId();
                                Log.d("testMes","publisher1 = " + publisher[0] + " userId = " +
                                        firebaseUser.getUid());
                            }
                        }
                        theLastMessage = modelMessage.getMessage();
                        if (modelAuth.getPostId() != null) {
                            postIdIntent[0] = modelAuth.getPostId();
                            if (chatUsersList.getActivity() != null) {
                            }
                        }
                    }
                }

                switch (theLastMessage) {
                    case "default":
                        last_msg.setText("Нет сообщений");
                        break;
                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }
                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getAdsPhoto(String postId, ImageView imageViewChatAds) {
        final int[] count = {0};
        DatabaseReference referencePost = FirebaseDatabase.getInstance().
                getReference("Post").
                child(postId).child("arrayimagesurl");
        referencePost.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Log.d("onegingek", "count0 = " + count[0] + " postId = " +
                            postId);
                    if (count[0] != -1000) {
                        Log.d("onegingek", "count1 = " + count[0]);
                        count[0]++;
                        if (count[0] == 1) {
                            Glide.with(chatUsersList)
                                    .load(String.valueOf(String.valueOf(snapshot1.child("adsurl").getValue())))
                                    .into(imageViewChatAds);
//                        holder.setImageView(getActivity().getApplication(),
//                                String.valueOf(snapshot1.child("adsurl").getValue()));
                        }
                        if (count[0] == 2) {
                            count[0] = -1000;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
