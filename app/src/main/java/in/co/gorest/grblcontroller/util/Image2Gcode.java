package in.co.gorest.grblcontroller.util;

import android.graphics.Bitmap;
import java.util.ArrayList;
import java.util.Locale;

public class Image2Gcode {
    float coordX;//X
    float coordY;//Y
    int sz;//S (or Z)
    float lastX;//Last x/y  coords for compare
    float lastY;
    int lastSz;//last 'S' value for compare
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
                line += "G0 X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " S0";
                g0Flag = true;
            } else {
                if (g0Flag) {
                    line += "G1 ";
                    g0Flag = false;
                }
                line += "X" + String.format("%.2f", lastX) + " Y" + String.format("%.2f", lastY) + " S" + lastSz;
            }
        }

        return line;
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

    protected ArrayList<String> imageConvert2Gcode(Bitmap image, float resol, int feedRate, int laserIntensity, float startX, float startY) {
        if (image == null) {
            return null;
        }

        ArrayList<String> gcode = new ArrayList();
        String line;

        // 使用绝对坐标
        line = "G90\r\n";
        gcode.add(line);

        // 确保关闭激光
        line = "M5\r\n";
        gcode.add(line);

        // 使用M4激光模式进行雕刻
        line = "M4 S0\r\n";
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
        line = "G0X" + String.format("%.1f", 1.0 * startX) + "Y" + String.format("%.1f", image.getHeight() * resol + startY) + "\r\n";
        gcode.add(line);

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
                sz = interpolate(sz, 0, laserIntensity);
                line = generateLine();
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
            line = generateEndLine();
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
                sz = interpolate(sz, 0, laserIntensity);

                line = generateLine();
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

            line = generateEndLine();
            if ((line != null) && (!line.isEmpty())) {
                line += "\r\n";
                gcode.add(line);
            }
            gcode.add(line);

            col++;
            lin++;
        }

        lastPointFlag = true;
        line = generateLine();
        if ((line != null) && (!line.isEmpty())) {
            line += "\r\n";
            gcode.add(line);
        }

        // 关闭激光
        line = "M5\r\n";
        gcode.add(line);

        // 回到原点
        gcode.add("G0 X" + String.format("%.2f", 1.0 * startX) + " Y" + String.format("%.2f", 1.0 * startY) + "\r\n");

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
    public static ArrayList<String> outlineImage2Gcode(Bitmap image, float printWidth, float printHeight, int feedrate, int laserIntensity, float x, float y) {
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
        return PotraceJ.Export2GCode(plist, image.getHeight(), scale, feedrate, laserOn, "M5", "G0", x, y);
    }

    /**
     * 生成灰度图Gcode，注意：传进来的bitmap为转换后的灰度image或者转换后的黑白图image
     */
    public ArrayList<String> image2Gcode(Bitmap image, float resol, int feedRate, int laserIntensity, float x, float y) {
        return imageConvert2Gcode(image, resol, feedRate, laserIntensity, x, y);
    }
}

