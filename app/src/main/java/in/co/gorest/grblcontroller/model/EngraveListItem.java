package in.co.gorest.grblcontroller.model;

import org.json.JSONException;
import org.json.JSONObject;

public class EngraveListItem {

    private int iconResId;
    private String text;
    private boolean isVisible;

    public EngraveListItem(int iconResId, String text, boolean isVisible) {
        this.iconResId = iconResId;
        this.text = text;
        this.isVisible = isVisible;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getText() {
        return text;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    // 转换为JSON字符串
    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("iconResId", iconResId);
            jsonObject.put("text", text);
            jsonObject.put("isVisible", isVisible);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    // 从JSON字符串解析
    public static EngraveListItem fromJSON(String jsonString) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return new EngraveListItem(
                    jsonObject.getInt("iconResId"),
                    jsonObject.getString("text"),
                    jsonObject.getBoolean("isVisible")
            );
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
