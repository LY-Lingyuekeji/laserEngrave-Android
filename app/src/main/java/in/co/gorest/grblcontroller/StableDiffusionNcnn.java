package in.co.gorest.grblcontroller;

public class StableDiffusionNcnn {
    static {
        System.loadLibrary("sdncnn"); // 你自己的 native 库名
    }

    // 初始化模型
    public native boolean init(String modelDir);

    // 生成图片，传入 prompt，返回图片字节流（PNG/JPEG）
    public native byte[] generateImage(String prompt);

    // 释放资源
    public native void release();
}
