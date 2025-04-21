package com.example.peerchat;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatLogActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatLogAdapter adapter;
    private ChatDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_log);

        recyclerView = findViewById(R.id.recyclerViewChatLog);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // back button
        ImageButton backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> {
            finish(); // closes the activity
        });

        dbHelper = new ChatDbHelper(this);

        List<ChatLogEntry> messages = dbHelper.getAllMessages();
        adapter = new ChatLogAdapter(messages);
        recyclerView.setAdapter(adapter);
    }
}
