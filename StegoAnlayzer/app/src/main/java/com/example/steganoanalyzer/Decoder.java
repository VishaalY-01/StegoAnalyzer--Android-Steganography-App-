package com.example.steganoanalyzer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

public class Decoder extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageButton openImgBtn;
    private EditText keyInput;
    private Bitmap selectedImage;
    private ActivityResultLauncher<String> imageChooserLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_decoder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton back;
        back =findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        openImgBtn = findViewById(R.id.OpenImg);
        keyInput = findViewById(R.id.KeyInp);

        // Initialize the ActivityResultLauncher in onCreate
        imageChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        openImgBtn.setOnClickListener(view -> openImageChooser());

        findViewById(R.id.decode).setOnClickListener(view -> decryptData());
    }

    private void decryptData() {
        String key = keyInput.getText().toString();

        if (key.isEmpty() || selectedImage == null) {
            Toast.makeText(this, "Please enter key and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        String decryptedData = decodeTextFromImage(selectedImage, key);

        // Display the decrypted data to the user
        Toast.makeText(this, "Decrypted Data: " + decryptedData, Toast.LENGTH_LONG).show();
        TextView resultBox = findViewById(R.id.Result);
        resultBox.setText(decryptedData);
    }

    private void openImageChooser() {
        // Launch the image chooser using the ActivityResultLauncher
        imageChooserLauncher.launch("image/*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            try {
                Uri imageUri = data.getData();
                selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String decodeTextFromImage(Bitmap bitmap, String key) {
        if (bitmap == null || key.isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        StringBuilder binaryData = new StringBuilder();

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);

                binaryData.append(getBit(Color.red(pixel)));
                binaryData.append(getBit(Color.green(pixel)));
                binaryData.append(getBit(Color.blue(pixel)));
            }
        }

        String data = binaryToText(binaryData.toString());

        // Extract the original data using the key
        String dataWithKey = binaryToText(binaryData.toString());
        int keyIndex = dataWithKey.indexOf(key + "*^*^*");
        if (keyIndex == -1) {
            Toast.makeText(this, "Invalid key", Toast.LENGTH_SHORT).show();
            return null;
        }
        String originalData = dataWithKey.substring(0, keyIndex);

        return originalData;
    }

    private char getBit(int colorComponent) {
        return (colorComponent & 0x01) == 0 ? '0' : '1';
    }

    private String binaryToText(String binary) {
        StringBuilder text = new StringBuilder();
        int index = 0;
        int binaryLength = binary.length();
        while (index + 8 <= binaryLength) { // Check if index + 8 is within bounds
            String charBinary = binary.substring(index, index + 8);
            text.append((char) Integer.parseInt(charBinary, 2));
            index += 8;
        }
        return text.toString();
    }
}

