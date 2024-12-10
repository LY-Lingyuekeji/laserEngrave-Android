package in.co.gorest.grblcontroller.util;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class TelnetHelper {

    private Socket telnetSocket;
    private BufferedReader input;
    private OutputStream output;

    // Telnet连接
    public void connectToTelnet(final String ip, final int port, final TelnetConnectionCallback callback) {
        new ConnectTask(callback).execute(ip, String.valueOf(port));
    }

    // 发送命令
    public void sendCommand(final String command) {
        new Thread(() -> {
            try {
                if (output != null) {
                    output.write(command.getBytes());
                    output.flush();
                    Log.d("TelnetClient", "Command sent: " + command);
                }
            } catch (IOException e) {
                Log.e("TelnetClient", "Error sending command to Telnet server", e);
            }
        }).start();
    }

    // 读取响应
    public void readResponse(final TelnetResponseCallback callback) {
        new Thread(() -> {
            try {
                String line;
                while ((line = input.readLine()) != null) {
                    Log.d("TelnetClient", "Telnet Response: " + line);
                    if (callback != null) {
                        callback.onResponseReceived(line);
                    }
                }
            } catch (IOException e) {
                Log.e("TelnetClient", "Error reading from Telnet server", e);
            }
        }).start();
    }

    // 断开连接
    public void disconnect() {
        try {
            if (telnetSocket != null) {
                telnetSocket.close();
                Log.d("TelnetClient", "Telnet connection closed");
            }
        } catch (IOException e) {
            Log.e("TelnetClient", "Error closing Telnet connection", e);
        }
    }

    // Telnet 连接任务
    private class ConnectTask extends AsyncTask<String, Void, Boolean> {

        private final TelnetConnectionCallback callback;

        public ConnectTask(TelnetConnectionCallback callback) {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String ip = params[0];
            int port = Integer.parseInt(params[1]);

            try {
                telnetSocket = new Socket(ip, port);
                input = new BufferedReader(new InputStreamReader(telnetSocket.getInputStream()));
                output = telnetSocket.getOutputStream();
                return true;
            } catch (IOException e) {
                Log.e("TelnetClient", "Error connecting to Telnet server", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (callback != null) {
                if (success) {
                    callback.onConnected();
                } else {
                    callback.onConnectionFailed();
                }
            }
        }
    }

    // 连接回调接口
    public interface TelnetConnectionCallback {
        void onConnected();
        void onConnectionFailed();
    }

    // 响应回调接口
    public interface TelnetResponseCallback {
        void onResponseReceived(String response);
    }
}
