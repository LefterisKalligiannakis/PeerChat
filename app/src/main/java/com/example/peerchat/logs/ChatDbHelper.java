package com.example.peerchat.logs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ChatDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "chat.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "messages";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_IS_ME = "is_me";

    public ChatDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_TIMESTAMP + " INTEGER, " +
                COLUMN_MESSAGE + " TEXT, " +
                COLUMN_IS_ME + " INTEGER)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertMessage(long timestamp, String message, boolean isMe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TIMESTAMP, timestamp);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_IS_ME, isMe ? 1 : 0);
        db.insert(TABLE_NAME, null, values);
    }

    public List<ChatLogEntry> getAllMessages() {
        List<ChatLogEntry> messages = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                COLUMN_TIMESTAMP + " DESC"
        );
        while (cursor.moveToNext()) {
            String msg = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
            boolean isMe = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ME)) == 1;
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

            ChatLogEntry entry = new ChatLogEntry(msg, isMe, timestamp);
            messages.add(entry);
        }

        cursor.close();
        return messages;
    }

}

