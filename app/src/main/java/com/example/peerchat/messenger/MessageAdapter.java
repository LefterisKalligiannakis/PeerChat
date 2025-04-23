package com.example.peerchat.messenger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.peerchat.R;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<String> {

    private final Context context;

    public MessageAdapter(Context context, List<String> messages) {
        super(context, R.layout.item_message, messages);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        String message = getItem(position);
        TextView textView = convertView.findViewById(R.id.textViewMessage);
        textView.setText(message != null ? message : "");

        return convertView;
    }
}