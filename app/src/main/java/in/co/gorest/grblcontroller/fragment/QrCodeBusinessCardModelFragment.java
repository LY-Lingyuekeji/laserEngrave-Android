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
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import in.co.gorest.grblcontroller.BuildConfig;
import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.activity.EditActivity;
import in.co.gorest.grblcontroller.util.ImgUtil;

public class QrCodeBusinessCardModelFragment extends Fragment {
    // 用于日志记录的标签
    private final static String TAG = QrCodeBusinessCardModelFragment.class.getSimpleName();
    // 二维码
    private ImageView ivQrCode;
    // 姓
    private EditText etFirstName;
    // 名
    private EditText etLastName;
    // 手机号
    private EditText etPhone;
    // 邮箱
    private EditText etEmail;
    // 公司
    private EditText etCompany;
    // 职位
    private EditText etJob;
    // 微信
    private EditText etWechat;
    // QQ
    private EditText etQq;
    // 下一步
    private TextView tvNext;

    public QrCodeBusinessCardModelFragment() {
    }

    public static QrCodeBusinessCardModelFragment newInstance() {
        return new QrCodeBusinessCardModelFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode_businesscard_model, container, false);
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
        // 姓
        etFirstName = view.findViewById(R.id.et_first_name);
        // 名
        etLastName = view.findViewById(R.id.et_last_name);
        // 手机号
        etPhone = view.findViewById(R.id.et_phone);
        // 邮箱
        etEmail = view.findViewById(R.id.et_email);
        // 公司
        etCompany = view.findViewById(R.id.et_company);
        // 职位
        etJob = view.findViewById(R.id.et_job);
        // 微信
        etWechat = view.findViewById(R.id.et_wechat);
        // QQ
        etQq = view.findViewById(R.id.et_qq);
        // 下一步
        tvNext = view.findViewById(R.id.tv_next);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        // 设置二维码
        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                + "\nFN:" + etLastName.getText().toString()
                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                + "\nEMAIL:" + etEmail.getText().toString()
                + "\nORG:" + etCompany.getText().toString()
                + "\nTITLE:" + etJob.getText().toString()
                + "\nX-WETCHAT:" + etWechat.getText().toString()
                + "\nX-QQ:" + etQq.getText().toString()
                + "\nEND:VCARD");
    }

    /**
     * 初始化事件监听
     */
    private void setupListeners() {
        // 姓
        etFirstName.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + s.toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 名
        etLastName.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + s.toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 手机号
        etPhone.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + s.toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 邮箱
        etEmail.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + s.toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 公司
        etCompany.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + s.toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 职位
        etJob.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + s.toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // 微信
        etWechat.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + s.toString()
                                + "\nX-QQ:" + etQq.getText().toString()
                                + "\nEND:VCARD");
                    }
                };
                handler.postDelayed(runnable, 300); // 300ms防抖时间
            }
        });

        // QQ
        etJob.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // 设置二维码
                        setBarcodeToImageView(ivQrCode, "BEGIN:VCARD\nVERSION:3.0\nN:" + etFirstName.getText().toString()
                                + "\nFN:" + etLastName.getText().toString()
                                + "\nTEL;CELL;VOICE:" + etPhone.getText().toString()
                                + "\nEMAIL:" + etEmail.getText().toString()
                                + "\nORG:" + etCompany.getText().toString()
                                + "\nTITLE:" + etJob.getText().toString()
                                + "\nX-WETCHAT:" + etWechat.getText().toString()
                                + "\nX-QQ:" + s.toString()
                                + "\nEND:VCARD");
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
}