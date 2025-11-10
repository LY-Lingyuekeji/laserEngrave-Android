package in.co.gorest.grblcontroller.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import in.co.gorest.grblcontroller.R;

public class TestActivity extends AppCompatActivity {
    // 用于日志记录的标签
    private final static String TAG = TestActivity.class.getSimpleName();
    // 返回
    private ImageView ivBack;

    private ImageView originalImageView;
    private ImageView detectedImageView;
    private ImageView correctedImageView;
    private Uri photoUri;
    private static final int REQUEST_CODE_CAMERA = 1001;


    // 启用矢量图支持，确保在应用中可以正确显示矢量图形
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV not loaded");
        } else {
            Log.d("OpenCV", "OpenCV loaded");
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // 绑定视图
        DataBindingUtil.setContentView(this, R.layout.activity_test);

        // 修改状态栏的文字和图标变成黑色，以适应浅色背景
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            this.getWindow().getInsetsController().setSystemBarsAppearance(
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }


        // 初始化界面
        initView();
        // 初始化数据
        initData();
        // 初始化监听事件
        initListeners();
    }

    /**
     * 初始化界面
     */
    private void initView() {
        // 返回
        ivBack = findViewById(R.id.iv_back);

        originalImageView = findViewById(R.id.imageOriginal);
        detectedImageView = findViewById(R.id.imageDetected);
        correctedImageView = findViewById(R.id.imageProcessed);

    }

    /**
     * 初始化数据
     */
    private void initData() {

    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        // 返回
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnTakePhoto.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
            } else {
                takePhoto();
            }
        });

    }
    private File photoFile;

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
                Uri photoURI = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, REQUEST_CODE_CAMERA);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = getExternalFilesDir(null);
        return File.createTempFile("JPEG_" + timeStamp, ".jpg", storageDir);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA && resultCode == RESULT_OK) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            originalImageView.setImageBitmap(bitmap);
            processBitmap(bitmap);
        }
    }

    private void processBitmap(Bitmap bitmap) {

        InputImage inputImage;
        try {
            inputImage = InputImage.fromBitmap(bitmap, 0);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        // 使用新的 TextRecognizerOptions 来初始化 TextRecognizer
        TextRecognizerOptions textRecognizerOptions = new TextRecognizerOptions.Builder().build();
        TextRecognizer recognizer = TextRecognition.getClient(textRecognizerOptions);


        // 使用 ML Kit 进行文字识别
        recognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    List<Text.TextBlock> blocks = visionText.getTextBlocks();
                    Map<String, Point> letterMap = new HashMap<>();

                    for (Text.TextBlock block : blocks) {
                        for (Text.Line line : block.getLines()) {
                            for (Text.Element element : line.getElements()) {
                                String text = element.getText().toUpperCase();
                                if (text.length() == 1 && "ABCD".contains(text)) {
                                    Rect box = element.getBoundingBox();
                                    if (box != null) {
                                        Point center = new Point(box.centerX(), box.centerY());
                                        letterMap.put(text, center);
                                    }
                                }
                            }
                        }
                    }

                    if (letterMap.size() < 4) {
                        Toast.makeText(this, "未识别到 A、B、C、D 四个角", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    // 获取 A/B/C/D 四个点
                    Point pointA = letterMap.get("A");
                    Point pointB = letterMap.get("B");
                    Point pointC = letterMap.get("C");
                    Point pointD = letterMap.get("D");

                    // 复制原图，用于绘制标记
                    Bitmap markedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas canvas = new Canvas(markedBitmap);
                    Paint pointPaint = new Paint();
                    pointPaint.setColor(Color.GREEN);
                    pointPaint.setStyle(Paint.Style.FILL);
                    pointPaint.setAntiAlias(true);
                    pointPaint.setStrokeWidth(20); // 可选：如果你是用 drawPoint 而非 drawCircle

                    // 初始化红色线画笔（连接线）
                    Paint linePaint = new Paint();
                    linePaint.setColor(Color.RED);
                    linePaint.setStyle(Paint.Style.STROKE);
                    linePaint.setAntiAlias(true);
                    linePaint.setStrokeWidth(24); // 加粗线宽，更明显

                    // 标记四个点
                    canvas.drawCircle((float) pointA.x, (float) pointA.y, 15, pointPaint);
                    canvas.drawCircle((float) pointB.x, (float) pointB.y, 15, pointPaint);
                    canvas.drawCircle((float) pointC.x, (float) pointC.y, 15, pointPaint);
                    canvas.drawCircle((float) pointD.x, (float) pointD.y, 15, pointPaint);

                    // 连接四个点形成四边形
                    canvas.drawLine((float) pointA.x, (float) pointA.y, (float) pointC.x, (float) pointC.y, linePaint); // A -> C
                    canvas.drawLine((float) pointC.x, (float) pointC.y, (float) pointD.x, (float) pointD.y, linePaint); // C -> D
                    canvas.drawLine((float) pointD.x, (float) pointD.y, (float) pointB.x, (float) pointB.y, linePaint); // D -> B
                    canvas.drawLine((float) pointB.x, (float) pointB.y, (float) pointA.x, (float) pointA.y, linePaint); // B -> A

                    // 显示标记后的图像
                    detectedImageView.setImageBitmap(markedBitmap);

                    // 开始透视变换
                    applyPerspectiveTransformation(bitmap, new Point[]{pointA, pointB, pointC, pointD});


                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "文字识别失败", Toast.LENGTH_SHORT).show();
                });


//        Mat srcMat = new Mat();
//        Utils.bitmapToMat(bitmap, srcMat);
//        Imgproc.cvtColor(srcMat, srcMat, Imgproc.COLOR_RGBA2RGB);
//
//        Mat resized = new Mat();
//        Imgproc.resize(srcMat, resized, new Size(800, 800.0 * srcMat.rows() / srcMat.cols()));
//
//        Mat gray = new Mat();
//        Imgproc.cvtColor(resized, gray, Imgproc.COLOR_RGB2GRAY);
//        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);
//        Imgproc.Canny(gray, gray, 100, 200);
//
//        // 查找轮廓
//        List<MatOfPoint> contours = new ArrayList<>();
//        Imgproc.findContours(gray, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//
//        // 找出ABCD字母区域
//        List<Point> letterCenters = new ArrayList<>();
//        for (MatOfPoint contour : contours) {
//            Rect rect = Imgproc.boundingRect(contour);
//            if (rect.height > 30 && rect.height < 200 && rect.width > 20 && rect.width < 200) {
//                Point center = new Point(rect.x + rect.width / 2.0, rect.y + rect.height / 2.0);
//                letterCenters.add(center);
//            }
//        }
//
//        if (letterCenters.size() < 4) {
//            Toast.makeText(this, "未识别到四个字母", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 按左上、右上、右下、左下排序
//        Point[] corners = sortCorners(letterCenters);
//
//        // 绘制标记图像
//        Mat marked = resized.clone();
//        for (int i = 0; i < 4; i++) {
//            Imgproc.circle(marked, corners[i], 10, new Scalar(0, 0, 255), -1);
//        }
//
//        // 定义变换目标矩形
//        double width = 800, height = 600;
//        MatOfPoint2f dst = new MatOfPoint2f(
//                new Point(0, 0), new Point(width - 1, 0),
//                new Point(width - 1, height - 1), new Point(0, height - 1)
//        );
//
//        MatOfPoint2f src = new MatOfPoint2f(corners);
//        Mat transform = Imgproc.getPerspectiveTransform(src, dst);
//        Mat warped = new Mat();
//        Imgproc.warpPerspective(resized, warped, transform, new Size(width, height));
//
//        // 输出两个 Bitmap
//        markBitmap = Bitmap.createBitmap(marked.cols(), marked.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(marked, markBitmap);
//
//        correctedBitmap = Bitmap.createBitmap(warped.cols(), warped.rows(), Bitmap.Config.ARGB_8888);
//        Utils.matToBitmap(warped, correctedBitmap);
//
//        detectedImageView.setImageBitmap(markBitmap);
//        correctedImageView.setImageBitmap(correctedBitmap);
    }

