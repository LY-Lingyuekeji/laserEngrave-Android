package in.co.gorest.grblcontroller.util;

import android.util.Log;
import cn.wandersnail.commons.util.ShellUtils;
import in.co.gorest.grblcontroller.model.PrinterFiles;
import java.util.ArrayList;
import java.util.List;

public class GcodeHelper {
    public static String beginWriteDataToFile() {
        return "M110\n";
    }

    public static String cancelPrint() {
        return "M26\n";
    }

    public static String endWriteDataToFile() {
        return "M29\n";
    }

    public static String getPrintStatus() {
        return "M27\n";
    }

    public static String getPrinterStatus() {
        return "M997\n";
    }

    public static String getPrintingFile() {
        return "M994\n";
    }

    public static String getPrintingTime() {
        return "M992\n";
    }

    public static String getTemp() {
        return "M105\n";
    }

    public static String getUFileList() {
        return "M20 0:\n";
    }

    public static String moveStepOne() {
        return "G91\n";
    }

    public static String moveStepThree() {
        return "G90\n";
    }

    public static String pausePrint() {
        return "M25\n";
    }

    public static String resetX() {
        return " G28 X0\n";
    }

    public static String resetXYZ() {
        return "G28 X0 Y0\n";
    }

    public static String resetY() {
        return " G28 Y0\n";
    }

    public static String resetZ() {
        return " G28 Z0\n";
    }

    public static String startOrStopPrint() {
        return "M24\n";
    }

    public static String moveX(float f, float f2) {
        return "G91\nG1 X" + f + " F" + f2 + "\nG90\n";
    }

    public static String moveStepTwo(String str, float f, float f2) {
        return "G1 " + str + f + " F" + f2 + ShellUtils.COMMAND_LINE_END;
    }

    public static String moveY(float f, float f2) {
        return "G91\nG1 Y" + f + " F" + f2 + "\nG90\n";
    }

    public static String moveZ(float f, float f2) {
        return "G91\nG1 Z" + f + " F" + f2 + "\nG90\n";
    }

//    public static String movePrinterHead(int i, float f, float f2) {
//        return ExifInterface.GPS_DIRECTION_TRUE + i + "G91\nG1 E" + f + " F" + f2 + "\nG90\n";
//    }

    public static String getPrinterHeadTemp(int i, int i2) {
        return "M104 T" + i + " S" + i2 + ShellUtils.COMMAND_LINE_END;
    }

    public static String revisePrinterHeadTemp(int i, int i2) {
        return "M104 T" + i + " S" + i2 + ShellUtils.COMMAND_LINE_END;
    }

    public static String reviseHotBedTemp(int i) {
        return "M104 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String getHotBedTemp(int i) {
        return "M140 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String setFanSpeed(int i) {
        return "M106 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String getFanSpeed(int i) {
        return "M106 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String speedPercent(int i) {
        return "M220 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String getSpeedPercent(int i) {
        return "M220 S" + i + ShellUtils.COMMAND_LINE_END;
    }

    public static String getFileList() {
        Log.i("指令", "M20");
        return "M20 1:/\n";
    }

    public static String getSpecificFileList(String str) {
        Log.i("指令", "M20 " + str);
        return "M20 " + str + ShellUtils.COMMAND_LINE_END;
    }

    public static String getSdCardFileList() {
        Log.i("指令", "M20 1:");
        return "M20 1:\n";
    }

    public static String pickFile(String str) {
        return "M23 " + str + ShellUtils.COMMAND_LINE_END;
    }

    public static String createFile(String str) {
        return "M28 " + str + ShellUtils.COMMAND_LINE_END;
    }

    public static String deleteFileInSDCark(String str) {
        return "M30 " + str + ShellUtils.COMMAND_LINE_END;
    }

    public static boolean isGcodeFile(String str) {
        if (str.contains(".")) {
            String[] split = str.split(".");
            if (split[split.length - 1].toLowerCase().equals("g") || split[split.length - 1].toLowerCase().equals("gc") || split[split.length - 1].toLowerCase().equals("gco") || split[split.length - 1].toLowerCase().equals("gcod") || split[split.length - 1].toLowerCase().equals("gcode")) {
                return true;
            }
        }
        return false;
    }

