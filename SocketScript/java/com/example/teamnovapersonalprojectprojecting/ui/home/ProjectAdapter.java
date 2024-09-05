package com.example.teamnovapersonalprojectprojecting.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teamnovapersonalprojectprojecting.activity.project.EditCategoryDialogFragment;
import com.example.teamnovapersonalprojectprojecting.activity.project.EditChannelDialogFragment;
import com.example.teamnovapersonalprojectprojecting.chat.ChatActivity;
import com.example.teamnovapersonalprojectprojecting.R;
import com.example.teamnovapersonalprojectprojecting.socket.SocketConnection;
import com.example.teamnovapersonalprojectprojecting.socket.SocketEventListener;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.CategoryViewHolder> {
    public static final int TYPE_CATEGORY = 0;
    public static final int TYPE_CHANNEL = 1;

    private List<CategoryItem> itemList;
    private FragmentManager fragmentManager;

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewTitle;
        public TextView textViewContent;
        public LinearLayout expandableLayout;

        public CategoryViewHolder(View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewContent = itemView.findViewById(R.id.textview_content);
            expandableLayout = itemView.findViewById(R.id.expandableLayout);
        }

    }

    public ProjectAdapter(List<CategoryItem> itemList, FragmentManager fragmentManager) {
        this.itemList = itemList;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_channellist, parent, false);
        return new CategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = itemList.get(position);
        holder.textViewTitle.setText(item.getName());

        holder.expandableLayout.removeAllViews();
        for (ChannelItem channelItem: item.getStore()) {
            LayoutInflater inflater = LayoutInflater.from(holder.expandableLayout.getContext());
            View itemView = inflater.inflate(R.layout.item_channel, holder.expandableLayout, false);

            TextView itemText = itemView.findViewById(R.id.textview_content);
            itemText.setText(channelItem.name);
            itemText.setOnClickListener((view)-> {
                    SocketConnection.sendMessage(new JsonUtil()
                            .add(JsonUtil.Key.TYPE, SocketEventListener.eType.JOIN_CHANNEL)
                            .add(JsonUtil.Key.CHANNEL_ID, channelItem.id));
            });

            itemText.setOnLongClickListener((view)->{
                EditChannelDialogFragment.Instance(DataManager.Instance().projectId, item.categoryId, channelItem.id, channelItem.name)
                        .show(fragmentManager, "EditCategoryDialogFragment");
                return false;
            });

            holder.expandableLayout.addView(itemView);
        }


        boolean isExpanded = item.isExpanded();
        holder.expandableLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.textViewTitle.setOnClickListener(v -> {
            item.setExpanded(!item.isExpanded());
            holder.expandableLayout.setVisibility(item.isExpanded() ? View.VISIBLE : View.GONE);
            notifyItemChanged(position);
        });

        holder.textViewTitle.setOnLongClickListener(v -> {
            EditCategoryDialogFragment.Instance(DataManager.Instance().projectId, item.categoryId, item.name)
                    .show(fragmentManager, "EditChannelDialogFragment");
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class CategoryItem {
        private int categoryId;
        private String name;
        private List<ChannelItem> store;
        private boolean expanded;

        public CategoryItem(int categoryId, String name, List<ChannelItem> store) {
            this.categoryId = categoryId;
            this.name = name;
            this.store = store;
            this.expanded = false;
        }

        public String getName() {
            return name;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public List<ChannelItem> getStore() {
            return store;
        }
        public void addChannel(ChannelItem channelItem){
            store.add(channelItem);
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setName(String name) {
            this.name = name;
        }
        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public ChannelItem getChannelItem(int channelId){
            return store.stream().filter(channelItem -> channelItem.id == channelId).findFirst().orElse(null);
        }

        public boolean removeChannelItem(int channelId){
            return store.removeIf(channelItem -> channelItem.id == channelId);
        }
    }

    public static class ChannelItem{
        private int id;
        private String name;
        public ChannelItem(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public void setName(String name) {
            this.name = name;
        }
    }
}

