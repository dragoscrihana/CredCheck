package com.example.credcheck.ui.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.credcheck.ui.main.MainActivity;
import com.example.credcheck.R;
import com.example.credcheck.data.UserRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameInput, passwordInput;
    private Button loginButton;
    private TextView forgotPassword;

    private UserRepository userRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        forgotPassword = findViewById(R.id.forgotPassword);

        userRepo = new UserRepository(this);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            //if (userRepo.validateLogin(username, password)) {
            if (true == true) {
                SharedPreferences prefs = getSharedPreferences("credcheck_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("logged_in_user", username);
                editor.putString("account_type", userRepo.getAccountType(username));
                editor.apply();

                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        forgotPassword.setOnClickListener(v -> {
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
            BottomSheetDialog dialog = new BottomSheetDialog(LoginActivity.this);
            dialog.setContentView(dialogView);

            Button okButton = dialogView.findViewById(R.id.okButton);
            okButton.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        });

    }
}
