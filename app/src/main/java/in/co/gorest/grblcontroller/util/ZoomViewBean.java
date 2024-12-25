package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import java.util.List;
import in.co.gorest.grblcontroller.model.EffectBean;

public class ZoomViewBean {
    private RelativeLayout.LayoutParams params;
    private float resols;
    private int depthProgress = 20,//雕刻深度
            speedProgress = 40;//雕刻速度
    private float scaleX = 1.0f, scaleY = 1.0f;
    private View view;
    private Uri uri;
    private Uri initBitmapUri;
    private List<EffectBean> effectBeans;

    public List<EffectBean> getEffectBeans() {
        return effectBeans;
    }

    public void setEffectBeans(List<EffectBean> effectBeans) {
        this.effectBeans = effectBeans;
    }

    public Uri getInitBitmapUri() {
        return initBitmapUri;
    }

    public void setInitBitmapUri(Uri initBitmapUri) {
        this.initBitmapUri = initBitmapUri;
    }

    public RelativeLayout.LayoutParams getParams() {
        return params;
    }

    public void setParams(RelativeLayout.LayoutParams params) {
        this.params = params;
    }


    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public int getDepthProgress() {
        return depthProgress;
    }

    public void setDepthProgress(int depthProgress) {
        this.depthProgress = depthProgress;
    }

    public int getSpeedProgress() {
        return speedProgress;
    }

    public void setSpeedProgress(int speedProgress) {
        this.speedProgress = speedProgress;
    }

    public boolean isAndReverse() {
        return andReverse;
    }

    public void setAndReverse(boolean andReverse) {
        this.andReverse = andReverse;
    }

    private boolean andReverse;


    public int getSharp() {
        return Sharp;
    }

    public void setSharp(int sharp) {
        Sharp = sharp;
    }

    private int Sharp;


    public float getResols() {
        return resols;
    }

    public void setResols(float resols) {
        this.resols = resols;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    private ImageView ivClose;

    public ImageView getIvIcon() {
        return ivIcon;
    }

    public void setIvIcon(ImageView ivIcon) {
        this.ivIcon = ivIcon;
    }

    private ImageView ivIcon;

    public ImageView getIvClose() {
        return ivClose;
    }

    public void setIvClose(ImageView ivClose) {
        this.ivClose = ivClose;
    }

    private ZoomView zoomView;

    public ZoomView getZoomView() {
        return zoomView;
    }

    public void setZoomView(ZoomView zoomView) {
        this.zoomView = zoomView;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    private Bitmap bitmap;
    private int Wide;
    private int height;
    private int editWideX;

    public int getEditScrollX() {
        return editScrollX;
    }

    public void setEditScrollX(int editScrollX) {
        this.editScrollX = editScrollX;
    }

    private int editScrollX = 0;

    public int getEditWideX() {
        return editWideX;
    }

    public void setEditWideX(int editWideX) {
        this.editWideX = editWideX;
    }

    public int getEditHighY() {
        return editHighY;
    }

    public void setEditHighY(int editHighY) {
        this.editHighY = editHighY;
    }

    private int editHighY;

    public int getWide() {
        return Wide;
    }

    public void setWide(int wide) {
        Wide = wide;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    private List<String> gcodes;

    public List<String> getGcodes() {
        return gcodes;
    }

    public void setGcodes(List<String> gcodes) {
        this.gcodes = gcodes;
    }

    private String Types;

    public String getTypes() {
        return Types;
    }

    public void setTypes(String types) {
        Types = types;
    }
}
