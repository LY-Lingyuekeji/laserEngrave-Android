package in.co.gorest.grblcontroller.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import in.co.gorest.grblcontroller.GrblController;

/**
 * 图片工具类
 * 打开相机、打开相册、剪切图片
 */
public class ImgUtil {
    public static final int TAKE_PHOTO = 1;//拍照
    public static final int CHOOSE_PHOTO = 2;//选择相册
    public static final int REQUEST_CODE_CAMERA = 3;//相机权限请求
    public static final int REQUEST_CODE_ALBUM = 4;//相册权限请求
    public static Uri imageUri;//相机拍照图片保存地址

    private static String TAG = "ImgUtil";

    /**
     * 选择图片，从图库、相机
     *
     * @param activity 上下文
     */
    public static void choicePhoto(final Activity activity) {
        //采用的是系统Dialog作为选择弹框
        new AlertDialog.Builder(activity).setTitle("上传头像")//设置对话框标题
                .setPositiveButton("拍照", new DialogInterface.OnClickListener() {//添加确定按钮
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 23) {//检查相机权限
                            ArrayList<String> permissions = new ArrayList<>();
                            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                permissions.add(Manifest.permission.CAMERA);
                            }

                            if (permissions.size() == 0) {//有权限,跳转
                                //打开相机-兼容7.0
                                openCamera(activity);
                            } else {
                                activity.requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_CAMERA);
                            }
                        } else {
                            //打开相机-兼容7.0
                            openCamera(activity);
                        }
                    }
                }).setNegativeButton("系统相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //如果有权限申请，请在Activity中onRequestPermissionsResult权限返回里面重新调用openAlbum()
                if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ALBUM);
                } else {
                    openAlbum(activity);
                }
            }
        }).show();//在按键响应事件中显示此对话框
    }

    /**
     * 打开相机
     * 兼容7.0
     *
     * @param activity
     */
    public static void openCamera(Activity activity) {
        // 创建File对象，用于存储拍照后的图片
        File outputImage = new File(activity.getExternalCacheDir(), System.currentTimeMillis() +".png");
        try {
            if (outputImage.exists()) {
                outputImage.delete();
            }
            outputImage.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Build.VERSION.SDK_INT < 24) {
            imageUri = Uri.fromFile(outputImage);
        } else {
            //Android 7.0系统开始 使用本地真实的Uri路径不安全,使用FileProvider封装共享Uri
            //参数二:fileprovider绝对路径 com.dyb.testcamerademo：项目包名
            imageUri = FileProvider.getUriForFile(activity, "com.lingyue.laserengraving.fileprovider", outputImage);
        }
        // 启动相机程序
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        activity.startActivityForResult(intent, TAKE_PHOTO);
    }

    public static void openAlbum(Activity activity) {
        //调用系统图库的意图
        Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
        choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        activity.startActivityForResult(choosePicIntent, CHOOSE_PHOTO);
    }

    /**
     * 启动裁剪
     *
     * @param activity     上下文
     * @param sourceUir    需要裁剪图片的Uri
     * @param aspectRatioX 裁剪图片宽高比
     * @param aspectRatioY 裁剪图片宽高比
     * @return
     */
