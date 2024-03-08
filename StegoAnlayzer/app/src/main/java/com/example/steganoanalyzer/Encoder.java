package com.example.steganoanalyzer;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Encoder extends AppCompatActivity {
    private ActivityResultLauncher<String> imageChooserLauncher;
    private ImageButton openImgBtn;
    private EditText dataInput, keyInput;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_encoder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and UI elements
        openImgBtn = findViewById(R.id.OpenImg);
        dataInput = findViewById(R.id.DataInp);
        keyInput = findViewById(R.id.KeyInp);

        ImageButton back;
        back =findViewById(R.id.back_button);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Initialize ActivityResultLauncher for image chooser
        imageChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            Toast.makeText(this, "Image loaded successfully", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        openImgBtn.setOnClickListener(view -> openImageChooser());
        findViewById(R.id.encode).setOnClickListener(view -> encryptData());
    }

    private void encryptData() {
        String data = dataInput.getText().toString();
        String key = keyInput.getText().toString();

        if (data.isEmpty() || key.isEmpty() || selectedImage == null) {
            Toast.makeText(this, "Please enter data, key, and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap encryptedImage = encodeTextToImage(selectedImage, data, key);

        saveImageToFile(encryptedImage);
    }

    private void saveImageToFile(Bitmap bitmap) {
        try {
            // Get the directory for the public external storage
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!directory.exists()) {
                directory.mkdirs(); // Create the directory if it doesn't exist
            }

            // Generate a unique filename based on current time
            String fileName = "encoded_" + System.currentTimeMillis() + ".png";
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Tell the MediaScanner about the new file so that it appears in the gallery
            MediaScannerConnection.scanFile(this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/png"},
                    null);

            Toast.makeText(this, "Image encrypted and saved as " + fileName, Toast.LENGTH_SHORT).show();

            // Start a new activity and pass the image filename as an extra
            Intent intent = new Intent(this, EncoderSuccess.class);
            intent.putExtra("imageFileName", fileName);
            startActivity(intent);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImageChooser() {
        // Launch the image chooser using the ActivityResultLauncher
        imageChooserLauncher.launch("image/*");
    }

    private Bitmap encodeTextToImage(Bitmap bitmap, String data, String key) {
        if (bitmap == null || data.isEmpty() || key.isEmpty()) {
            throw new IllegalArgumentException("Invalid input parameters");
        }

        String binaryData = stringToBinary(data + key + "*^*^*");

        Bitmap encodedImage = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        int index = 0;
        for (int y = 0; y < encodedImage.getHeight(); y++) {
            for (int x = 0; x < encodedImage.getWidth(); x++) {
                if (index < binaryData.length()) {
                    int pixel = encodedImage.getPixel(x, y);

                    int newPixel = Color.rgb(
                            modifyColorComponent(Color.red(pixel), getBit(binaryData, index)),
                            modifyColorComponent(Color.green(pixel), getBit(binaryData, index + 1)),
                            modifyColorComponent(Color.blue(pixel), getBit(binaryData, index + 2))
                    );

                    encodedImage.setPixel(x, y, newPixel);
                    index += 3;
                } else {
                    break; // Break the loop if all data has been encoded
                }
            }
        }

        return encodedImage;
    }

    private char getBit(String binaryData, int index) {
        if (index < binaryData.length()) {
            return binaryData.charAt(index);
        } else {
            return '0'; // Default to '0' if index is out of bounds
        }
    }


    private String stringToBinary(String input) {
        StringBuilder binary = new StringBuilder();
        for (char c : input.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    private int modifyColorComponent(int colorComponent, char bit) {
        int modifiedComponent = colorComponent & 0xFE;
        if (bit == '1') {
            modifiedComponent |= 0x01;
        }
        return modifiedComponent;
    }
}
