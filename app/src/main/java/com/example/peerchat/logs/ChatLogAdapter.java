package com.example.peerchat.logs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peerchat.R;

public class ChatLogAdapter extends RecyclerView.Adapter<ChatLogAdapter.ViewHolder> {
    private final List<ChatLogEntry> chatLogs;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public ChatLogAdapter(List<ChatLogEntry> chatLogs) {
        this.chatLogs = chatLogs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView messageView, timestampView;

        public ViewHolder(View itemView) {
            super(itemView);
            messageView = itemView.findViewById(R.id.textMessage);
            timestampView = itemView.findViewById(R.id.textTimestamp);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ChatLogEntry log = chatLogs.get(position);
        holder.messageView.setText((log.isMe() ? "Me: " : "Peer: ") + log.getMessage());

        long timestampInMillis = Long.parseLong(log.getTimestamp());
        Date parsedDate = new Date(timestampInMillis); // convert to Date

        // format date
        holder.timestampView.setText(sdf.format(parsedDate));

    }


    @Override
    public int getItemCount() {
        return chatLogs.size();
    }
}

