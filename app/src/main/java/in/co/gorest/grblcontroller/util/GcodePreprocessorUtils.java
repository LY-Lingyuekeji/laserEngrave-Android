package in.co.gorest.grblcontroller.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GcodePreprocessorUtils {

    private static final String EMPTY = "";
    private static final Pattern WHITE_SPACE = Pattern.compile("\\s");
    private static final Pattern COMMENT = Pattern.compile("\\(.*\\)|\\s*;.*|%$");
    private static final Pattern COMMENT_PARSE = Pattern.compile("(?<=\\()[^()]*|(?<=;).*|%");

    public static String removeWhiteSpace(String command){
        return WHITE_SPACE.matcher(command).replaceAll(EMPTY).toUpperCase();
    }

    public static String parseComment(String command) {
        String comment = EMPTY;

        Matcher matcher = COMMENT_PARSE.matcher(command);
        if(matcher.find()) comment = matcher.group(0);
        return comment;
    }

    public static String removeComment(String command) {
        return COMMENT.matcher(command).replaceAll(EMPTY);
    }

}
