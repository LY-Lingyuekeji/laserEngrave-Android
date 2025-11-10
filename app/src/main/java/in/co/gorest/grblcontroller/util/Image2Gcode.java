package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import in.co.gorest.grblcontroller.model.ScanDirection;

public class Image2Gcode {
    float coordX;//X
    float coordY;//Y
    double sz;//S (or Z)
    float lastX; //Last x/y  coords for compare
    float lastY;
    double lastSz;//last 'S' value for compare
    char szChar;//Use 'S' or 'Z' for test laser power
    String coordXStr;//String formated X
    String coordYStr;////String formated Y
    String szStr;////String formated S
    boolean lastPointFlag = false;
    boolean g0Flag = false;

    //Interpolate a 8 bit grayscale value (0-255) between min,max
    private int interpolate(int grayValue, int min, int max) {
        int dif = max - min;
        return (min + ((grayValue * dif) / 255));
    }

    /**
     * 将灰度值映射为雕刻深度（支持 Gamma 调整，黑色最深）
     *
     * @param grayValue 灰度值（0-255）
     * @param minDepth  最浅（通常 0.0）
     * @param maxDepth  最深（例如 1.0）
     * @param gamma     对比度修正（1.0 = 线性，>1 强化暗部）
     * @return 对应的浮点雕刻深度（正值，表示下刀深度）
     */
    private double interpolateDepth(int grayValue, double minDepth, double maxDepth, double gamma) {
        if (grayValue < 0) grayValue = 0;
        if (grayValue > 255) grayValue = 255;

        // 归一化并反转（0=黑->1，255=白->0）
        double normalized = 1.0 - (grayValue / 255.0);

        // gamma 调整
        if (gamma != 1.0) {
            normalized = Math.pow(normalized, gamma);
        }

        double range = maxDepth - minDepth;
        double depth = minDepth + normalized * range;

        // 保证数值稳定，返回双精度（四位小数）
        return Math.round(depth * 10000.0) / 10000.0;
    }


    private String generateLine() {
        String line = "";

        // G0是快速移动，G1操作工件时的移动速度
        if ((sz != lastSz) && (lastSz != -1)) {
            if ((sz == 0) && (lastSz != 0))    // 直接把激光关了
            {
                // line = "S0\r";
            }

            if (lastSz == 0)    // 上一个点为0，就快速移动到上一个坐标
            {
                line += "G0 X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + "S0";
                g0Flag = true;
            } else {
                if (g0Flag) {
                    line += "G1 ";
                    g0Flag = false;
                }
                line += "X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + "S" + lastSz;
            }
        }
        return line;
    }

//    // CNC
//    private String generateLineForCNC(int zCutDepth) {
//        String line = "";
//
//        // G0是快速移动，G1操作工件时的移动速度
//        if ((sz != lastSz) && (lastSz != -1)) {
//            if ((sz == 0) && (lastSz != 0))    // 直接把激光关了
//            {
//                // line = "S0\r";
//            }
//
//            if (lastSz == 0)    // 上一个点为0，就快速移动到上一个坐标
//            {
//                line += "G0 Z" + zCutDepth + "\r\n";
//                line += "G0 X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " T0";
//                g0Flag = true;
//            } else {
//                if (g0Flag) {
//                    line += "G1 ";
//                    g0Flag = false;
//                }
//                line += "Z-" + zCutDepth + "\r\n";
//                line += "X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " T" + lastSz;
//            }
//        }
//        return line;
//    }