//    public static void startUCrop(Activity activity, Uri sourceUir, float aspectRatioX, float aspectRatioY) {
//        //裁剪后保存到文件中
//        Uri destinationUri = Uri.fromFile(new File(activity.getCacheDir(), System.currentTimeMillis() + ".png"));
//        //初始化，第一个参数：需要裁剪的图片；第二个参数：裁剪后图片
//        UCrop uCrop = UCrop.of(sourceUir, destinationUri);
//        //初始化UCrop配置
//        UCrop.Options options = new UCrop.Options();
//        //设置裁剪图片可操作的手势
//        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
//        //设置toolbar颜色
//        options.setToolbarColor(ActivityCompat.getColor(activity, R.color.colorBlue));
//        //设置状态栏颜色
//        options.setStatusBarColor(ActivityCompat.getColor(activity, R.color.colorBlue));
//        //是否隐藏底部容器，默认显示
//        options.setHideBottomControls(true);
//        //是否能调整裁剪框
//        options.setFreeStyleCropEnabled(true);
////        options.set
//        uCrop.withOptions(options);
//        //设置裁剪图片的宽高比，比如16：9（设置后就不能选择其他比例了、选择面板就不会出现了）
////        uCrop.withAspectRatio(aspectRatioX, aspectRatioY);
//        uCrop.start(activity);
//    }

    /**
     * 得到byte[]
     * 这里对传入的图片Uri压缩到1M以内，并转换为byte[]后返回
     *
     * @param activity 上下文
     * @param uri      传入图片的Uri
     * @return byte[]
     */
    public static byte[] getImgByteFromUri(Activity activity, Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);//100表示不压缩，直接放到out里面
        int options = 90;//压缩比例
        while (out.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            out.reset(); // 重置baos即清空baos
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, out);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        byte[] bs = out.toByteArray();

        return bs;
    }

    /**
     * 通过uri获取图片并进行压缩
     *
     * @param uri
     */
    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = false;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0)
            be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = false;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return bitmap;//再进行质量压缩
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.PNG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    //把白色转换成透明
    public static Bitmap getImageToChange(Bitmap mBitmap) {
        Bitmap createBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        if (mBitmap != null) {
            int mWidth = mBitmap.getWidth();
            int mHeight = mBitmap.getHeight();
            for (int i = 0; i < mHeight; i++) {
                for (int j = 0; j < mWidth; j++) {
                    int color = mBitmap.getPixel(j, i);
                    int g = Color.green(color);
                    int r = Color.red(color);
                    int b = Color.blue(color);
                    int a = Color.alpha(color);
                    if (g >= 250 && r >= 250 && b >= 250) {
                        a = 0;
                    }
                    color = Color.argb(a, r, g, b);
                    createBitmap.setPixel(j, i, color);
                }
            }
        }
        return createBitmap;
    }

    public static File saveBitmap(String name, Bitmap bitmap) {
        return saveBitmap(name,bitmap,100);
    }

    public static File saveBitmap(String name, Bitmap bitmap, int quality) {
        Log.e("spm","saveBitmap:"+name);
        String TargetPath = GrblController.getInstance().getExternalFilesDir(null) + "/laser/";
        File saveFile = null;
        if (fileIsExist(TargetPath)) {
            saveFile = new File(TargetPath, name);
            try {
                FileOutputStream saveImgout = new FileOutputStream(saveFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, saveImgout);
                saveImgout.flush();
                saveImgout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return saveFile;
    }

    public static boolean fileIsExist(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdir();
        }
    }

    /**
     * 图片压缩-质量压缩
     *
     * @param filePath 源图片路径
     * @return 压缩后的路径
     */

    public static String compressImage(String filePath) {

        //原文件
        File oldFile = new File(filePath);


        //压缩文件路径 照片路径/
        String targetPath = oldFile.getPath();
        int quality = 50;//压缩比例0-100
        Bitmap bm = getSmallBitmap(filePath);//获取一定尺寸的图片
        int degree = getRotateAngle(filePath);//获取相片拍摄角度

        if (degree != 0) {//旋转照片角度，防止头像横着显示
            bm = setRotateAngle(degree,bm);
        }
        File outputFile = new File(targetPath);
        try {
            if (!outputFile.exists()) {
                outputFile.getParentFile().mkdirs();
                //outputFile.createNewFile();
            }
//            else {
//                outputFile.delete();
//            }
            FileOutputStream out = new FileOutputStream(outputFile);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return filePath;
        }
        return outputFile.getPath();
    }

    /**
     * 根据路径获得图片信息并按比例压缩，返回bitmap
     */
    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//只解析图片边沿，获取宽高
        BitmapFactory.decodeFile(filePath, options);
        // 计算缩放比
        options.inSampleSize = calculateInSampleSize(options, 480, 800);
        // 完整解析图片返回bitmap
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }


    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
    /**
     * 获取图片的旋转角度
     *
     * @param filePath
     * @return
     */
    public static int getRotateAngle(String filePath) {
        int rotate_angle = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate_angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate_angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate_angle = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotate_angle;
    }

    /**
     * 旋转图片角度
     *
     * @param angle
     * @param bitmap
     * @return
     */
    public static Bitmap setRotateAngle(int angle, Bitmap bitmap) {

        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(angle);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;

    }


    public static  Bitmap comp(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        if( baos.toByteArray().length / 1024>1024) {//判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.PNG, 50, baos);//这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 400f;//这里设置高度为800f
        float ww = 240f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if ((w > h && w > ww)||(w == h && w > ww)) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if ((w < h && h > hh)||(w < h && h == hh)) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
//        if (be <= 0)
//            be = 1;
        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressImages(bitmap);//压缩好比例大小后再进行质量压缩
    }
    public static Bitmap compressImages(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        int options = 90;
        int length = baos.toByteArray().length / 1024;

        if (length>5000){
            //重置baos即清空baos
            baos.reset();
            //质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.PNG, 10, baos);
        }else if (length>4000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.PNG, 20, baos);
        }else if (length>3000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.PNG, 50, baos);
        }else if (length>2000){
            baos.reset();
            image.compress(Bitmap.CompressFormat.PNG, 70, baos);
        }
        //循环判断如果压缩后图片是否大于1M,大于继续压缩
        while (baos.toByteArray().length / 1024>1024) {
            //重置baos即清空baos
            baos.reset();
            //这里压缩options%，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.PNG, options, baos);
            //每次都减少10
            options -= 10;
        }
        //把压缩后的数据baos存放到ByteArrayInputStream中
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        //把ByteArrayInputStream数据生成图片
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }
    /**
     * 获取正确缩放裁剪适应IamgeView的Bitmap
     * @param imageView
     * @param bitmap
     * @return
     */
    public static Bitmap createFitBitmap(ImageView imageView, Bitmap bitmap) {
        Log.e(TAG, "createFitBitmap<---------------------");
        int widthTarget = imageView.getWidth();
        int heightTarget = imageView.getHeight();
        int widthBitmap = bitmap.getWidth();
        int heightBitmap = bitmap.getHeight();
        Log.e(TAG,"widthTarget = " + widthTarget );
        Log.e(TAG, "heightTarget = " + heightTarget );
        Log.e(TAG, "widthBitmap = " + widthBitmap );
        Log.e(TAG, "heightBitmap = " + heightBitmap );
        Bitmap result = null;
        if( widthBitmap >= widthTarget && heightBitmap >= heightTarget ){
            result = createLargeToSmallBitmap(widthBitmap, heightBitmap, widthTarget, heightTarget, bitmap);
        }
        else if( widthBitmap >= widthTarget && heightBitmap < heightTarget ){
            Bitmap temp = createLargeWidthToEqualHeightBitmap(widthBitmap, heightBitmap, widthTarget, heightTarget, bitmap);
            result = createLargeToSmallBitmap(temp.getWidth(), temp.getHeight(), widthTarget, heightTarget, temp);
        }
        else if( widthBitmap < widthTarget && heightBitmap >= heightTarget ){
            Bitmap temp = createLargeHeightToEqualWidthBitmap(widthBitmap, heightBitmap, widthTarget, heightTarget, bitmap);
            result = createLargeToSmallBitmap(temp.getWidth(), temp.getHeight(), widthTarget, heightTarget, temp);
        }
        else{
            Bitmap temp = createSmallToEqualBitmap(widthBitmap, heightBitmap, widthTarget, heightTarget, bitmap);
            result = createFitBitmap(imageView, temp);
        }
        Log.e(TAG, "createFitBitmap--------------------->");
        return result;
    }



    private static Bitmap createLargeToSmallBitmap( int widthBitmap, int heightBitmap, int widthTarget, int heightTarget, Bitmap bitmap){
        Log.e(TAG, "createLargeToSmallBitmap<---------------------");
        Log.e(TAG, "widthTarget = " + widthTarget );
        Log.e(TAG, "heightTarget = " + heightTarget );
        Log.e(TAG, "widthBitmap = " + widthBitmap );
        Log.e(TAG,"heightBitmap = " + heightBitmap );
        int x = (widthBitmap - widthTarget) / 2;
        int y = (heightBitmap - heightTarget) / 2;
        Log.e(TAG, "createLargeToSmallBitmap--------------------->");
        return Bitmap.createBitmap(bitmap, x, y, widthTarget, heightTarget);
    }

    private static Bitmap createLargeWidthToEqualHeightBitmap(int widthBitmap, int heightBitmap, int widthTarget, int heightTarget, Bitmap bitmap){

        Log.e(TAG, "createLargeWidthToEqualHeightBitmap<---------------------");
        Log.e(TAG, "widthTarget = " + widthTarget );
        Log.e(TAG, "heightTarget = " + heightTarget );
        Log.e(TAG, "widthBitmap = " + widthBitmap );
        Log.e(TAG, "heightBitmap = " + heightBitmap );
        double scale = ( heightTarget * 1.0 ) / heightBitmap;
        Log.e(TAG, "createLargeWidthToEqualHeightBitmap--------------------->");
        return Bitmap.createScaledBitmap(bitmap, (int)(widthBitmap * scale) , heightTarget, false);
    }

    private static Bitmap createLargeHeightToEqualWidthBitmap(int widthBitmap, int heightBitmap, int widthTarget, int heightTarget, Bitmap bitmap){

        Log.e(TAG, "createLargeHeightToEqualWidthBitmap<---------------------");
        Log.e(TAG, "widthTarget = " + widthTarget );
        Log.e(TAG, "heightTarget = " + heightTarget );
        Log.e(TAG, "widthBitmap = " + widthBitmap );
        Log.e(TAG, "heightBitmap = " + heightBitmap );
        double scale = ( widthTarget * 1.0 ) / widthBitmap;
        Log.e(TAG, "createLargeHeightToEqualWidthBitmap--------------------->");
        return Bitmap.createScaledBitmap(bitmap, widthTarget , (int)(heightTarget * scale), false);
    }

    private static Bitmap createSmallToEqualBitmap(int widthBitmap, int heightBitmap, int widthTarget, int heightTarget, Bitmap bitmap){

        Log.e(TAG, "createSmallToEqualBitmap<---------------------");
        Log.e(TAG, "widthTarget = " + widthTarget );
        Log.e(TAG, "heightTarget = " + heightTarget );
        Log.e(TAG, "widthBitmap = " + widthBitmap );
        Log.e(TAG, "heightBitmap = " + heightBitmap );
        double scaleWidth = ( widthTarget * 1.0 ) / widthBitmap;
        double scaleHeight = ( heightTarget * 1.0 ) / heightBitmap;
        double scale = Math.min(scaleWidth, scaleHeight);
        Log.e(TAG, "createSmallToEqualBitmap--------------------->");
        return Bitmap.createScaledBitmap(bitmap, (int)(widthBitmap * scale), (int)(heightBitmap * scale), false);
    }

}
