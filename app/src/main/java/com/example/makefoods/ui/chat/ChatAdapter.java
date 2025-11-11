package com.example.makefoods.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.makefoods.R;
import com.example.makefoods.model.Message;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT  = 2;

    private final List<Message> items = new ArrayList<>();

    // 외부에서 메시지 목록 전체를 교체
    public void submitList(List<Message> newList) {
        items.clear();
        if (newList != null) items.addAll(newList);
        notifyDataSetChanged();
    }

    // 외부에서 하나씩 추가할 때(옵션)
    public void add(Message msg) {
        items.add(msg);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message m = items.get(position);
        return (m.getSender() == Message.Sender.USER) ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View v = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserMessageVH(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_bot, parent, false);
            return new BotMessageVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message m = items.get(position);
        if (holder instanceof UserMessageVH) {
            ((UserMessageVH) holder).bind(m);
        } else if (holder instanceof BotMessageVH) {
            ((BotMessageVH) holder).bind(m);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ----- ViewHolders -----
    static class UserMessageVH extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        UserMessageVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        void bind(Message m) {
            tvMessage.setText(m.getText());
        }
    }

    static class BotMessageVH extends RecyclerView.ViewHolder {
        private final TextView tvMessage;
        BotMessageVH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
        void bind(Message m) {
            tvMessage.setText(m.getText());
        }
    }
}
