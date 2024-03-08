package com.example.steganoanalyzer;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private ArrayList<String> tipsList;
    private ArrayList<String> displayedTipsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ImageView next_b;
        ImageView StegoImg;
        TextView Stego;
        TextView text2;
        TextView tip;
        TextView Tips;
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tip = findViewById(R.id.tip);
        Tips = findViewById(R.id.Tips);
        next_b = findViewById(R.id.next_b);
        StegoImg = findViewById(R.id.StegoImg);
        Stego = findViewById(R.id.Stego);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        StegoImg.startAnimation(fadeInAnimation);
        Stego.startAnimation(fadeInAnimation);
        next_b.startAnimation(fadeInAnimation);
        tip.startAnimation(fadeInAnimation);
        Tips.startAnimation(fadeInAnimation);


        // Initialize lists
        tipsList = new ArrayList<>();
        displayedTipsList = new ArrayList<>();
        readTipsFromFile(); // Call to populate tipsList
        displayRandomTip(tip);
    }


    private void readTipsFromFile() {
        AssetManager assetManager = getAssets();
        try {
            InputStream inputStream = assetManager.open("cybersecurity_tips.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                tipsList.add(line);
            }
            bufferedReader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle file reading error here (e.g., display a message to the user)
        }
    }


    private void displayRandomTip(TextView tipsTextView) {
        if (tipsList.isEmpty()) {
            tipsTextView.setText("No tips available.");
            return;
        }

        Random random = new Random();
        int index = random.nextInt(tipsList.size());
        String randomTip = tipsList.get(index);

        // Check if the tip has already been displayed
        if (!displayedTipsList.contains(randomTip)) {
            // Display the random tip
            tipsTextView.setText(randomTip);
            // Add the tip to the displayed list
            displayedTipsList.add(randomTip);
        } else {
            // If the tip has been displayed, choose another random tip recursively
            displayRandomTip(tipsTextView);
        }

        // Check if all tips have been displayed
        if (displayedTipsList.size() == tipsList.size()) {
            // Reset the displayed list to start again
            displayedTipsList.clear();
        }
    }


    public void  openActivity(View v){
        Intent intent=new Intent(this, ContentPage.class);
        startActivity(intent);
    }
}
