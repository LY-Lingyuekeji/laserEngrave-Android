package in.co.gorest.grblcontroller.util;

import android.util.Log;
import cn.wandersnail.commons.util.ShellUtils;
import in.co.gorest.grblcontroller.model.CommandSetInfo;
import java.util.ArrayList;

public class engraveUtils {
    public static String string2Command(CommandSetInfo commandSetInfo) {
        String str;
        String str2;
        String str3 = PrefUtil.getInstance().get("mainboard", "normal");
        String option = commandSetInfo.getOption();
        String distance = commandSetInfo.getDistance();
        String speed = commandSetInfo.getSpeed();
        if (str3.contains("esp")) {
            if (option.contains("x_sub")) {
                return "$J=G91 G21 X-" + distance + " F" + speed + ShellUtils.COMMAND_LINE_END;
            }
            if (option.contains("y_add")) {
                return "$J=G91 G21 Y" + distance + " F" + speed + ShellUtils.COMMAND_LINE_END;
            }
            if (option.contains("home")) {
                return "$J=G90 G21 X0 Y0 F" + speed + ShellUtils.COMMAND_LINE_END;
            }
            if (option.contains("y_sub")) {
                return "$J=G91 G21 Y-" + distance + " F" + speed + ShellUtils.COMMAND_LINE_END;
            }
            if (option.contains("x_add")) {
                return "$J=G91 G21 X" + distance + " F" + speed + ShellUtils.COMMAND_LINE_END;
            }
            if (option.contains("stronglight")) {
                return "M3 S1000\nG1 F1000\n";
            }
            if (option.contains("weaklight")) {
                return "M3 S50\nG1 F1000\n";
            }
            if (option.contains("offlight")) {
                return "M5\n";
            }
            if (option.contains("oncool")) {
                return "M8\n";
            }
            if (option.contains("offcool")) {
                return "M9\n";
            }
            if (option.contains("locat")) {
                return "G92 X0 Y0\n";
            }
            if (option.contains("engrave")) {
                return "M24";
            }
            if (option.contains("stop")) {
                if (PrefUtil.getInstance().get("currentStatus", "IDLE").equals("PAUSE")) {
                    return "~";
                }
                if (PrefUtil.getInstance().get("currentStatus", "IDLE").equals("PRINTING")) {
                    return "!";
                }
            } else {
                if (option.contains("over")) {
                    return "\u0018";
                }
                int i = 0;
                if (option.contains("power")) {
                    String currentPower = commandSetInfo.getCurrentPower();
                    Log.i("command_power", "--" + currentPower + "--" + commandSetInfo.getPower());
                    if (currentPower == null) {
                        currentPower = "100";
                    }
                    int parseInt = Integer.parseInt(currentPower);
                    int parseInt2 = Integer.parseInt(commandSetInfo.getPower());
                    if (parseInt2 == 100) {
                        return "\u0099";
                    }
                    if (parseInt > parseInt2) {
                        int i2 = parseInt - parseInt2;
                        int i3 = i2 / 10;
                        int i4 = i2 % 10;
                        str2 = "";
                        for (int i5 = 0; i5 < i3; i5++) {
                            str2 = str2 + "\u009b";
                        }
                        while (i < i4) {
                            str2 = str2 + "\u009d";
                            i++;
                        }
                    } else {
                        if (parseInt >= parseInt2) {
                            return "";
                        }
                        int i6 = parseInt2 - parseInt;
                        int i7 = i6 / 10;
                        int i8 = i6 % 10;
                        str2 = "";
                        for (int i9 = 0; i9 < i7; i9++) {
                            str2 = str2 + "\u009a";
                        }
                        while (i < i8) {
                            str2 = str2 + "\u009c";
                            i++;
                        }
                    }
                    return str2;
                }
                if (option.contains("workSpeed")) {
                    String currentSpeed = commandSetInfo.getCurrentSpeed();
                    if (currentSpeed == null) {
                        currentSpeed = "100";
                    }
                    int parseInt3 = Integer.parseInt(currentSpeed);
                    int parseInt4 = Integer.parseInt(commandSetInfo.getSpeed());
                    if (parseInt4 == 100) {
                        return "\u0090";
                    }
                    if (parseInt3 > parseInt4) {
                        int i10 = parseInt3 - parseInt4;
                        int i11 = i10 / 10;
                        int i12 = i10 % 10;
                        str = "";
                        for (int i13 = 0; i13 < i11; i13++) {
                            str = str + "\u0092";
                        }
                        while (i < i12) {
                            str = str + "\u0094";
                            i++;
                        }
                    } else {
                        if (parseInt3 >= parseInt4) {
                            return "";
                        }
                        int i14 = parseInt4 - parseInt3;
                        int i15 = i14 / 10;
                        int i16 = i14 % 10;
                        str = "";
                        for (int i17 = 0; i17 < i15; i17++) {
                            str = str + "\u0091";
                        }
                        while (i < i16) {
                            str = str + "\u0093";
                            i++;
                        }
                    }
                    return str;
                }
                if (!option.contains("engraveTime") && !option.contains("moveSpeed")) {
                    if (option.contains("checkAndEngraving")) {
                        String printFileName = commandSetInfo.getPrintFileName();
                        PrefUtil.getInstance().put("currentProgress", "0");
                        return "[ESP220]/" + printFileName + ShellUtils.COMMAND_LINE_END;
                    }
                    if (!option.contains("preDraw") && !option.contains("delete") && option.contains("engravingname")) {
                        return "";
                    }
                }
            }
        } else {
            if (option.contains("x_sub")) {
                return "G91\nG1 X-" + distance + " F3000\nG90\n";
            }
            if (option.contains("y_add")) {
                return "G91\nG1 Y" + distance + " F3000\nG90\n";
            }
            if (option.contains("home")) {
                return "G90 X0 Y0\n";
            }
            if (option.contains("y_sub")) {
                return "G91\nG1 Y-" + distance + " F3000\nG90\n";
            }
            if (option.contains("x_add")) {
                return "G91\nG1 X" + distance + " F3000\nG90\n";
            }
            if (option.contains("stronglight")) {
                return "M03 S1000\n";
            }
            if (option.contains("weaklight")) {
                return "M03 S50\n";
            }
            if (option.contains("offlight")) {
                return "M05 S0\n";
            }
            if (option.contains("locat")) {
                return "G92 X0 Y0\n";
            }
            if (option.contains("engrave")) {
                return "M24\n";
            }
            if (option.contains("stop")) {
                if (PrefUtil.getInstance().get("currentStatus", "IDLE").equals("PAUSE")) {
                    return "M24\nM03 S255\n";
                }
                if (PrefUtil.getInstance().get("currentStatus", "IDLE").equals("PRINTING")) {
                    return "M25\nM05 S0\n";
                }
            } else {
                if (option.contains("over")) {
                    return "M26\nM05 S0\n";
                }
                if (!option.contains("power") && !option.contains("workSpeed") && !option.contains("engraveTime") && !option.contains("moveSpeed") && !option.contains("checkAndEngraving") && !option.contains("preDraw") && !option.contains("delete") && option.contains("engravingname")) {
                    return "M994\n";
                }
            }
        }
        return "";
    }

    public static boolean CMDWithoutOK(byte[] bArr) {
        ArrayList arrayList = new ArrayList();
        arrayList.add("!");
        arrayList.add("~");
        arrayList.add("M5");
        return arrayList.contains(new String(bArr));
    }
}