    /**
     * CNC —— 按灰度动态控制下刀深度（使用类字段，epsilon 比较）
     *
     * - 保持你原来的 G0/G1 切换逻辑
     * - 使用四位小数输出 Z 深度
     */
    private String generateLineForCNC(double sz, double lastSz, double lastX, double lastY,
                                      double zCutDepth, double safeZ, boolean g0FlagParam) {
        StringBuilder line = new StringBuilder();

        double eps = 1e-6;

        // 只有在深度有显著变化时才输出指令，避免浮点噪声导致大量无用指令
        if ((Math.abs(sz - lastSz) > eps) && (lastSz != -1.0)) {

            if ((Math.abs(sz - 0.0) < eps) && (Math.abs(lastSz - 0.0) > eps)) {
                // 当前为抬刀，上一点为雕刻：抬刀
                line.append("G0 Z").append(String.format("%.4f", safeZ)).append("\r\n");
            }

            if (Math.abs(lastSz - 0.0) < eps) {
                // 上一个点为空白：快速移动到位置，再下刀
                line.append("G0 Z").append(String.format("%.4f", safeZ)).append("\r\n");
                line.append("G0 X").append(String.format("%.2f", lastX))
                        .append(" Y").append(String.format("%.2f", lastY))
                        .append(" T0");
                this.g0Flag = true;
            } else {
                // 连续雕刻：尽量将 Z 与 XY 合并成连续 G1（减少点切换）
                if (this.g0Flag) {
                    line.append("G1 ");
                    this.g0Flag = false;
                }
                line.append("Z-").append(String.format("%.4f", sz)).append("\r\n");
                line.append("X").append(String.format("%.2f", lastX))
                        .append(" Y").append(String.format("%.2f", lastY))
                        .append(" T").append(String.format("%.4f", lastSz));
            }
        }

        return line.toString();
    }


    private String generateLineNew(float coordX, float coordY, int laserPower) {
        // 如果激光功率值为0，激光应该关闭，不生成雕刻G代码
        if (laserPower == 0) {
            return "M5"; // 关闭激光
        }

        // 生成激光雕刻的G代码
        String line = String.format(Locale.US, "G1 X%.2f Y%.2f S%d", coordX, coordY, laserPower);
        return line;
    }

    private String generateEndLine() {
        String line = "";
        if ((lastSz != 0) && (lastSz != -1)) {
            line += "G1 X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " S" + lastSz;
        }

        return line;
    }

//    // CNC
//    private String generateEndLineForCNC(int zCutDepth) {
//        String line = "";
//        if ((lastSz != 0) && (lastSz != -1)) {
//            line += "G1 Z-" + zCutDepth + "\r\n";
//            line += "G1 X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " T" + lastSz;
//        }
//
//        return line;
//    }

    /**
     * CNC —— 每行结束时收尾（保持原逻辑）
     */
    private String generateEndLineForCNC(double sz, double zCutDepth, double safeZ,
                                         double lastX, double lastY, boolean g0FlagParam) {
        StringBuilder line = new StringBuilder();

        if (!this.g0Flag) {
            line.append("G0 Z").append(String.format("%.4f", safeZ)).append("\r\n");
            this.g0Flag = true;
        }

        line.append("G0 X").append(String.format("%.2f", lastX))
                .append(" Y").append(String.format("%.2f", lastY))
                .append(" Z").append(String.format("%.4f", safeZ));

        return line.toString();
    }


