package in.co.gorest.grblcontroller.util;

import java.io.File;
import java.util.List;

/**
 * 作者: liuhuaqian on 2021-04-13.
 */
public interface GcodeResults {
    void onGcodeResults(String results, File file);

    void onGcodeResults(List<String> gcode);
}
