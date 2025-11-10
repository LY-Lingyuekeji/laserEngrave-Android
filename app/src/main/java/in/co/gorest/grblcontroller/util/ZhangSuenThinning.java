package in.co.gorest.grblcontroller.util;

public class ZhangSuenThinning {
    public static int[][] thin(int[][] binaryImage) {
        int[][] preprocessed = preprocessBinaryImage(binaryImage);
        int height = preprocessed.length;
        int width = height > 0 ? preprocessed[0].length : 0;
        int[][] skeleton = new int[height][width];

        // 复制预处理后的图像
        for (int i = 0; i < height; i++) {
            System.arraycopy(preprocessed[i], 0, skeleton[i], 0, width);
        }

        boolean hasChanged;
        do {
            hasChanged = false;
            boolean step1Deleted = iterate(skeleton, true);
            boolean step2Deleted = iterate(skeleton, false);
            hasChanged = step1Deleted || step2Deleted;
        } while (hasChanged);

        return postProcess(skeleton);
    }

    private static int[][] preprocessBinaryImage(int[][] image) {
        int height = image.length;
        int width = height > 0 ? image[0].length : 0;
        int[][] result = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                result[y][x] = image[y][x] > 0 ? 1 : 0;
            }
        }
        return result;
    }

    private static int[][] postProcess(int[][] skeleton) {
        int height = skeleton.length;
        int width = height > 0 ? skeleton[0].length : 0;
        int[][] result = new int[height][width];
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (skeleton[y][x] == 0) continue;
                int count = 0;
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        count += skeleton[y + dy][x + dx];
                    }
                }
                // 保留至少有两个邻居的像素
                if (count >= 3) result[y][x] = 1;
            }
        }
        return result;
    }

    private static boolean iterate(int[][] matrix, boolean step1) {
        int height = matrix.length;
        int width = height > 0 ? matrix[0].length : 0;
        boolean[][] toDelete = new boolean[height][width];
        boolean deleted = false;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (matrix[y][x] == 0) continue;

                // 获取8邻域像素值
                int[] n = getNeighbors(matrix, x, y);

                // 条件1: 邻域中至少有2个且不超过6个前景像素
                int count = n[0] + n[1] + n[2] + n[3] + n[4] + n[5] + n[6] + n[7];
                if (count < 2 || count > 6) continue;

                // 条件2: 邻域中0→1的跳变次数为1
                int transitions = 0;
                for (int i = 0; i < 8; i++) {
                    if (n[i] == 0 && n[(i + 1) % 8] == 1) transitions++;
                }
                if (transitions != 1) continue;

                // 条件3: 步骤1需满足特定邻域条件
                boolean condition3;
                if (step1) {
                    condition3 = (n[0] * n[2] * n[4] == 0) && (n[2] * n[4] * n[6] == 0);
                } else {
                    condition3 = (n[0] * n[2] * n[6] == 0) && (n[0] * n[4] * n[6] == 0);
                }

                if (condition3) {
                    toDelete[y][x] = true;
                    deleted = true;
                }
            }
        }

        // 删除标记的像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (toDelete[y][x]) matrix[y][x] = 0;
            }
        }

        return deleted;
    }

    // 获取8邻域像素值（顺时针顺序）
    private static int[] getNeighbors(int[][] matrix, int x, int y) {
        return new int[]{
                matrix[y - 1][x],     // P2
                matrix[y - 1][x + 1], // P3
                matrix[y][x + 1],    // P4
                matrix[y + 1][x + 1], // P5
                matrix[y + 1][x],     // P6
                matrix[y + 1][x - 1], // P7
                matrix[y][x - 1],     // P8
                matrix[y - 1][x - 1]  // P9
        };
    }

}

