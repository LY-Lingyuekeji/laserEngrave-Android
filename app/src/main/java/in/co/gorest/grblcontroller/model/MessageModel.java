package in.co.gorest.grblcontroller.model;

public class MessageModel {
    private String content;  // 消息内容
    private boolean isSentByUser;  // 是否是用户自己发送的消息

    // 构造方法
    public MessageModel(String content, boolean isSentByUser) {
        this.content = content;
        this.isSentByUser = isSentByUser;
    }

    // 获取消息内容
    public String getContent() {
        return content;
    }

    // 判断是否是用户自己发送的消息
    public boolean isSentByUser() {
        return isSentByUser;
    }
}
