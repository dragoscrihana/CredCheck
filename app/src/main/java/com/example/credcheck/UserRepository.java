package com.example.credcheck;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.credcheck.model.User;
import com.example.credcheck.util.Utils;

public class UserRepository {
    private final UserDatabaseHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new UserDatabaseHelper(context);
        initializeUsers();
    }

    private void initializeUsers() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (getUser("hospital") == null) {
            insertUser(new User("hospital", Utils.hashPassword("hospital123"), "hospital"));
        }
        if (getUser("restaurant") == null) {
            insertUser(new User("restaurant", Utils.hashPassword("restaurant123"), "restaurant"));
        }
    }

    public void insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_USERNAME, user.getUsername());
        values.put(UserDatabaseHelper.COLUMN_PASSWORD, user.getPassword());
        values.put(UserDatabaseHelper.COLUMN_TYPE, user.getAccountType());
        db.insert(UserDatabaseHelper.TABLE_USERS, null, values);
    }

    public User getUser(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(UserDatabaseHelper.TABLE_USERS, null,
                UserDatabaseHelper.COLUMN_USERNAME + " = ?",
                new String[]{username}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String password = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_PASSWORD));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(UserDatabaseHelper.COLUMN_TYPE));
            cursor.close();
            return new User(username, password, type);
        }
        return null;
    }

    public boolean validateLogin(String username, String password) {
        User user = getUser(username);
        if (user == null) return false;

        String hashedInput = Utils.hashPassword(password);
        return user.getPassword().equals(hashedInput);
    }

    public String getAccountType(String username) {
        User user = getUser(username);
        return user != null ? user.getAccountType() : null;
    }
}