    public static List<PrinterFiles> decodeFileList(String str) {
        Log.i("readData返回值", str);
        ArrayList arrayList = new ArrayList();
        String[] split = str.split(ShellUtils.COMMAND_LINE_END);
        boolean z = false;
        for (int i = 0; i < split.length && !split[i].equals("End file list"); i++) {
            if (z) {
                if (split[i].contains(" ")) {
                    split[i].split(" ");
                    if (split[i].contains("/")) {
                        if (FileUtils.isNumeric(split[i].split(" ")[split[i].split(" ").length - 1])) {
                            arrayList.add(new PrinterFiles(split[i].split("/")[split[i].split("/").length - 1].split(" ")[0].toLowerCase(), FileSizeUtils.getFileSize(Integer.parseInt(split[i].split(" ")[split[i].split(" ").length - 1])), split[i].toLowerCase()));
                            Log.i("FileName", split[i].split("/")[split[i].split("/").length - 1].split(" ")[0].toLowerCase());
                        } else {
                            arrayList.add(new PrinterFiles(split[i].split("/")[split[i].split("/").length - 1].toLowerCase(), "", split[i].toLowerCase()));
                        }
                    } else if (FileUtils.isNumeric(split[i].split(" ")[split[i].split(" ").length - 1])) {
                        arrayList.add(new PrinterFiles(split[i].split(" ")[0].toLowerCase(), "", split[i].toLowerCase()));
                    } else {
                        arrayList.add(new PrinterFiles(split[i].toLowerCase(), "", split[i].toLowerCase()));
                    }
                } else if (split[i].contains("/")) {
                    arrayList.add(new PrinterFiles(split[i].split("/")[split[i].split("/").length - 1].toLowerCase(), "", split[i].toLowerCase()));
                } else {
                    arrayList.add(new PrinterFiles(split[i].toLowerCase(), "", split[i].toLowerCase()));
                }
            }
            if (split[i].equals("Begin file list")) {
                z = true;
            }
        }
        return arrayList;
    }

    public static List<PrinterFiles> decodeFileListForMKS(String str) {
        Log.i("readData返回值", str);
        ArrayList arrayList = new ArrayList();
        String[] split = str.split(ShellUtils.COMMAND_LINE_END);
        boolean z = false;
        for (int i = 0; i < split.length && !split[i].equals("End file list"); i++) {
            if (z) {
                if (split[i].contains(" ")) {
                    if (FileUtils.isNumeric(split[i].split(" ")[split[i].split(" ").length - 1])) {
                        arrayList.add(new PrinterFiles(split[i].split(" ")[0].toLowerCase(), "", split[i].toLowerCase()));
                    } else {
                        arrayList.add(new PrinterFiles(split[i].toLowerCase(), "", split[i].toLowerCase()));
                    }
                } else {
                    arrayList.add(new PrinterFiles(split[i].toLowerCase(), "", split[i].toLowerCase()));
                }
            }
            if (split[i].equals("Begin file list")) {
                z = true;
            }
        }
        return arrayList;
    }

    public static float decodePrintProgress(String str) {
        int i;
        if (str.contains(" ")) {
            String[] split = str.split(" ");
            for (int i2 = 0; i2 < split.length; i2++) {
                if (split[i2].equals("byte") && split.length - 1 >= (i = i2 + 1)) {
                    String[] split2 = split[i].split("/");
                    if (split2.length > 1) {
                        try {
                            return (Float.parseFloat(split2[0]) / Float.parseFloat(split2[1])) * 100.0f;
                        } catch (Exception unused) {
                            return 0.0f;
                        }
                    }
                }
            }
        }
        return 0.0f;
    }

    public static String[] decodePrintTemp(String str) {
        String[] strArr = new String[4];
        if (str.contains("T0:")) {
            strArr[0] = getTempData(str.split("T0:"));
        } else if (str.contains("T:")) {
            strArr[0] = getTempData(str.split("T:"));
        } else {
            strArr[0] = "0 / 0";
        }
        if (str.contains("T1:")) {
            strArr[1] = getTempData(str.split("T1:"));
        } else {
            strArr[1] = "0 / 0";
        }
        if (str.contains("B:")) {
            strArr[2] = getTempData(str.split("B:"));
        } else {
            strArr[2] = "0 / 0";
        }
        strArr[3] = "0 / 0";
        return strArr;
    }

    public static String[] decodeAutoTemp(String str) {
        String[] strArr = new String[4];
        if (str.contains("E:0")) {
            String[] split = str.split("E:0")[0].replace(" ", "").split(":");
            if (split.length > 1) {
                strArr[0] = split[1].replace(ShellUtils.COMMAND_LINE_END, "") + " / 0";
            } else {
                strArr[0] = "0 / 0";
            }
        } else {
            strArr[0] = "0 / 0";
        }
        if (str.contains("E:1")) {
            String[] split2 = str.split("E:1")[0].split(" ");
            if (split2.length > 2) {
                String[] split3 = split2[2].replace(" ", "").split(":");
                if (split3.length > 1) {
                    strArr[1] = split3[1].replace(ShellUtils.COMMAND_LINE_END, "") + " / 0";
                }
            } else {
                strArr[1] = "0 / 0";
            }
        } else {
            strArr[1] = "0 / 0";
        }
        if (str.contains("B:")) {
            String[] split4 = str.split("B:");
            strArr[2] = split4[split4.length - 1].replace(ShellUtils.COMMAND_LINE_END, "") + " / 0";
        } else {
            strArr[2] = "0 / 0";
        }
        strArr[3] = "0 / 0";
        return strArr;
    }

