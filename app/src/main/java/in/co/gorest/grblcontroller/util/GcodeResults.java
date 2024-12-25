package in.co.gorest.grblcontroller.util;

import java.io.File;
import java.util.List;

public interface GcodeResults {
    void onGcodeResults(String results, File file);

    void onGcodeResults(List<String> gcode);
}