    /**
     * 激光雕刻 Gcode 生成
     *
     * @param image          原始图片
     * @param targetWidth    雕刻目标宽度（单位：mm）
     * @param targetHeight   雕刻目标高度（单位：mm）
     * @param resol          分辨率（单位间隔，值越小越精细）
     * @param feedRate       速度
     * @param laserIntensity 激光功率
     * @param startX         起始X
     * @param startY         起始Y
     * @param direction      扫描方向
     * @param callback       进度回调
     * @return Gcode 列表
     */
    protected ArrayList<String> imageConvert2Gcode(Bitmap image, float targetWidth, float targetHeight, float resol, int feedRate, int laserIntensity, float startX, float startY, ScanDirection direction, GcodeProgressCallback callback) {
        if (image == null) {
            return null;
        }

        ArrayList<String> gcode = new ArrayList<>();
        gcode.add("G90\r\n"); // 绝对定位
        gcode.add("M5\r\n");
        gcode.add("M4 S0\r\n");
        gcode.add(String.format("F%d\r\n", feedRate));
        gcode.add("G21\r\n");
        gcode.add("G1\r\n");

        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();

        int cols = (int) (targetWidth / resol);
        int rows = (int) (targetHeight / resol);
        int totalPixels = cols * rows;
        int processedPixels = 0;

        lastX = -1;
        lastY = -1;
        lastSz = -1;
        lastPointFlag = false;

        String line;


        switch (direction) {
            case HORIZONTAL:
                for (int y = 0; y < rows; y++) {
                    coordY = resol * y;
                    for (int x = 0; x < cols; x++) {
                        coordX = resol * x;
                        int px = x * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                    y++;
                    if (y >= rows) break;

                    coordY = resol * y;
                    for (int x = cols - 1; x >= 0; x--) {
                        coordX = resol * x;
                        int px = x * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                }
                break;

            case VERTICAL:
                for (int x = 0; x < cols; x++) {
                    coordX = resol * x;
                    for (int y = 0; y < rows; y++) {
                        coordY = resol * y;
                        int px = x * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                    x++;
                    if (x >= cols) break;

                    coordX = resol * x;
                    for (int y = rows - 1; y >= 0; y--) {
                        coordY = resol * y;
                        int px = x * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                }
                break;

            case DIAGONAL_LD_RU: // ↘方向，从左上到右下
                for (int sum = 0; sum <= cols + rows - 2; sum++) {
                    for (int x = 0; x <= sum; x++) {
                        int y = sum - x;
                        if (x >= cols || y >= rows) continue;
                        coordX = resol * x;
                        coordY = resol * y;
                        int px = x * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                }
                break;

            case DIAGONAL_LU_RD: // ↙方向，从右上到左下
                for (int sum = 0; sum <= cols + rows - 2; sum++) {
                    for (int x = 0; x <= sum; x++) {
                        int y = sum - x;
                        int cx = cols - 1 - x;
                        if (cx < 0 || y >= rows) continue;
                        coordX = resol * cx;
                        coordY = resol * y;
                        int px = cx * imgWidth / cols;
                        int py = y * imgHeight / rows;
                        sz = 255 - (image.getPixel(px, imgHeight - 1 - py) & 0xFF);
                        sz = interpolate((int) sz, 0, laserIntensity);
                        line = generateLine();
                        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                        lastX = coordX + startX;
                        lastY = coordY + startY;
                        lastSz = sz;

                        if (callback != null && ++processedPixels % 1000 == 0)
                            callback.onProgress((int) ((processedPixels / (float) totalPixels) * 100));
                    }
                    line = generateEndLine();
                    if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");
                }
                break;
        }

        lastPointFlag = true;
        line = generateLine();
        if (line != null && !line.isEmpty()) gcode.add(line + "\r\n");

        gcode.add("M5\r\n");
        gcode.add(String.format("G0 X%.2f Y%.2f\r\n", startX, startY));
        if (callback != null) callback.onProgress(100);

        return gcode;
    }


    /**
     * CNC
     *
     * @param image     图片
     * @param resol     分辨率
     * @param feedRate  速度
     * @param zCutDepth Z轴下刀深度
     * @param startX    X轴起始位置
     * @param startY    Y轴起始位置
     * @return Gcode 内容
     */
/*    protected ArrayList<String> imageConvert2GcodeForCNC(Bitmap image, float resol, int feedRate, int zCutDepth, float startX, float startY) {
        if (image == null) {
            return null;
        }

        ArrayList<String> gcode = new ArrayList();
        String line;

        Log.d("Image2Gcode", "X=" + String.format("%.1f", image.getHeight() * resol) + "Y=" + String.format("%.1f", image.getHeight() * resol));
        // 插入边框信息
        line = ";Bounds:X0 Y0 to X" + String.format("%.1f", image.getWidth() * resol) + " Y" + String.format("%.1f", image.getHeight() * resol) + "\r\n";
        gcode.add(line);

        // 使用绝对坐标
        line = "G90\r\n";
        gcode.add(line);

        // 确保关闭激光
        line = "M5\r\n";
        gcode.add(line);

        // 确保关闭激光
        line = "G0 Z" + zCutDepth + "\r\n";
        gcode.add(line);


        // 使用M4激光模式进行雕刻
        line = "M3 S1000\r\n";
        gcode.add(line);

        // 设置边界速度（雕刻速度）
        line = String.format("F%d\r\n", feedRate);
        gcode.add(line);

        // 使用mm作为单位
        line = "G21\r\n";
        gcode.add(line);

        // ========== 生成图片对应的gcode ==========
        int pixBurned = 0;
        int lin = 0;    //顶部/底部 pixel
        int col = 0;    //左边/右边 pixel

        lastX = -1;//reset last positions
        lastY = -1;
        lastSz = -1;

        // 快速移动到左上角
//        line = "G0X" + String.format("%.1f", 1.0 * startX) + "Y" + String.format("%.1f", image.getHeight() * resol + startY) + "\r\n";
//        gcode.add(line);

        // 雕刻时使用G1模式
        line = "G1\r\n";
        gcode.add(line);

        //Start image
        lin = 0;//top tile
        col = 0;//Left pixel

        lastPointFlag = false;
        lastSz = -1;
        while (lin < image.getHeight() - 1) {
            //Y coordinate
            coordY = resol * (float) lin;
            while (col < image.getWidth()) // From left to right
            {
                // X coordinate
                coordX = resol * (float) col;
                // Power value
                sz = image.getPixel(col, (image.getHeight() - 1) - lin);
                sz = sz & 0xFF;   // 获取灰度值
                sz = 255 - sz;
                sz = interpolate(sz, 0, zCutDepth);
                line = generateLineForCNC(zCutDepth);
                pixBurned++;

                if ((line != null) && (!line.isEmpty())) {
                    line += "\r\n";
                    gcode.add(line);
                }

                // update postion
                lastX = coordX + startX;
                lastY = coordY + startY;
                lastSz = sz;
                col++;
            }
            line = generateEndLineForCNC(zCutDepth);
            if ((line != null) && (!line.isEmpty())) {
                line += "\r\n";
                gcode.add(line);
            }
            gcode.add(line);

            col--;
            lin++;

            //From right to left
            coordY = resol * (float) lin;
            while ((col >= 0) && (lin >= 0)) {
                //X coordinate
                coordX = resol * (float) col;

                // Power value
                sz = image.getPixel(col, (image.getHeight() - 1) - lin);
                sz = sz & 0xFF;   // 获取灰度值
                sz = 255 - sz;
                sz = interpolate(sz, 0, zCutDepth);

                line = generateLineForCNC(zCutDepth);
                pixBurned++;

                if ((line != null) && (!line.isEmpty())) {
                    line += "\r\n";
                    gcode.add(line);
                }
                // update postion
                lastX = coordX + startX;
                lastY = coordY + startY;
                lastSz = sz;
                col--;
            }

            line = generateEndLineForCNC(zCutDepth);
            if ((line != null) && (!line.isEmpty())) {
                line += "\r\n";
                gcode.add(line);
            }
            gcode.add(line);

            col++;
            lin++;
        }

        lastPointFlag = true;
        line = generateLineForCNC(zCutDepth);
        if ((line != null) && (!line.isEmpty())) {
            line += "\r\n";
            gcode.add(line);
        }

        // 关闭激光
        line = "M5\r\n";
        gcode.add(line);

        // 回到原点
        gcode.add("G0 X" + String.format("%.2f", 1.0 * startX) + " Y" + String.format("%.2f", 1.0 * startY) + "Z " + zCutDepth + "\r\n");

        return gcode;
    }*/


    /**
     * CNC 主流程（只替换灰度→深度部分以调用 interpolateDepth）
     * 注意：保持你的循环、蛇形扫描与调用 generateLineForCNC/generateEndLineForCNC 逻辑不变
     */
    protected ArrayList<String> imageConvert2GcodeForCNC(
            Bitmap image,
            float targetWidth,
            float targetHeight,
            int feedRate,
            double zCutDepth,
            float startX,
            float startY
    ) {
        if (image == null) {
            return null;
        }

        ArrayList<String> gcode = new ArrayList<>();
        String line;

        float resolX = targetWidth / image.getWidth();
        float resolY = targetHeight / image.getHeight();

        double safeZ = zCutDepth; // 贴近表面（你要求不 +2）

        line = ";Bounds:X0 Y0 to X" + String.format("%.1f", targetWidth)
                + " Y" + String.format("%.1f", targetHeight) + "\r\n";
        gcode.add(line);

        gcode.add("G90\r\n"); // 绝对坐标模式
        gcode.add("M5\r\n");  // 主轴停转
        gcode.add("G0 Z" + String.format("%.4f", safeZ) + "\r\n"); // 抬刀
        gcode.add("M3 S1000\r\n"); // 启动主轴
        gcode.add(String.format("F%d\r\n", feedRate));
        gcode.add("G21\r\n"); // 毫米单位

        int lin = 0;
        int col = 0;

        // 使用类字段（请确保类中有 double sz, double lastSz, float lastX, float lastY, boolean g0Flag）
        // sz 和 lastSz 是双精度深度（单位：mm）
        // g0Flag 用来记录当前是否处于 G0（快速移动）状态
        // lastX / lastY 用于定位

        gcode.add("G1\r\n");

        // 可调参数：gamma（对比度），depthScale（整体放大深度）
        double gamma = 2.2;       // 1.0 = 线性；>1 强化暗部
        double depthScale = 1.2;  // 如果需要整体放大深度可以 >1.0

        while (lin < image.getHeight() - 1) {
            coordY = resolY * lin;

            while (col < image.getWidth()) {
                coordX = resolX * col;

                int pixel = image.getPixel(col, (image.getHeight() - 1) - lin);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                // 加权灰度
                int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);

                // 调用 interpolateDepth（黑色最深）
                double depth = interpolateDepth(gray, 0.0, zCutDepth, gamma);

                // depthScale 用于放大或缩小整体深度（不改映射方向）
                depth *= depthScale;

                // 保证不超过 zCutDepth
                if (depth < 0.0) depth = 0.0;
                if (depth > zCutDepth) depth = zCutDepth;

                // 赋回类字段（双精度）
                this.sz = depth;

                // 生成行（使用类的 lastSz/lastX/lastY/g0Flag）
                line = generateLineForCNC(this.sz, this.lastSz, this.lastX, this.lastY, zCutDepth, safeZ, this.g0Flag);
                if (line != null && !line.isEmpty()) {
                    gcode.add(line + "\r\n");
                }

                // 更新位置与状态（保持你原来的字段类型）
                this.lastX = coordX + startX;
                this.lastY = coordY + startY;
                this.lastSz = this.sz;

                col++;
            }

            line = generateEndLineForCNC(this.sz, zCutDepth, safeZ, this.lastX, this.lastY, this.g0Flag);
            if (line != null && !line.isEmpty()) {
                gcode.add(line + "\r\n");
            }

            // 蛇形反向
            col--;
            lin++;
            coordY = resolY * lin;

            while (col >= 0 && lin >= 0) {
                coordX = resolX * col;

                int pixel = image.getPixel(col, (image.getHeight() - 1) - lin);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;

                int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);

                double depth = interpolateDepth(gray, 0.0, zCutDepth, gamma);
                depth *= depthScale;
                if (depth < 0.0) depth = 0.0;
                if (depth > zCutDepth) depth = zCutDepth;

                this.sz = depth;

                line = generateLineForCNC(this.sz, this.lastSz, this.lastX, this.lastY, zCutDepth, safeZ, this.g0Flag);
                if (line != null && !line.isEmpty()) {
                    gcode.add(line + "\r\n");
                }

                this.lastX = coordX + startX;
                this.lastY = coordY + startY;
                this.lastSz = this.sz;
                col--;
            }

            line = generateEndLineForCNC(this.sz, zCutDepth, safeZ, this.lastX, this.lastY, this.g0Flag);
            if (line != null && !line.isEmpty()) {
                gcode.add(line + "\r\n");
            }

            col++;
            lin++;
        }

        lastPointFlag = true;
        line = generateLineForCNC(this.sz, this.lastSz, this.lastX, this.lastY, zCutDepth, safeZ, this.g0Flag);
        if (line != null && !line.isEmpty()) {
            gcode.add(line + "\r\n");
        }

        gcode.add("M5\r\n");
        gcode.add("G0 X" + String.format("%.2f", startX)
                + " Y" + String.format("%.2f", startY)
                + " Z" + String.format("%.4f", safeZ) + "\r\n");

        return gcode;
    }






    /**
     * 这里直接传入原始图片即可
     *
     * @param image
     * @param printWidth
     * @param printHeight
     * @param feedrate
     * @param laserIntensity
     * @return
     */
    public static ArrayList<String> outlineImage2Gcode(Bitmap image, float printWidth, float printHeight, int feedrate, int laserIntensity, float x, float y, boolean isAir, int zDown) {
        if (image == null) {
            return null;
        }
        int imageWidht = image.getWidth();
        double scale = 1.0 * imageWidht / printWidth;
        // 通过potrace进行轮廓提取
        PotraceJ.turdsize = 2;
        PotraceJ.alphamax = 0.0;
        PotraceJ.opttolerance = 0.2;
        PotraceJ.curveoptimizing = true;
        String laserOn = "S" + laserIntensity;
        ArrayList<ArrayList<PotraceJ.Curve>> plist = PotraceJ.PotraceTrace(image);
        return PotraceJ.Export2GCode(plist, image.getHeight(), scale, feedrate, laserOn, "M5", "G0", x, y, isAir, zDown);
    }

    /**
     * 使用中心线提取方式生成 Gcode（Zhang-Suen 细化算法）
     */
    public static ArrayList<String> centerlineImage2Gcode(Bitmap image, float printWidth, float printHeight, int feedrate, int laserIntensity, float offsetX, float offsetY) {
        if (image == null) return null;
        int width = image.getWidth();
        int height = image.getHeight();
        double scaleX = printWidth / (double) width;
        double scaleY = printHeight / (double) height;

        // Step 1: 转为二值数组
        int[][] binary = bitmapToBinaryArray(image);

        // Step 2: 使用 Zhang-Suen 算法进行骨架提取
        int[][] skeleton = ZhangSuenThinning.thin(binary);

        // Step 3: 将骨架像素转为 GCode
        ArrayList<String> gcode = new ArrayList<>();
        // G代码头部初始化
        gcode.add("G90\r\n");    // 绝对坐标
        gcode.add("M5\r\n");     // 关闭激光
        gcode.add("F" + feedrate + "\r\n"); // 进给速度
        gcode.add("G21\r\n");    // 毫米单位
        gcode.add("G1\r\n");     // 线性插补模式

        boolean[][] visited = new boolean[height][width];
        List<List<int[]>> paths = new ArrayList<>();

        // 搜索所有独立路径
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (skeleton[row][col] == 1 && !visited[row][col]) {
                    List<int[]> path = new ArrayList<>();
                    tracePath(col, row, skeleton, visited, path);
                    if (path.size() > 1) {
                        optimizePathOrder(path); // 优化路径点顺序
                        paths.add(path);
                    }
                }
            }
        }

        // 生成G代码
        for (List<int[]> path : paths) {
            if (path.isEmpty()) continue;

            // 快速移动到起点（激光关闭）
            float startX = (float) (path.get(0)[0] * scaleX) + offsetX;
            float startY = (float) ((height - 1 - path.get(0)[1]) * scaleY) + offsetY;
            gcode.add(String.format(Locale.US, "G0 X%.2f Y%.2f S0\r\n", startX, startY));

            // 开启激光并开始雕刻
            gcode.add("M3 S" + laserIntensity + "\r\n");

            // 生成路径点G代码
            for (int[] point : path) {
                float coordX = (float) (point[0] * scaleX) + offsetX;
                float coordY = (float) ((height - 1 - point[1]) * scaleY) + offsetY;
                gcode.add(String.format(Locale.US, "G1 X%.2f Y%.2f S%d\r\n", coordX, coordY, laserIntensity));
            }

            // 结束线段雕刻
            gcode.add("M5\r\n");
        }

        // 返回原点
        gcode.add(String.format(Locale.US, "G0 X%.2f Y%.2f\r\n", offsetX, offsetY));
        return gcode;
    }

    /**
     * 生成灰度图Gcode，注意：传进来的bitmap为转换后的灰度image或者转换后的黑白图image
     *
     * @param image          图片
     * @param resol          分辨率
     * @param feedRate       速度
     * @param laserIntensity 激光功率
     * @param x              X轴偏移
     * @param y              Y轴偏移
     * @return Gcode内容
     */
    public ArrayList<String> image2Gcode(Bitmap image, float targetWidth, float targetHeight, float resol, int feedRate, int laserIntensity, float x, float y, ScanDirection scanDirection, GcodeProgressCallback callback) {
        return imageConvert2Gcode(image, targetWidth, targetHeight, resol, feedRate, laserIntensity, x, y, scanDirection, callback);
    }

    /**
     * 生成灰度图Gcode，注意：传进来的bitmap为转换后的灰度image或者转换后的黑白图image
     *
     * @param image        图片
     * @param targetWidth  目标雕刻宽度
     * @param targetHeight 目标雕刻高度
     * @param feedRate     速度
     * @param zCutDepth    Z轴下刀深度
     * @param x            X轴偏移
     * @param y            Y轴偏移
     * @return Gcode内容
     */
//    public ArrayList<String> image2GcodeForCNC(Bitmap image, float resol, int feedRate, int zCutDepth, float x, float y) {
//        return imageConvert2GcodeForCNC(image, resol, feedRate, zCutDepth, x, y);
//    }
    public ArrayList<String> image2GcodeForCNC(Bitmap image, float targetWidth, float targetHeight, int feedRate, int zCutDepth, float x, float y) {
        return imageConvert2GcodeForCNC(image, targetWidth, targetHeight, feedRate, zCutDepth, x, y);
    }


    /**
     * Bitmap 转二值数组（黑白图）
     */
    public static int[][] bitmapToBinaryArray(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[][] binary = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int gray = (Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3;
                binary[y][x] = gray < 128 ? 1 : 0; // 前景为 1，背景为 0
            }
        }
        return binary;
    }

