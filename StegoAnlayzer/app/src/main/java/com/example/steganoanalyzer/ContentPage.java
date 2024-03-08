package com.example.steganoanalyzer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ContentPage extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_content_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        ImageButton btnEncoder = findViewById(R.id.Encoder);
        btnEncoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContentPage.this, Encoder.class);
                startActivity(intent);
            }
        });

        ImageButton btnDecoder = findViewById(R.id.Decoder);
        btnDecoder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContentPage.this, Decoder.class);
                startActivity(intent);
            }
        });

        // Create a callback for onBackPressed
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    // If already pressed once, exit the app
                    finishAffinity();
                    System.exit(0);
                } else {
                    doubleBackToExitPressedOnce = true;
                    Toast.makeText(ContentPage.this, "Press back again to exit", Toast.LENGTH_SHORT).show();

                    // Reset the flag after a delay (2 seconds)
                    new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        };

        // Add the callback to the onBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
