package in.co.gorest.grblcontroller.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.model.MessageModel;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // 定义两个消息类型：发送的消息和接收的消息
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;

    private List<MessageModel> messageModelList;  // 存储消息的列表

    // 构造方法，传入消息列表
    public MessageAdapter(List<MessageModel> messageModelList) {
        this.messageModelList = messageModelList;
    }

    // 根据消息类型返回不同的视图类型
    @Override
    public int getItemViewType(int position) {
        MessageModel messageModel = messageModelList.get(position);
        return messageModel.isSentByUser() ? TYPE_SENT : TYPE_RECEIVED;  // 判断是发送的消息还是接收的消息
    }

    // 创建 ViewHolder，根据视图类型加载不同的布局
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sent_message, parent, false);
            return new SentMessageViewHolder(view);  // 发送消息的布局
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_received_message, parent, false);
            return new ReceivedMessageViewHolder(view);  // 接收消息的布局
        }
    }

    // 绑定数据到 ViewHolder
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageModel messageModel = messageModelList.get(position);
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(messageModel);  // 绑定发送的消息数据
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(messageModel);  // 绑定接收的消息数据
        }
    }

    // 获取总的消息数量
    @Override
    public int getItemCount() {
        return messageModelList.size();
    }

    // 发送消息的 ViewHolder
    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;  // 用于显示消息内容的 TextView

        public SentMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.sent_message_text);  // 初始化 TextView
        }

        // 绑定数据
        public void bind(MessageModel messageModel) {
            messageText.setText(messageModel.getContent());  // 设置消息内容
        }
    }

    // 接收消息的 ViewHolder
    public static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;

        public ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.received_message_text);  // 初始化 TextView
        }

        // 绑定数据
        public void bind(MessageModel messageModel) {
            messageText.setText(messageModel.getContent());  // 设置消息内容
        }
    }
}
