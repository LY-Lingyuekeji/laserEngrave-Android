package in.co.gorest.grblcontroller.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.events.ModelChangeEvent;
import in.co.gorest.grblcontroller.events.ScanResultMessageEvent;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class QrCodeCopyModelFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = QrCodeCopyModelFragment.class.getSimpleName();
    // 二维码
    private ImageView ivQrCode;
    // 扫描二维码
    private TextView tvScanQrcode;
    // 二维码文字输入框
    private EditText etQrCodeText;
    // 字数限制
    private TextView tvQrCodeLimit;
    // 下一步
    private TextView tvNext;

    public QrCodeCopyModelFragment() {
    }

    public static QrCodeCopyModelFragment newInstance() {
        return new QrCodeCopyModelFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 注册EventBus
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //注销EventBus
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode_copy_model, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 初始化界面
        initView(view);
        // 初始化数据
        initData();
        // 初始化事件监听
        setupListeners();
    }

    /**
     * 初始化界面
     *
     * @param view view
     */
    private void initView(View view) {
        // 二维码
        ivQrCode = view.findViewById(R.id.iv_qr_code);
        // 扫描二维码
        tvScanQrcode = view.findViewById(R.id.tv_scan_qrcode);
        // 二维码文字输入框
        etQrCodeText = view.findViewById(R.id.et_qr_code_text);
        // 字数限制
        tvQrCodeLimit = view.findViewById(R.id.tv_qr_code_limit);
        // 下一步
        tvNext = view.findViewById(R.id.tv_next);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置二维码
        setBarcodeToImageView(ivQrCode, "QRCODE");
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 扫描二维码
        tvScanQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(requireActivity());
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                integrator.setPrompt("扫描二维码");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setOrientationLocked(true);
                integrator.initiateScan();
            }
        });

        // 二维码文字输入框
        etQrCodeText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvQrCodeLimit.setText(s.length() + "/1000");
                if (s.length() > 1000) {
                    Toast.makeText(getActivity(), "最多输入1000个字", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 更新UI的操作
                        if (!s.toString().equals("") || !s.toString().isEmpty()) {
                            setBarcodeToImageView(ivQrCode,s.toString());
                        } else {
                            setBarcodeToImageView(ivQrCode, "QRCODE");
                        }
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 下一步
        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 创建一个与LinearLayout大小相同的Bitmap对象
                Bitmap bitmap = Bitmap.createBitmap(ivQrCode.getWidth(), ivQrCode.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                ivQrCode.draw(canvas);
                File barcodeBitmap = ImgUtil.saveBitmap("qrcode" + System.currentTimeMillis() + ".png", bitmap);
                Uri imageUris = Uri.fromFile(barcodeBitmap);
                Intent intent = new Intent(getActivity(), EditActivity.class);
                intent.putExtra("type", "5");
                intent.putExtra(BuildConfig.APPLICATION_ID + ".InputUri", imageUris);
                intent.putExtra("businessType", 1);
                getActivity().startActivity(intent);
                getActivity().finish();
            }
        });
    }


    /**
     * 设置条形码到ImageView
     *
     * @param imageView 指定的ImageView
     * @param text      文字
     */
    public static void setBarcodeToImageView(ImageView imageView, String text) {
        int width = imageView.getWidth(); // 获取ImageView的宽度
        int height = imageView.getHeight(); // 获取ImageView的高度
        if (width <= 0 || height <= 0) {
            width = 200;
            height = 200;
        }

        try {
            Log.d(TAG, "w=" + width + "--h=" + height);
            generateBarcode(imageView, text, width, height);
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 转换为条形码
     *
     * @param imageView imageView
     * @param text      内容
     * @param width     宽度
     * @param height    高度
     */
    private static void generateBarcode(ImageView imageView, String text, int width, int height) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        Bitmap bitmap = Bitmap.createBitmap(bitMatrix.getWidth(), bitMatrix.getHeight(), Bitmap.Config.ARGB_8888);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        imageView.setImageBitmap(bitmap);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnScanResultMessageEvent(ScanResultMessageEvent event) {
        if (!event.getMessage().isEmpty()) {
            Log.d(TAG, "ScanResult=" + event.getMessage());
           etQrCodeText.setText(event.getMessage());
        }
    }
}