    /**
     * 深度优先搜索追踪连续路径
     */
    private static void tracePath(int x, int y, int[][] skeleton, boolean[][] visited, List<int[]> path) {
        int[] dx = {1, 1, 0, -1, -1, -1, 0, 1}; // 8邻域搜索顺序
        int[] dy = {0, 1, 1, 1, 0, -1, -1, -1};

        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{x, y});

        while (!stack.isEmpty()) {
            int[] point = stack.pop();
            int px = point[0], py = point[1];

            if (px < 0 || py < 0 || px >= skeleton[0].length || py >= skeleton.length) continue;
            if (visited[py][px] || skeleton[py][px] != 1) continue;

            visited[py][px] = true;
            path.add(new int[]{px, py});

            // 按顺时针方向搜索邻域点
            for (int i = 0; i < 8; i++) {
                int nx = px + dx[i];
                int ny = py + dy[i];
                stack.push(new int[]{nx, ny});
            }
        }
    }

    /**
     * 优化路径点顺序，确保相邻点连续
     */
    private static void optimizePathOrder(List<int[]> path) {
        if (path.size() < 2) return;

        List<int[]> ordered = new ArrayList<>();
        ordered.add(path.remove(0));

        while (!path.isEmpty()) {
            int[] last = ordered.get(ordered.size() - 1);
            int nearestIdx = -1;
            int minDist = Integer.MAX_VALUE;

            // 查找最近邻点
            for (int i = 0; i < path.size(); i++) {
                int[] p = path.get(i);
                int dist = Math.abs(p[0] - last[0]) + Math.abs(p[1] - last[1]);
                if (dist < minDist) {
                    minDist = dist;
                    nearestIdx = i;
                }
            }

            if (nearestIdx != -1) {
                ordered.add(path.remove(nearestIdx));
            } else {
                break;
            }
        }

        path.clear();
        path.addAll(ordered);
    }


    public interface GcodeProgressCallback {
        void onProgress(int percent);
    }

}

