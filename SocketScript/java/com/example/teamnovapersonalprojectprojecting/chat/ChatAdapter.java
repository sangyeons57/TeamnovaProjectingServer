package com.example.teamnovapersonalprojectprojecting.chat;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.activity.project.ProjectJoinDialogFragment;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.UserData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatItem> chatList;  //recylerview에 실제 표시되는 값들
    private Queue<ChatItem> notReadyChat;   // 서버로 부터 응답이 돌아오지 않은 CHatItem
    private Map<Integer, ChatItem> chatItemMap;  //chatId로 ChatItme을 가져올 수 있게 해둔 것

    private FragmentManager fragmentManager;

    public ChatAdapter(List<ChatItem> chatList, FragmentManager fragmentManager) {
        this.chatList = chatList;
        notReadyChat = new LinkedList<>();
        chatItemMap = new HashMap<>();
        this.fragmentManager =fragmentManager;
    }

    public static class ChatItem {
        public int id;
        public int chatId;
        public int userId;
        public String message;
        public String dateTime;
        public boolean isModified;


        public ChatItem(int id, int chatId, int userId, String message, String dateTime, Boolean isModified) {
            this.id = id;
            this.chatId = chatId;
            this.userId = userId;
            this.message = message;
            this.dateTime = dateTime;
            this.isModified = isModified;
        }

        public ChatItem setValue(ChatItem other){
            this.id = other.id;
            this.chatId = other.chatId;
            this.userId = other.userId;
            this.message = other.message;
            this.dateTime = other.dateTime;
            this.isModified = other.isModified;
            return this;
        }

        @NonNull
        @Override
        public String toString() {
            return "id: " + id + " chatId: " + chatId + " userId: " + userId + " message: " + message + " dateTime: " + dateTime + " isModified: " + isModified;
        }
    }

    public void addChat(int index, ChatItem chatItem) {
        if(chatItem == null || chatItem.userId == DataManager.SYSTEM_ID){
            return;
        }
        Log.d("chatAdapter", chatItem.toString() );

        if(chatItem.chatId != DataManager.NOT_SETUP_I
                && chatItemMap.containsKey(chatItem.chatId)){
            chatItemMap.get(chatItem.chatId).setValue(chatItem);
        } else {
            chatList.add(index,chatItem);
            chatItemMap.put(chatItem.chatId, chatItem);
        }
    }

    public void addChat(ChatItem chatItem) {
        addChat(chatList.size(), chatItem);
    }

    public void addNotReadyChat(int index ,ChatItem chatItem){
        notReadyChat.add(chatItem);
        addChat(index, chatItem);
    }

    public void addChatMessages(Cursor cursor){
        int id = cursor.getInt(0);
        int chatId = cursor.getInt(2);
        int writerId = cursor.getInt(3);
        String data = cursor.getString(4);
        String lastTime = cursor.getString(5);
        boolean isModified = (cursor.getInt(6) == 1) ? true : false;

        ChatItem chatItem = new ChatItem(
                id,
                chatId,
                writerId,
                data,
                lastTime,
                isModified
                );
        addChat(chatItem);
    }

    /**
     *
     * notreadyChat 테이블에서하나 가져옴
     * notReadyChat은 사용자에게 보여주었으나 서버로 부터 보네졌다는 응답이
     * 돌아오지 않은 chat들을 모아둔 queue이다.
     */
    public ChatItem pollNotReadyChat(){
        return notReadyChat.poll();
    }

    public int getPosition (ChatItem chatItem){
        return chatList.indexOf(chatItem);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public ImageView profileImageView;
        public TextView nameTextView;
        public TextView messageTextView;
        public TextView dateTextView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chatItem = chatList.get(position);

        holder.profileImageView.setImageResource(R.drawable.ic_account_black_24dp);
        holder.messageTextView.setText(chatItem.message);
        holder.dateTextView.setText(chatItem.dateTime);

        UserData userData = DataManager.getUserData(chatItem.userId);
        if(userData.profileImagePath != null && !userData.profileImagePath.isEmpty()) {
            DB_FileList.setFileImage(holder.profileImageView, userData.profileImagePath);
        }
        holder.nameTextView.setText(userData.username);


        setClickableUrl(holder.messageTextView, chatItem.message);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
    private void setClickableUrl(TextView textview, String message){
        SpannableString spannable = new SpannableString(message);
        Matcher matcher = DataManager.Instance().urlPattern.matcher(message);

        // url이 없을경우 실행 안함
        if (!matcher.find()) {
            return;
        }
        matcher.reset();
        while (matcher.find()) {
            Uri uri = Uri.parse(matcher.group());
            String path;
            if((path = uri.getPath()) != null && uri.getHost().equals(SocketConnection.SERVER_ADDRESS)){

                String token;
                if (path.equals("/invite") && (token = uri.getQueryParameter("token")) != null) {
                    setClickableSpan(spannable, matcher.start(), matcher.end(), new ClickableSpan() {
                        @Override
                        public void onClick(@NonNull View widget) {
                            ProjectJoinDialogFragment.Instance(token)
                                    .show(fragmentManager, "ProjectJoinDialogFragment");
                        }
                    });
                }
            } else {
                setClickableSpan(spannable, matcher.start(), matcher.end(), new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri.toString()));
                        widget.getContext().startActivity(intent);
                    }
                });
            }
        }

        textview.setText(spannable);
        textview.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setClickableSpan(SpannableString spannableString, int start, int end, ClickableSpan clickableSpan) {
        spannableString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
}