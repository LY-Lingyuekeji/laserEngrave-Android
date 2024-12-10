package in.co.gorest.grblcontroller.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PictureUtil {

    /**
     * 计算图片的缩放
     *
     * @param options
     * @param reqWidth
     *            请求宽度
     * @param reqHeight
     *            请求高度
     * @return 返回缩放比例
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return
     */
    public static Bitmap getSmallBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 把bitmap转换成String
     *
     * @param filePath
     *            地址
     * @return bitmap转换后的字符串
     */
    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return Bitmap
     */
    public static Bitmap getthumbBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = 4;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取bitmap
     *
     * @param filePath
     *            图片路径
     * @param inSampleSize
     *            压缩比例，图片不缩小
     * @return
     */
    public static Bitmap getBitmap(String filePath, int inSampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 获取指定尺寸的bitmap
     *
     * @param filePath
     *            路径
     * @param width
     *            宽度
     * @param height
     *            高度
     * @return
     */
    public static Bitmap getBitmap(String filePath, int width, int height) {
        Bitmap bitmap = getBitmap(filePath, 2);
        // int bitmapWidth = bitmap.getWidth();
        // int bitmapHeight = bitmap.getHeight();
        // float scaleWidth = width / bitmapWidth;
        // float scaleHeight = height / bitmapHeight;
        // Matrix matrix = new Matrix();
        // matrix.postScale(scaleWidth, scaleHeight);
        // // 产生缩放后的Bitmap对象
        // System.out.println("bitmapWidth="+bitmapWidth);
        // System.out.println("bitmapHeight="+bitmapHeight);
        // Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
        // bitmapHeight, matrix, false);
        Bitmap resizeBitmap = ThumbnailUtils.extractThumbnail(bitmap, width,
                height);
        return resizeBitmap;
    }

    /**
     * 根据路径获得突破并压缩返回bitmap用于显示
     *
     * @param filePath
     * @return Bitmap
     */
    public static Bitmap getsmallthumbBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = 8;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 根据路径删除图片
     *
     * @param path
     */
    public static void deleteTempFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 添加到图库
     */
    public static void galleryAddPic(Context context, String path) {
        Intent mediaScanIntent = new Intent(
                Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(path);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * 获取保存图片的文件夹
     *
     * @return
     */
    public static File getAlbumDir() {
        File dir = new File(getAlbumName());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 获取保存图片的文件夹的名称
     *
     * @return
     */
    public static String getAlbumName() {
        return Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/es/photo/";
    }

    /**
     * 通过uri获取图片的真是路径
     *
     * @param photoUri
     * @return
     */
    @SuppressLint("NewApi")
    public static String getPhotoInPhonePath(Context context, Uri photoUri) {
        String photoPath = "";
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        if (isKitKat && DocumentsContract.isDocumentUri(context, photoUri)) {
            String wholeID = DocumentsContract.getDocumentId(photoUri);
            String id = wholeID.split(":")[1];
            String[] column = { MediaStore.Images.Media.DATA };
            String sel = MediaStore.Images.Media._ID + "=?";
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                    new String[] { id }, null);
            int columnIndex = cursor.getColumnIndex(column[0]);
            if (cursor.moveToFirst()) {
                photoPath = cursor.getString(columnIndex);
            }
            cursor.close();
        } else {
            String[] pojo = { MediaStore.Images.Media.DATA };
            Cursor cursor = context.getContentResolver().query(photoUri, pojo,
                    null, null, null);
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
                cursor.moveToFirst();
                photoPath = cursor.getString(columnIndex);
                if (Integer.parseInt(Build.VERSION.SDK) < 14) {
                    cursor.close();
                }
            }
        }
        return photoPath;
    }

    /**
     * 旋转图片
     *
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotaingImageView(int angle, Bitmap bitmap) {
        System.gc();
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        if (bitmap != null) {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, true);
            System.gc();
            return bitmap;
        } else {
            return bitmap;
        }
    }

    /**
     * 读取图片旋转的角度
     *
     * @param path
     *            图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 图片压缩，并保存到新的位置
     *
     * @param context
     * @param maxSize
     *            图片最大大小
     * @param path
     *            图片地址
     * @return
     * @throws Exception
     */
    public static String decodeBitmap(Context context, long maxSize, String path)
            throws Exception {

        // int bitmapWidth = bitmap.getWidth();
        // int bitmapHeight = bitmap.getHeight();
        // float scaleWidth = 1;
        // float scaleHeight = 1;
        // Matrix matrix = new Matrix();
        // matrix.postScale(scaleWidth, scaleHeight);
        // // 产生缩放后的Bitmap对象
        // Bitmap resizeBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth,
        // bitmapHeight, matrix, false);
        File firstDir = new File(FileUtils.sdDirS + "/Photo_LJ");
        if (!firstDir.exists()) {
            firstDir.mkdirs();
        }
        String newPath = FileUtils.SDPATH + System.currentTimeMillis() + ".jpg";
        Bitmap bitmap = getSmallBitmap(path);
        File file = new File(path);
        File saveFile = new File(newPath);

        long length = file.length();
        if (length > maxSize) {
            saveFile.delete();
            FileOutputStream out = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
            out.flush();
            out.close();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();// 记得释放资源，否则会内存溢出
                bitmap = null;
            }
        } else {
            FileUtils.copyFile1(path, newPath);
        }
        System.gc();
        File newFile = new File(newPath);
        if (newFile.length() > maxSize) {
            decodeBitmap(context, maxSize, newPath);
        }
        return newPath;
    }

    /**
     * 保存图片
     *
     * @param context
     * @param inSampleSize
     *            压缩比
     * @param path
     *            图片路径
     * @param quality
     *            压缩质量
     * @param maxSize
     *            压缩最大值
     */
    public static void savePicture(Context context, int inSampleSize,
                                   String path, int quality, long maxSize) {
        Bitmap bitmap = PictureUtil.getBitmap(path, inSampleSize);
        File file = new File(path);
        long size = file.length();
        if (maxSize < size) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                out.flush();
                out.close();
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();// 记得释放资源，否则会内存溢出
                    bitmap = null;
                }
            } catch (Exception e) {
                UsualTools.showShortToast(context, "图像获取失败");
                e.printStackTrace();
            }
            savePicture(context, inSampleSize, path, quality, maxSize);
        } else {
            return;
        }
    }

    /**
     *
     * @param context
     * @param bitmap
     *            图像
     * @param path
     *            地址
     * @param quality
     *            压缩质量
     * @param maxSize
     *            压缩最大值
     */
    public static void savePicture(Context context, Bitmap bitmap, String path,
                                   int quality, long maxSize) {
        File file = new File(path);
        long size = file.length();
        if (maxSize < size) {
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
                out.flush();
                out.close();
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();// 记得释放资源，否则会内存溢出
                    bitmap = null;
                }
            } catch (Exception e) {
                UsualTools.showShortToast(context, "图像获取失败");
                e.printStackTrace();
            }
            savePicture(context, bitmap, path, quality, maxSize);
        } else {
            return;
        }
    }

    /**
     * 以最省内存的方式读取本地资源的图片
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap readBitMap(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // 获取资源图片
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /**
     * 从资源文件夹中获取图片并转换为byte
     *
     * @param context
     * @param resId
     * @return
     */
    public static byte[] getByteFromResource(Context context, int resId) {
        InputStream inStream = context.getResources().openRawResource(resId);
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100]; // buff用于存放循环读取的临时数据
        int rc = 0;
        try {
            while ((rc = inStream.read(buff, 0, 100)) > 0) {
                swapStream.write(buff, 0, rc);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return swapStream.toByteArray();
    }
}
