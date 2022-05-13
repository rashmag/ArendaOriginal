package com.application.arenda.Chat;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.application.arenda.Ads.LentaAds;
import com.application.arenda.Model.ModelAll;
import com.application.arenda.R;
import com.bumptech.glide.Glide;
import com.github.pgreze.reactions.ReactionPopup;
import com.github.pgreze.reactions.ReactionsConfig;
import com.github.pgreze.reactions.ReactionsConfigBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewHolderMessage extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private View itemView;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    public static final int MSG_TYPE_RENT = 2;
    private MessageActivity messageActivity;
    private List<ModelAll> modelMessages;
    private Dialog dialog;
    private String imageUrl;
    private BottomSheetDialog bottomSheetDialog;
    private ImageView sendMessageBtn;
    private FirebaseUser fUser;

    public ViewHolderMessage(MessageActivity messageActivity, List<ModelAll> modelMessages, String imageUrl,
                             ImageView sendMessageBtn) {
        this.messageActivity = messageActivity;
        this.imageUrl = imageUrl;
        this.sendMessageBtn = sendMessageBtn;
        this.modelMessages = modelMessages;
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        CircleImageView prodileImage;
        TextView showMessage;
        TextView txtSeen;
        ImageView feeling;

        public RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            prodileImage = itemView.findViewById(R.id.profile_image);
            showMessage = itemView.findViewById(R.id.show_message);
            txtSeen = itemView.findViewById(R.id.txt_seen);
            feeling = itemView.findViewById(R.id.feeling);
        }
    }

    public static class RecyclerViewHolder2 extends RecyclerView.ViewHolder {
        Button allow, refuse;

        public RecyclerViewHolder2(@NonNull View itemView) {
            super(itemView);
            allow = itemView.findViewById(R.id.allow);
            refuse = itemView.findViewById(R.id.refuse);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_right, parent, false);
            itemView.setTag("right");
            return new ViewHolderMessage.RecyclerViewHolder(itemView);
        } else if (viewType == MSG_TYPE_RENT) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_get_rent, parent, false);
            itemView.setTag("rent");
            return new ViewHolderMessage.RecyclerViewHolder2(itemView);
        } else {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_left, parent, false);
            itemView.setTag("left");
            return new ViewHolderMessage.RecyclerViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ModelAll modelMessage = modelMessages.get(position);
        if (holder.getItemViewType() == MSG_TYPE_RENT) {
            RecyclerViewHolder2 holder2 = (RecyclerViewHolder2) holder;

            holder2.allow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child("Chat")
                            .child(modelMessage.getPostId()).child(LentaAds.RENT_KEY).setValue(LentaAds.ALLOW_RENT)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                    }
                                }
                            });
                }
            });

            holder2.refuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseDatabase.getInstance().getReference().child("Chat")
                            .child(modelMessage.getPostId()).child(LentaAds.RENT_KEY).setValue(LentaAds.REFUSE_RENT)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                    }
                                }
                            });
                }
            });
        } else {
            RecyclerViewHolder holder1 = (RecyclerViewHolder) holder;
            holder1.showMessage.setText(modelMessage.getMessage());


            int reactions[] = new int[]{
                    R.drawable.surprised_icon_reacrions,
                    R.drawable.gestures_icon_reacrions,
                    R.drawable.love_icon_reacrions,
                    R.drawable.like_icon_reacrions,
                    R.drawable.sad_icon_reacrions,
                    R.drawable.angry_icon_reacrions
            };
            if (modelMessage.getFeeling() >= 0) {
                if (!modelMessage.getMessage().equals("Данное сообщение удаленно")) {
                    holder1.feeling.setImageResource(reactions[modelMessage.getFeeling()]);
                    holder1.feeling.setVisibility(View.VISIBLE);
                }
            } else {
                holder1.feeling.setVisibility(View.GONE);
            }
            if (!modelMessage.getMessage().
                    equals("Данное сообщение удаленно")) {
                ReactionsConfig config = new ReactionsConfigBuilder(messageActivity)
                        .withReactions(reactions)
                        .build();

                ReactionPopup popup = new ReactionPopup(messageActivity, config, (pos) -> {
                    if (modelMessage.getFeeling() == pos) {
                        FirebaseDatabase.getInstance().getReference().child("Chat")
                                .child(modelMessage.getMessageId())
                                .child("feeling").removeValue();
                    } else {
                        if (pos != -1) {
                            modelMessage.setFeeling(pos);
                            FirebaseDatabase.getInstance().getReference().child("Chat")
                                    .child(modelMessage.getMessageId()).setValue(modelMessage)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                holder1.feeling.setImageResource(reactions[pos]);
                                                holder1.feeling.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    });
                        }
                    }

                    return true; // true is closing popup, false is requesting a new selection
                });
                holder1.showMessage.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!modelMessage.getMessage().equals("Данное сообщение удаленно")) {
                            popup.onTouch(v, event);
                        }
                        return false;
                    }
                });
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (holder.itemView.getTag().equals("right")) {
                        if (!modelMessage.getMessage().equals("Данное сообщение удаленно")) {
                            showDeleteDialogRight(modelMessage);
                        }
                    } else if (holder.itemView.getTag().equals("left")) {
                        if (!modelMessage.getMessage().equals("Данное сообщение удаленно")) {
                            showDeleteDialogLeft(modelMessage);
                        }
                    }
                    return false;
                }
            });
            Glide.with(messageActivity).

                    load(imageUrl).

                    into(holder1.prodileImage);


            if (position == modelMessages.size() - 1) {
                if (modelMessage.getIsseen()) {
                    holder1.txtSeen.setText("Seen");
                } else {
                    holder1.txtSeen.setText("Delivered");
                }
            } else {
                holder1.txtSeen.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public int getItemCount() {
        return modelMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (modelMessages.get(position).getMessage().equals(LentaAds.REQUEST_RENT)
        && modelMessages.get(position).getUserId().equals(fUser.getUid())) {
            return MSG_TYPE_RENT;
        } else if (modelMessages.get(position).getId().equals(fUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }

    private void showDeleteDialogLeft(ModelAll modelMessageDialog) {
        dialog = new Dialog(messageActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_on_long_left_message);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView copyImageLeft = dialog.findViewById(R.id.copyImageLeft);

        copyImageLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyMessage(modelMessageDialog);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void copyMessage(ModelAll modelMessageDialog) {
        ClipboardManager clipboard = (ClipboardManager) messageActivity.
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("", modelMessageDialog.getMessage());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(messageActivity, "Скопировано!", Toast.LENGTH_SHORT).show();
    }

    private void showDeleteDialogRight(ModelAll modelMessageDialog) {
//        bottomSheetDialog = new BottomSheetDialog(messageActivity,R.style.BottomSheet);
//        View v = LayoutInflater.from(messageActivity).inflate(R.layout.bottom_sheet_layout_right,
//                (ViewGroup)messageActivity.findViewById(R.id.bottom_sheet));
        dialog = new Dialog(messageActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_on_long_right_message);
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        TextView cancel = dialog.findViewById(R.id.cancel);
        TextView deleteEveryone = dialog.findViewById(R.id.deleteEveryone);
        TextView deleteForMe = dialog.findViewById(R.id.deleteForMe);
        ImageView editPencil = dialog.findViewById(R.id.editPencil);
        ImageView copyImageRight = dialog.findViewById(R.id.copyImageRight);

        copyImageRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyMessage(modelMessageDialog);
                dialog.dismiss();
            }
        });
        editPencil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageBtn.setTag("pencil");
                messageActivity.editText(modelMessageDialog);

                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        deleteEveryone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                modelMessageDialog.setMessage("Данное сообщение удаленно");
                FirebaseDatabase.getInstance().getReference()
                        .child("Chat")
                        .child(modelMessageDialog.getMessageId()).setValue(modelMessageDialog);

                FirebaseDatabase.getInstance().getReference().child("Chat").child(modelMessageDialog.getMessageId())
                        .child("feeling").removeValue();

                dialog.dismiss();
            }
        });
        deleteForMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                FirebaseDatabase.getInstance().getReference()
//                        .child("Chat")
//                        .child(modelMessageDialog.getMessageId()).setValue(null);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}

