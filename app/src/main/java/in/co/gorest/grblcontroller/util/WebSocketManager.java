package in.co.gorest.grblcontroller.util;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import in.co.gorest.grblcontroller.events.ServiceMessageEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketManager {

    private static WebSocketManager instance;

    private WebSocket webSocket;
    private OkHttpClient client;
    private boolean isConnected = false;
    private String currentIp;

    private Handler handler = new Handler(Looper.getMainLooper());
    private final String TAG = "WebSocketManager";

    private WebSocketListener socketListener;

    private OnReceiveListener receiveListener;

    private int reconnectAttempts = 0; // 重连次数
    private final int MAX_RECONNECT_ATTEMPTS = 10; // 最大重连次数
    private final long RECONNECT_INTERVAL_MS = 3000; // 重连间隔（毫秒）

    private final Runnable heartbeatRunnable = new Runnable() {
        @Override
        public void run() {
            if (isConnected && webSocket != null && currentIp  != null) {
                sendHttpQuery(); // 发送 HTTP 查询指令
                handler.postDelayed(this, 1000); // 每 1 秒发一次心跳
            }
        }
    };

    public static WebSocketManager getInstance() {
        if (instance == null) {
            synchronized (WebSocketManager.class) {
                if (instance == null) {
                    instance = new WebSocketManager();
                }
            }
        }
        return instance;
    }

    private WebSocketManager() {
        client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS) // WebSocket不需要超时
                .build();
    }

    public void connect(String ip) {
        if (isConnected && ip.equals(currentIp)) {
            Log.d(TAG, "已经连接，无需重复连接");
            return;
        }

        currentIp = ip;
        String url = "ws://" + ip + ":81";

        Request request = new Request.Builder()
                .url(url)
                .build();

        socketListener = new WebSocketListenerImpl();
        webSocket = client.newWebSocket(request, socketListener);
    }

    public void disconnect() {
        stopHeartbeat();
        if (webSocket != null) {
            webSocket.close(1000, "手动断开");
            webSocket = null;
        }
        isConnected = false;
    }

    public void reconnect() {
        disconnect();
        if (currentIp != null) {
            connect(currentIp);
        }
    }

    public boolean isConnected() {
        return isConnected;
    }


    public void send(String message) {
        if (isConnected && webSocket != null && currentIp  != null) {
            sendMessage(message); // 发送 HTTP 查询指令
        } else {
            Log.w(TAG, "未连接，无法发送字符串");
        }
    }

//    public void send(byte[] bytes) {
//        if (isConnected && webSocket != null) {
//            webSocket.send(ByteString.of(bytes));
//        } else {
//            Log.w(TAG, "未连接，无法发送二进制");
//        }
//    }

    public void setOnReceiveListener(OnReceiveListener listener) {
        this.receiveListener = listener;
    }

    private void startHeartbeat() {
        handler.removeCallbacks(heartbeatRunnable);
        handler.postDelayed(heartbeatRunnable, 1000);
    }

    private void stopHeartbeat() {
        handler.removeCallbacks(heartbeatRunnable);
    }

    private class WebSocketListenerImpl extends WebSocketListener {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            isConnected = true;
            Log.d(TAG, "连接成功");
            reconnectAttempts = 0; // 重置重连次数
            startHeartbeat();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d(TAG, "收到消息: " + text);
            if (receiveListener != null) {
                receiveListener.onMessage(text);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d(TAG, "收到二进制消息，长度: " + bytes.size());
            // 可添加二进制处理逻辑
            try {
                String message = bytes.string(Charset.forName("UTF-8")); // 默认 UTF-8 解码
                Log.d(TAG, "二进制转换后的消息: " + message);

                if (receiveListener != null) {
                    receiveListener.onMessage(message);
                }

                EventBus.getDefault().post(new ServiceMessageEvent(message));
            } catch (Exception e) {
                Log.e(TAG, "二进制消息解码失败: " + e.getMessage());
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.w(TAG, "连接关闭中: " + reason);
            isConnected = false;
            stopHeartbeat();
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.w(TAG, "连接已关闭: " + reason);
            isConnected = false;
            stopHeartbeat();
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.e(TAG, "连接失败: " + t.getMessage());
            isConnected = false;
            stopHeartbeat();

            scheduleReconnect(); // 只在这里执行自动重连
        }
    }


    private void scheduleReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS && currentIp != null) {
            reconnectAttempts++;
            Log.d(TAG, "准备重连，第 " + reconnectAttempts + " 次...");
            handler.postDelayed(() -> connect(currentIp), RECONNECT_INTERVAL_MS);
        } else {
            Log.w(TAG, "达到最大重连次数，停止重连");
        }
    }

    // 心跳
    private void sendHttpQuery() {
        String url = "http://" + currentIp + "/command?commandText=?&PAGEID=0";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "心跳请求失败: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (receiveListener != null) {
                        receiveListener.onMessage(response.body().string());
                    }
                } else {
                    Log.w(TAG, "心跳响应错误码: " + response.code());
                }
            }
        });
    }

    // 实际的消息发送
    private void sendMessage(String message) {
        String url = "http://" + currentIp + "/command?commandText=" + message + "&PAGEID=0";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "消息发送失败: " + e.getMessage());
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (receiveListener != null) {
                        receiveListener.onMessage(response.body().string());
                    }
                } else {
                    Log.w(TAG, "消息发送响应错误码: " + response.code());
                }
            }
        });
    }

    public interface OnReceiveListener {
        void onMessage(String message);
    }
}

