package in.co.gorest.grblcontroller.util;

import java.text.DecimalFormat;
import okhttp3.internal.ws.RealWebSocket;

/* loaded from: classes2.dex */
public class FileSizeUtils {
    public static long ONE_GB = 0;
    public static long ONE_KB = 1024;
    public static long ONE_MB;
    public static long ONE_PB;
    public static long ONE_TB;

    static {
        long j = RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE * RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE;
        ONE_MB = j;
        long j2 = j * RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE;
        ONE_GB = j2;
        long j3 = j2 * RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE;
        ONE_TB = j3;
        ONE_PB = j3 * RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE;
    }

    public static String getFileSize(long j) {
        if (j < 0) {
            return String.valueOf(j);
        }
        String humanReadableFileSize = getHumanReadableFileSize(j, ONE_PB, "PB");
        if (humanReadableFileSize != null) {
            return humanReadableFileSize;
        }
        String humanReadableFileSize2 = getHumanReadableFileSize(j, ONE_TB, "TB");
        if (humanReadableFileSize2 != null) {
            return humanReadableFileSize2;
        }
        String humanReadableFileSize3 = getHumanReadableFileSize(j, ONE_GB, "GB");
        if (humanReadableFileSize3 != null) {
            return humanReadableFileSize3;
        }
        String humanReadableFileSize4 = getHumanReadableFileSize(j, ONE_MB, "MB");
        if (humanReadableFileSize4 != null) {
            return humanReadableFileSize4;
        }
        String humanReadableFileSize5 = getHumanReadableFileSize(j, ONE_KB, "KB");
        if (humanReadableFileSize5 != null) {
            return humanReadableFileSize5;
        }
        return String.valueOf(j) + "B";
    }

    private static String getHumanReadableFileSize(long j, long j2, String str) {
        if (j == 0) {
            return "0K";
        }
        if (j / j2 < 1) {
            return null;
        }
        return new DecimalFormat("######.##" + str).format(j / j2);
    }
}
