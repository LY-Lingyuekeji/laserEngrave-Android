package in.co.gorest.grblcontroller;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import in.co.gorest.grblcontroller.util.PuzzleView;

public class PuzzleActivity extends Activity {
    private static int screenWidth;
    private static int screenHeight;
    private static final int REQUEST_CODE_SELECT_IMAGES = 101;
    private static final int REQUEST_CODE_PERMISSION = 102;
    private FrameLayout container;
    private PuzzleView puzzleView;
    private Button startBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        setContentView(R.layout.activity_puzzle);

        container = findViewById(R.id.puzzle_container);
        startBtn = findViewById(R.id.btn_start);

        startBtn.setOnClickListener(v -> checkPermissionsAndSelectImages());
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }

    private void checkPermissionsAndSelectImages() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            selectImages();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_PERMISSION);
            } else {
                selectImages();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            selectImages();
        } else {
            Toast.makeText(this, "请授权读取存储权限", Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImages() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  // 这里保证多选
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGES && resultCode == RESULT_OK && data != null) {
            List<Bitmap> bitmapList = new ArrayList<>();
            try {
                if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        Uri uri = clipData.getItemAt(i).getUri();
                        Bitmap bitmap = getBitmapFromUri(uri);
                        if (bitmap != null) bitmapList.add(bitmap);
                    }
                } else if (data.getData() != null) {
                    Uri uri = data.getData();
                    Bitmap bitmap = getBitmapFromUri(uri);
                    if (bitmap != null) bitmapList.add(bitmap);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!bitmapList.isEmpty()) {
                // 图片选完，隐藏按钮，开始游戏
                if (startBtn != null) {
                    container.removeView(startBtn);
                    startBtn = null;
                }
                startPuzzle(bitmapList);
            } else {
                Toast.makeText(this, "请选择至少一张图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try (InputStream stream = getContentResolver().openInputStream(uri)) {
            return BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            return null;
        }
    }

    private void startPuzzle(List<Bitmap> bitmaps) {
        container.removeAllViews();
        puzzleView = new PuzzleView(this);
        puzzleView.setImageList(bitmaps);
        container.addView(puzzleView);
        puzzleView.startGame();
    }
}

