package com.example.steganoanalyzer;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;

public class Encoder extends AppCompatActivity {
    private ActivityResultLauncher<String> imagePickerLauncher;
    private static final int REQUEST_IMAGE = 1;

    private EditText dataInputEditText;
    private EditText keyInputEditText;
    private File selectedImageFile;

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

        dataInputEditText = findViewById(R.id.DataInp);
        keyInputEditText = findViewById(R.id.KeyInp);
        ImageButton selectImageButton = findViewById(R.id.OpenImg);
        ImageButton encryptButton = findViewById(R.id.encode);

        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        encryptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                encryptData();
            }
        });

        // Initialize imagePickerLauncher
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            selectedImageFile = new File(result.getPath());
                            Toast.makeText(Encoder.this, "Image selected: " + selectedImageFile.getName(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void openImagePicker() {
        imagePickerLauncher.launch("image/*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageFile = new File(data.getData().getPath());
            Toast.makeText(this, "Image selected: " + selectedImageFile.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void encryptData() {
        String inputData = dataInputEditText.getText().toString().trim();
        String encryptionKey = keyInputEditText.getText().toString().trim();

        if (inputData.isEmpty() || encryptionKey.isEmpty() || selectedImageFile == null) {
            Toast.makeText(this, "Please provide data, encryption key, and select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize Python interpreter
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python py = Python.getInstance();
        PyObject pyObj = py.getModule("image_encoder");

        // Call the Python function for image encryption
        PyObject result = pyObj.callAttr("encode_img_data", selectedImageFile.getAbsolutePath(), inputData, encryptionKey, getOutputImagePath());

        if (result.toBoolean()) {
            Toast.makeText(this, "Data encrypted successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error: Encryption failed", Toast.LENGTH_SHORT).show();
        }
    }

    private String getOutputImagePath() {
        // Define the output path for the encrypted image
        File outputDir = getExternalFilesDir(null);
        String outputFileName = "encoded.png";  // Default naming with PNG extension
        return new File(outputDir, outputFileName).getAbsolutePath();
    }
}
