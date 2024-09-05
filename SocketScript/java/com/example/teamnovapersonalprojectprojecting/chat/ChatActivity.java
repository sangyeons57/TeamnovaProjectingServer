package com.example.teamnovapersonalprojectprojecting.chat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.local.database.CursorReturn;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.DB_ChatTable;
import com.example.teamnovapersonalprojectprojecting.local.database.chat.LocalDBChat;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_ChannelList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_DMList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_UserList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.socket.eventList.SendMessage;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final int pageSize = 20;

    public static final String LAST_CHAT_ID = "lastChatId";
    public static final String IS_DM = "isDM";

    private RecyclerView chatRecyclerView;
    private LinearLayoutManager layoutManager;
    private List<ChatAdapter.ChatItem> chatList;
    private ChatAdapter adapter;

    private ImageButton sendButton;
    private ImageButton addMultiMediaButton;
    private EditText messageEditText;
    private TextView channelNameTextView;

    private SocketEventListener.EventListener eventListener;

    private boolean isReadyFirstItem;
    private boolean isAtBottom;
    private boolean isReadyToLoadMoreData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        DataManager.Instance().currentContext = this;

        isReadyFirstItem = false;
        isAtBottom = true;
        isReadyToLoadMoreData = true;

        sendButton = findViewById(R.id.sendButton);
        messageEditText = findViewById(R.id.messageEditText);
        channelNameTextView = findViewById(R.id.channelNameTextView);
        addMultiMediaButton = findViewById(R.id.multiMediaButton);

        chatRecyclerView = findViewById(R.id.chatRecylerView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        chatRecyclerView.setLayoutManager(layoutManager);


        chatList = new ArrayList<>();
        adapter = new ChatAdapter(chatList, getSupportFragmentManager()); // Create your adapter with chat data
        chatRecyclerView.setAdapter(adapter);

        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(oldBottom - bottom > 100){
                    chatRecyclerView.smoothScrollToPosition(0);
                }
            }
        });

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (isReadyToLoadMoreData && newState == RecyclerView.OVER_SCROLL_ALWAYS && !isAtBottom) {
                    loadMoreData(true, false);
                }
            }
        });
        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                //화면이 올라갈떄 (과거 데이터를 볼떄)
                if (layoutManager != null && dy < 0) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                    if (isReadyToLoadMoreData && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                        Log.d("ChatActivity", "visibleItemCount: " + visibleItemCount + ", totalItemCount: " + totalItemCount + ", pastVisibleItems: " + pastVisibleItems);
                        // Load more data
                        loadMoreData(true, false);
                    }
                    isAtBottom = false;
                } else if (layoutManager != null && dy > 0) {
                    isAtBottom = layoutManager.findFirstVisibleItemPosition() == 0;
                }
            }
        });

        Intent intent = getIntent();
        int lastChatId = intent.getIntExtra(LAST_CHAT_ID, 0);
        boolean isDM = intent.getBooleanExtra(IS_DM, true);
        int channelId = DataManager.Instance().channelId;

        Log.d("ChatActivity", "NewLastChatId: " + lastChatId + " BeforeLastChatId: " + LocalDBChat.GetTable(DB_ChatTable.class).getLastChatId(channelId));

        if (lastChatId > LocalDBChat.GetTable(DB_ChatTable.class).getLastChatId(channelId)){
            LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChatByServer(channelId, pageSize, 0);
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHAT_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_CHAT_DATA){
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    loadMoreData(false, true);
                    return false;
                }
            });
        } else {
            LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, pageSize, 0).execute(new CursorReturn.Execute() {
                @Override
                public void run(Cursor cursor) {
                    while (cursor.moveToNext()){
                        adapter.addChatMessages(cursor);
                    }
                    runOnUiThread(() -> { chatRecyclerView.scrollToPosition(0); });
                }
            });
        }


        if(isDM){
            LocalDBMain.GetTable(DB_ChannelList.class).addChanelListByServer(DataManager.Instance().channelId);
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHANNEL_DATA, new SocketEventListener.EventListener(){
                @Override
                public boolean run(JsonUtil jsonUtil) {
                    int otherId = LocalDBMain.GetTable(DB_DMList.class).getOtherId(DataManager.Instance().channelId);
                    String title = LocalDBMain.GetTable(DB_UserList.class).getUsername(otherId);

                    DataManager.Instance().mainHandler.post(()-> channelNameTextView.setText(title));

                    SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.GET_CHAT_DATA, this);
                    return false;
                }
            });
        } else {
            LocalDBMain.GetTable(DB_ChannelList.class).addChanelListByServer(DataManager.Instance().channelId);
            SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHANNEL_DATA, new SocketEventListener.EventListenerOnce(SocketEventListener.eType.GET_CHANNEL_PROJECT){
                @Override
                public boolean runOnce(JsonUtil jsonUtil) {
                    channelNameTextView.setText(jsonUtil.getString(JsonUtil.Key.CHANNEL_NAME, DataManager.NOT_SETUP_S));
                    return false;
                }
            });
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(messageEditText.getText().toString().trim().equals("")) {
                    return ;
                }

                adapter.addNotReadyChat(0, new ChatAdapter.ChatItem(
                        DataManager.NOT_SETUP_I,
                        DataManager.NOT_SETUP_I,
                        DataManager.Instance().userId,
                        messageEditText.getText().toString(),
                        "",
                        false
                ));

                adapter.notifyDataSetChanged();
                chatRecyclerView.scrollToPosition(0);

                SocketConnection.sendMessage(new JsonUtil()
                                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.SEND_MESSAGE)
                                .add(JsonUtil.Key.MESSAGE, messageEditText.getText().toString())
                                .add(JsonUtil.Key.USER_ID, DataManager.Instance().userId)
                                .add(JsonUtil.Key.USERNAME, DataManager.Instance().username));

                messageEditText.setText("");
                messageEditText.clearFocus();
            }
        });

        SocketEventListener.addAddEventQueue(SocketEventListener.eType.SEND_MESSAGE, eventListener = (jsonUtil)->{
             int chatId = jsonUtil.getInt(JsonUtil.Key.CHAT_ID, 0);
             int writerId = jsonUtil.getInt(JsonUtil.Key.USER_ID, 0);
             String message = jsonUtil.getString(JsonUtil.Key.MESSAGE, "");
             String lastTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "");
             boolean isModified = jsonUtil.getBoolean(JsonUtil.Key.IS_MODIFIED, false);
             long id = SendMessage.lastChatId;

             if(jsonUtil.getBoolean(JsonUtil.Key.IS_SELF, false)) {
                ChatAdapter.ChatItem chatItem = adapter.pollNotReadyChat();
                if (chatItem != null) {
                    chatItem.dateTime = jsonUtil.getString(JsonUtil.Key.DATETIME, "0000.00.00 ER00:00");
                    chatItem.message = message;
                    chatItem.isModified = isModified;
                    chatItem.chatId = chatId;
                    chatItem.userId = writerId;
                    chatItem.id = (int) id;
                    runOnUiThread(() -> {
                        adapter.notifyItemChanged(0);
                        chatRecyclerView.smoothScrollToPosition(0);
                    });
                }
            } else {
                adapter.addChat(0, new ChatAdapter.ChatItem(
                        (int) id,
                        chatId,
                        writerId,
                        message,
                        lastTime,
                        isModified
                ));
                 runOnUiThread(() -> {
                     adapter.notifyItemInserted(0);
                     if(isAtBottom){
                         chatRecyclerView.smoothScrollToPosition(0);
                     }
                 });
            }
            return false;
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.SEND_MESSAGE, eventListener);
        DataManager.Instance().channelId = DataManager.NOT_SETUP_I;

        SocketConnection.sendMessage(new JsonUtil()
                .add(JsonUtil.Key.TYPE, SocketEventListener.eType.EXIT_CHANNEL));
    }
    private void loadMoreData(boolean isNotifyData, boolean isScrollToBottom){
        int channelId = DataManager.Instance().channelId;
        int offset = chatList.size();
        int limit = chatList.size() % pageSize == 0 ? pageSize :  pageSize - chatList.size() % pageSize;
        Log.d("ChatActivity", "limit: " + limit + ", offset: " + offset);

        LocalDBChat.GetTable(DB_ChatTable.class).getChatDataRangeFromBack(channelId, limit, offset).execute(new CursorReturn.Execute() {
            @Override
            public void run(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    do{
                        adapter.addChatMessages(cursor);
                    } while (cursor.moveToNext());
                    chatRecyclerView.post(()->{
                        if(isNotifyData) {
                            adapter.notifyItemRangeInserted(offset, cursor.getCount());
                        }
                        if(isScrollToBottom){
                            chatRecyclerView.scrollToPosition(0);
                        }
                    });
                    isReadyFirstItem = true;
                } else {
                    isReadyToLoadMoreData = false;
                    LocalDBChat.GetTable(DB_ChatTable.class).addOrUpdateChatByServer(channelId, limit, offset);
                    SocketEventListener.addAddEventQueue(SocketEventListener.eType.GET_CHAT_DATA, new SocketEventListener.EventListener(){
                        @Override
                        public boolean run(JsonUtil jsonUtil) {
                            JSONArray data = jsonUtil.getJsonArray(JsonUtil.Key.DATA, new JSONArray());
                            if( data.length() != 0 ) { isReadyToLoadMoreData = true; }
                            SocketEventListener.addRemoveEventQueue(SocketEventListener.eType.GET_CHAT_DATA, this);
                            return false;
                        }
                    });
                }
            }
        });
    }


    private ActivityResultLauncher<Intent> someActivityResultLauncher;
    // 이미지 선택을 위한 인텐트를 시작하는 메서드
    private void openImageChooser() {

    }



}