    private static String getTempData(String[] strArr) {
        if (strArr.length > 1) {
            String[] split = strArr[1].split(" ");
            if (split.length > 1) {
                return split[0] + " / " + split[1].substring(1, split[1].length());
            }
        }
        return "0 / 0";
    }

    public static boolean isNCFile(String str) {
        Log.i("isNCFile--1-", str);
        if (str.contains(".")) {
            Log.i("isNCFile--2-", str);
            String[] split = str.split(".");
            if (split.length > 0) {
                Log.i("isNCFile--3-", str);
                if (split[split.length - 1].toLowerCase().equals("nc")) {
                    Log.i("isNCFile--4-", str);
                    return true;
                }
            }
        }
        return false;
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x0091  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x00aa  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static List<PrinterFiles> decodeFileListForESP(String r13) {
        /*
            java.lang.String r0 = "readData返回值"
            android.util.Log.i(r0, r13)
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.lang.String r1 = "\n"
            java.lang.String[] r13 = r13.split(r1)
            r1 = 0
            r2 = r1
        L12:
            int r3 = r13.length
            if (r2 >= r3) goto Lce
            r3 = r13[r2]
            java.lang.String r4 = "[SD Free:"
            boolean r3 = r3.contains(r4)
            if (r3 == 0) goto L21
            goto Lce
        L21:
            r3 = r13[r2]
            java.lang.String r4 = "[FILE:"
            boolean r3 = r3.contains(r4)
            if (r3 == 0) goto Lca
            r3 = r13[r2]
            java.lang.String r4 = "]"
            boolean r3 = r3.contains(r4)
            if (r3 == 0) goto Lca
            r3 = r13[r2]
            java.lang.String r5 = "\\|"
            java.lang.String[] r3 = r3.split(r5)
            int r5 = r3.length
            java.lang.String r6 = "/"
            java.lang.String r7 = ""
            r8 = 1
            if (r5 <= r8) goto Lac
            r5 = r3[r1]
            java.lang.String r9 = ":"
            java.lang.String[] r5 = r5.split(r9)
            int r10 = r5.length
            r11 = 2
            if (r10 <= r8) goto L87
            r5 = r5[r8]
            java.lang.String[] r5 = r5.split(r6)
            int r10 = r5.length
            if (r10 != r11) goto L87
            r10 = r5[r8]
            java.lang.String r10 = r10.toLowerCase()
            java.lang.String r12 = ".nc"
            boolean r10 = r10.contains(r12)
            if (r10 != 0) goto L84
            r10 = r5[r8]
            java.lang.String r10 = r10.toLowerCase()
            java.lang.String r12 = ".gcode"
            boolean r10 = r10.contains(r12)
            if (r10 != 0) goto L84
            r10 = r5[r8]
            java.lang.String r10 = r10.toLowerCase()
            java.lang.String r12 = ".gc"
            boolean r10 = r10.contains(r12)
            if (r10 == 0) goto L87
        L84:
            r5 = r5[r8]
            goto L88
        L87:
            r5 = r7
        L88:
            r3 = r3[r8]
            java.lang.String[] r3 = r3.split(r9)
            int r9 = r3.length
            if (r9 <= r8) goto Laa
            r9 = r3[r8]
            boolean r4 = r9.contains(r4)
            if (r4 == 0) goto La7
            r4 = r3[r8]
            r3 = r3[r8]
            int r3 = r3.length()
            int r3 = r3 - r11
            java.lang.String r3 = r4.substring(r1, r3)
            goto Lae
        La7:
            r3 = r3[r8]
            goto Lae
        Laa:
            r3 = r7
            goto Lae
        Lac:
            r3 = r7
            r5 = r3
        Lae:
            boolean r4 = r5.equals(r7)
            if (r4 != 0) goto Lca
            boolean r4 = r3.equals(r7)
            if (r4 != 0) goto Lca
            makerbase.com.mkslaser.Models.PrinterFiles r4 = new makerbase.com.mkslaser.Models.PrinterFiles
            long r7 = java.lang.Long.parseLong(r3)
            java.lang.String r3 = makerbase.com.mkslaser.Utils.FileSizeUtils.getFileSize(r7)
            r4.<init>(r5, r3, r6)
            r0.add(r4)
        Lca:
            int r2 = r2 + 1
            goto L12
        Lce:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: makerbase.com.mkslaser.Common.GcodeHelper.decodeFileListForESP(java.lang.String):java.util.List");
    }
}