//    private Point[] sortCorners(List<Point> pts) {
//        // 找到 A(左上), B(左下), C(右上), D(右下)
//        Point A = null, B = null, C = null, D = null;
//        for (Point p : pts) {
//            if (A == null || (p.x + p.y) < (A.x + A.y)) A = p;
//            if (C == null || (p.x - p.y) > (C.x - C.y)) C = p;
//            if (B == null || (p.x - p.y) < (B.x - B.y)) B = p;
//            if (D == null || (p.x + p.y) > (D.x + D.y)) D = p;
//        }
//        return new Point[]{A, C, D, B}; // 顺序：左上、右上、右下、左下
//    }


    private void applyPerspectiveTransformation(Bitmap bitmap, Point[] corners) {
        Mat srcMat = new Mat();
        Utils.bitmapToMat(bitmap, srcMat);

        Mat dstMat = new Mat();

        // 原图四个角点
        MatOfPoint2f srcPoints = new MatOfPoint2f(
                new Point(corners[0].x, corners[0].y), // A
                new Point(corners[2].x, corners[2].y), // C
                new Point(corners[3].x, corners[3].y), // D
                new Point(corners[1].x, corners[1].y)  // B
        );

        int width = 600;
        int height = 800;

        MatOfPoint2f dstPoints = new MatOfPoint2f(
                new Point(0, 0),
                new Point(width, 0),
                new Point(width, height),
                new Point(0, height)
        );

        Mat transform = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(srcMat, dstMat, transform, new Size(width, height));

        Bitmap correctedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstMat, correctedBitmap);

        correctedImageView.setImageBitmap(correctedBitmap);
    }

//    private Point[] sortCorners(List<Point> pts) {
//        // 找到 A(左上), B(左下), C(右上), D(右下)
//        Point A = null, B = null, C = null, D = null;
//        for (Point p : pts) {
//            if (A == null || (p.x + p.y) < (A.x + A.y)) A = p;
//            if (C == null || (p.x - p.y) > (C.x - C.y)) C = p;
//            if (B == null || (p.x - p.y) < (B.x - B.y)) B = p;
//            if (D == null || (p.x + p.y) > (D.x + D.y)) D = p;
//        }
//        return new Point[]{A, C, D, B}; // 顺序：左上、右上、右下、左下
//    }

}
