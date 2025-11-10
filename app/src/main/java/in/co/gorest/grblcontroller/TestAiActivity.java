package in.co.gorest.grblcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import in.co.gorest.grblcontroller.util.DashScopeText2Image;

public class TestAiActivity extends AppCompatActivity {

    EditText editPrompt;
    Button btnGenerate;
    ProgressBar loading;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testai);

        editPrompt = findViewById(R.id.editPrompt);
        btnGenerate = findViewById(R.id.btnGenerate);
        loading = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.imageView);

        btnGenerate.setOnClickListener(v -> {
            String prompt = editPrompt.getText().toString().trim();
            if (prompt.isEmpty()) {
                Toast.makeText(this, "请输入提示词", Toast.LENGTH_SHORT).show();
                return;
            }

            loading.setVisibility(View.VISIBLE);
            DashScopeText2Image.generateImage(prompt, new DashScopeText2Image.ResultCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        Glide.with(TestAiActivity.this).load(imageUrl).into(imageView);
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        loading.setVisibility(View.GONE);
                        Toast.makeText(TestAiActivity.this, "错误: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        });
    }


}



