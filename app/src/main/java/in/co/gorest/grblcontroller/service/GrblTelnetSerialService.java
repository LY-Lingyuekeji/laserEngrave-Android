package in.co.gorest.grblcontroller.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import in.co.gorest.grblcontroller.R;
import in.co.gorest.grblcontroller.events.GrblRealTimeCommandEvent;
import in.co.gorest.grblcontroller.events.UiToastEvent;
import in.co.gorest.grblcontroller.helpers.NotificationHelper;
import in.co.gorest.grblcontroller.listeners.SerialTelnetCommunicationHandler;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.model.GcodeCommand;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class GrblTelnetSerialService extends Service {

    private static final String TAG = GrblTelnetSerialService.class.getSimpleName();
    public static final String KEY_IP_ADDRESS = "KEY_IP_ADDRESS";
    public static final String KEY_PORT = "KEY_PORT";
    WifiManager wifiManager;
    Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    private int mNewState;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static volatile boolean isGrblFound = false;

    SerialTelnetCommunicationHandler serialTelnetCommunicationHandler;

    private final IBinder mBinder = new GrblTelnetSerialServiceBinder();

    private long statusUpdatePoolInterval = Constants.GRBL_STATUS_UPDATE_INTERVAL;


    @Override
    public void onCreate() {
        super.onCreate();
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // 设置初始状态
        mState = STATE_NONE;
        mNewState = mState;

        if (wifiManager == null) {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_wifi_manager_error), true, true));
            stopSelf();
        } else {
            // 启动前台服务
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                startForeground(Constants.BLUETOOTH_SERVICE_NOTIFICATION_ID, this.getNotification(null));
            }
            serialTelnetCommunicationHandler = new SerialTelnetCommunicationHandler(this);
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class GrblTelnetSerialServiceBinder extends Binder {
        public GrblTelnetSerialService getService() {
            return GrblTelnetSerialService.this;
        }
    }

    public void setMessageHandler(Handler grblServiceMessageHandler) {
        this.mHandler = grblServiceMessageHandler;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.start();

        if (intent != null) {
            String ipAddress = intent.getStringExtra(KEY_IP_ADDRESS);
            int port = intent.getIntExtra(KEY_PORT, 23);
            if (ipAddress != null) {
                try {
                    this.connect(ipAddress, port);
                } catch (IllegalArgumentException e) {
                    EventBus.getDefault().post(new UiToastEvent(e.getMessage(), true, true));
                    disconnectService();
                    stopSelf();
                }
            }
        } else {
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_unknown_error), true, true));
            disconnectService();
            stopSelf();
        }

        return Service.START_NOT_STICKY;
    }

    public void disconnectService() {
        serialTelnetCommunicationHandler.stopGrblStatusUpdateService();
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        this.stop();
        isGrblFound = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectService();
        mState = STATE_NONE;
        this.stop();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            stopForeground(true);
        }
        updateUserInterfaceTitle();
        EventBus.getDefault().unregister(this);
    }

    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        mNewState = mState;
        if (mHandler != null) {
            mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
        }
    }

    public synchronized int getState() {
        return mState;
    }

    synchronized void start() {
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        updateUserInterfaceTitle();
    }

    synchronized void connect(String ipAddress, int port) {
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(ipAddress, port);
        mConnectThread.start();
        updateUserInterfaceTitle();
    }

    @SuppressLint("MissingPermission")
    private synchronized void connected(Socket socket) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.DEVICE_NAME, socket.getRemoteSocketAddress().toString());
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        updateUserInterfaceTitle();

        try {
            wait(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isGrblFound) serialWriteByte(GrblUtils.GRBL_RESET_COMMAND);
    }

    synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        mState = STATE_NONE;
        updateUserInterfaceTitle();
    }

    private void connectionFailed() {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, getString(R.string.text_unable_to_connect_to_device));
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }
        mState = STATE_NONE;
        updateUserInterfaceTitle();
        this.start();
    }

    private void connectionLost() {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOAST, getString(R.string.text_device_connection_was_lost));
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        mState = STATE_NONE;
        updateUserInterfaceTitle();
        this.start();
    }

    private class AcceptThread extends Thread {
        private ServerSocket serverSocket;
        private boolean isRunning = true;

        public AcceptThread() {
            try {
                // 创建ServerSocket以接受客户端连接
                serverSocket = new ServerSocket(8080);
                mState = STATE_LISTEN;
                Log.d(TAG, "ServerSocket created on port " + 8080);
            } catch (IOException e) {
                Log.e(TAG, "ServerSocket creation failed", e);
                serverSocket = null;
                mState = STATE_NONE;
            }

        }

        public void run() {
            Log.d(TAG, "BEGIN AcceptThread");
            setName("AcceptThread");

            while (mState != STATE_CONNECTED && isRunning) {
                if (serverSocket == null) {
                    Log.e(TAG, "ServerSocket is null, cannot accept connections");
                    break;
                }
                try {
                    // 接受客户端连接
                    Socket clientSocket = serverSocket.accept();
                    Log.d(TAG, "Connection accepted from " + clientSocket.getRemoteSocketAddress());
                    // 连接成功，处理客户端连接
                    connected(clientSocket);
                } catch (IOException e) {
                    if (isRunning) {
                        Log.e(TAG, "Accept failed", e);
                        connectionFailed();
                    }
                    break;
                }

            }
            Log.i(TAG, "END AcceptThread");
            // 清理资源
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close ServerSocket", e);
            }
        }

        public void cancel() {
            isRunning = false;
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of ServerSocket failed", e);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final String ipAddress;
        private final int port;
        private Socket mmSocket;

        public ConnectThread(String ipAddress, int port) {
            this.ipAddress = ipAddress;
            this.port = port;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectThread");
            setName("ConnectThread");

            try {
                mmSocket = new Socket(ipAddress, port);
            } catch (IOException e) {
                Log.e(TAG, "unable to connect", e);
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final Socket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(Socket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN ConnectedThread");

            // Keep listening to the InputStream while connected
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(mmInStream))) {
                String readMessage;
                while ((readMessage = reader.readLine()) != null) {
                    serialTelnetCommunicationHandler.obtainMessage(Constants.MESSAGE_READ, readMessage.length(), -1, readMessage).sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                connectionLost();
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private void serialWriteBytes(byte[] b) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        r.write(b);
    }
    private static final byte[] BYTE_NEWLINE = {0x0A};

    public void serialWriteString(String s) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] buffer = s.getBytes();
                serialWriteBytes(buffer);
                serialWriteBytes(BYTE_NEWLINE);
                serialTelnetCommunicationHandler.obtainMessage(Constants.MESSAGE_WRITE, s.length(), -1, s).sendToTarget();
            }
        }).start();

    }

    public void serialWriteByte(byte b) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] c = {b};
                serialWriteBytes(c);
            }
        }).start();

    }

    private Notification getNotification(String message) {
        if (message == null)
            message = getString(R.string.text_telnet_service_foreground_message);

        return new NotificationCompat.Builder(getApplicationContext(), NotificationHelper.CHANNEL_SERVICE_ID)
                .setContentTitle(getString(R.string.text_telnet_service))
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(getResources().getColor(R.color.colorWhite))
                .setAutoCancel(true).build();
    }

    public long getStatusUpdatePoolInterval() {
        return this.statusUpdatePoolInterval;
    }

    public void setStatusUpdatePoolInterval(long poolInterval) {
        this.statusUpdatePoolInterval = poolInterval;
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onGrblGcodeSendEvent(GcodeCommand event) {
        serialWriteString(event.getCommandString());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onGrblRealTimeCommandEvent(GrblRealTimeCommandEvent grblRealTimeCommandEvent) {
        serialWriteByte(grblRealTimeCommandEvent.getCommand());
    }
}
