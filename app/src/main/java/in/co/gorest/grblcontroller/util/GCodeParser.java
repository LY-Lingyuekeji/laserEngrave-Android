package in.co.gorest.grblcontroller.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class GCodeParser {

    public static MaxPowerResult parseGCode(InputStream inputStream) throws IOException {
        List<DrawCommand> commands = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        float currentX = 0;
        float currentY = 0;
        float currentPower = 0;
        String currentCommand = "G1";

        float maxPower = 0;
        float minPower = Float.MAX_VALUE;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 跳过注释行
            if (line.startsWith(";")) continue;

            // 如果是 G0 或 G1 命令
            if (line.startsWith("G0") || line.startsWith("G1")) {
                currentCommand = line.substring(0, 2);
            }

            // 提取 X、Y、S 坐标
            float newX = currentX;
            float newY = currentY;
            float newPower = currentPower;

            newX = extractCoordinate(line, "X", newX);
            newY = extractCoordinate(line, "Y", newY);
            newPower = extractPower(line, newPower);

            // 更新最大功率值
            if (newPower > maxPower) {
                maxPower = newPower;
            }
            if (newPower < minPower) {
                minPower = newPower;
            }

            if (newX != currentX || newY != currentY || newPower != currentPower) {
                commands.add(new DrawCommand(newX, newY, newPower));
                currentX = newX;
                currentY = newY;
                currentPower = newPower;
            }
        }

        reader.close();
        return new MaxPowerResult(commands, maxPower, minPower);
    }

    private static float extractCoordinate(String line, String axis, float currentCoordinate) {
        int index = line.indexOf(axis);
        if (index != -1) {
            try {
                String value = line.substring(index + 1).split(" ")[0];
                return Float.parseFloat(value);
            } catch (NumberFormatException e) {
                return currentCoordinate;
            }
        }
        return currentCoordinate;
    }

    private static float extractPower(String line, float currentPower) {
        int index = line.indexOf("S");
        if (index != -1) {
            try {
                String value = line.substring(index + 1).split(" ")[0];
                // 考虑到 S 值是百分比的乘以 10 后的值，将其转换回百分比
                return Float.parseFloat(value) / 10.0f;  // 除以10得到原始百分比
            } catch (NumberFormatException e) {
                return currentPower;
            }
        }
        return currentPower;
    }

    public static class MaxPowerResult {
        public List<DrawCommand> commands;
        public float maxPower;
        public float minPower;

        public MaxPowerResult(List<DrawCommand> commands, float maxPower, float minPower) {
            this.commands = commands;
            this.maxPower = maxPower;
            this.minPower = minPower;
        }
    }
}
