package in.co.gorest.grblcontroller.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/* loaded from: classes2.dex */
public class BLEService extends Service {
    public static final String EXTRA_DATA = "UART.EXTRA_DATA";
    private static final String TAG = "BLEService";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothManager mBluetoothManager;
    private BluetoothDevice mDevice;
    private File mFileToSend;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private byte resendbyte;
    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID CHARACTERISTIC_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private final UUID CHARACTERISTIC_UUID_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private ArrayList<Byte> mDataBuffer = new ArrayList<>();
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() { // from class: makerbase.com.mkslaser.Service.BLEService.1
        @SuppressLint("MissingPermission")
        @Override // android.bluetooth.BluetoothAdapter.LeScanCallback
        public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bArr) {
            if (bluetoothDevice.getName() == null || !bluetoothDevice.getName().equals("MKS_BLE")) {
                return;
            }
            BLEService bLEService = BLEService.this;
            bLEService.mBluetoothGatt = bluetoothDevice.connectGatt(bLEService.getApplicationContext(), false, BLEService.this.mGattCallback);
            BLEService.this.mDevice = bluetoothDevice;
        }
    };
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() { // from class: makerbase.com.mkslaser.Service.BLEService.2
        @SuppressLint("MissingPermission")
        @Override // android.bluetooth.BluetoothGattCallback
        public void onConnectionStateChange(BluetoothGatt bluetoothGatt, int i, int i2) {
            if (i2 == 2) {
                bluetoothGatt.discoverServices();
                Log.i(BLEService.TAG, "蓝牙连接");
            } else if (i2 == 0) {
                BLEService.this.mBluetoothGatt.close();
                Log.i(BLEService.TAG, "蓝牙断开! status = " + i);
            }
        }

        @SuppressLint("MissingPermission")
        @Override // android.bluetooth.BluetoothGattCallback
        public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int i) {
            Log.i(BLEService.TAG, "ServicesDiscovered");
            if (i == 0) {
                bluetoothGatt.setCharacteristicNotification(bluetoothGatt.getService(BLEService.this.SERVICE_UUID).getCharacteristic(BLEService.this.CHARACTERISTIC_UUID), true);
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicRead(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            bluetoothGattCharacteristic.getUuid();
            if (i == 0) {
                Log.v(BLEService.TAG, "onCharacteristicRead");
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicWrite(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic, int i) {
            bluetoothGattCharacteristic.getUuid();
            if (i != 0) {
                Log.v(BLEService.TAG, "onCharacteristicWrite");
            }
        }

        @Override // android.bluetooth.BluetoothGattCallback
        public void onCharacteristicChanged(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
            for (byte b : bluetoothGattCharacteristic.getValue()) {
                BLEService.this.mDataBuffer.add(Byte.valueOf(b));
            }
            Log.i(BLEService.TAG, "onCharacteristicChanged=" + bluetoothGattCharacteristic.getUuid().toString());
            Log.i(BLEService.TAG, "onCharacteristicChanged: str=" + new String(bluetoothGattCharacteristic.getValue()));
            super.onCharacteristicChanged(bluetoothGatt, bluetoothGattCharacteristic);
        }
    };
    private final BroadcastReceiver bluetoothCommandReceiver = new BroadcastReceiver() { // from class: makerbase.com.mkslaser.Service.BLEService.3
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(BLEService.TAG, "onReceive: " + action);
            if (action.equals("BLUETOOTH_SEND")) {
                byte[] byteArrayExtra = intent.getByteArrayExtra("BLUETOOTH_EXTRA");
                BLEService.this.resendbyte = byteArrayExtra[0];
                BLEService.this.writeRXCharacteristic1(byteArrayExtra);
                return;
            }
            if (action.equals("BLUETOOTH_UNBIND")) {
                BLEService.this.disConnectDevice();
            }
        }
    };
    private final BroadcastReceiver bluetoothCommandReceiver1 = new BroadcastReceiver() { // from class: makerbase.com.mkslaser.Service.BLEService.4
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("BLUETOOTH_SEND1")) {
                BLEService.this.writeRXCharacteristic1(intent.getByteArrayExtra("BLUETOOTH_SEND1"));
            } else if (action.equals("BLUETOOTH_UNBIND1")) {
                BLEService.this.disConnectDevice();
            }
        }
    };
    private BroadcastReceiver bluetoothDataReceiver = new BroadcastReceiver() { // from class: makerbase.com.mkslaser.Service.BLEService.5
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            intent.getAction();
            Log.i("BluetoothService", "bluetoothData received");
        }
    };

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return new BluetoothBinder();
    }

    /* loaded from: classes2.dex */
    public class BluetoothBinder extends Binder {
        public BluetoothBinder() {
        }

        public BLEService getService() {
            return BLEService.this;
        }
    }

    public static void Start(Context context) {
        context.startService(new Intent(context, (Class<?>) BLEService.class));
    }

    public static void Stop(Context context) {
        context.stopService(new Intent(context, (Class<?>) BLEService.class));
    }

    @SuppressLint("MissingPermission")
    public void startScan() {
        this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
    }

    @SuppressLint("MissingPermission")
    public void connectDevice(String str) {
        String str2 = TAG;
        Log.e(str2, "connectDevice " + str);
        BluetoothDevice remoteDevice = this.mBluetoothAdapter.getRemoteDevice(str);
        if (remoteDevice == null) {
            Log.e(str2, "connectDevice get remote device failed!");
            return;
        }
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
        this.mBluetoothGatt = remoteDevice.connectGatt(this, false, this.mGattCallback);
    }

    @SuppressLint("MissingPermission")
    public void disConnectDevice() {
        String str = TAG;
        Log.e(str, "disConnectDevice");
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
        } else {
            Log.e(str, "disConnectDevice bluetoothGatt is null");
        }
    }

    @SuppressLint("MissingPermission")
    public void sendData(byte[] bArr) {
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(this.SERVICE_UUID).getCharacteristic(this.CHARACTERISTIC_UUID);
            characteristic.setValue(bArr);
            this.mBluetoothGatt.writeCharacteristic(characteristic);
        }
    }

    public boolean prepareSendFile(String str) {
        try {
            this.mFileToSend = new File(str);
            this.mInputStream = new FileInputStream(this.mFileToSend);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendFile() {
        try {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = this.mInputStream.read(bArr);
                if (read != -1) {
                    this.mOutputStream.write(bArr, 0, read);
                } else {
                    this.mOutputStream.close();
                    this.mInputStream.close();
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public byte[] receiveData() {
        byte[] bArr = new byte[this.mDataBuffer.size()];
        for (int i = 0; i < this.mDataBuffer.size(); i++) {
            bArr[i] = this.mDataBuffer.get(i).byteValue();
        }
        this.mDataBuffer.clear();
        return bArr;
    }

    @SuppressLint("MissingPermission")
    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        LocalBroadcastManager.getInstance(this).registerReceiver(this.bluetoothCommandReceiver, bluetoothDataIntentFilter());
        LocalBroadcastManager.getInstance(this).registerReceiver(this.bluetoothCommandReceiver1, bluetoothDataIntentFilter1());
        @SuppressLint("WrongConstant") BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
        this.mBluetoothManager = bluetoothManager;
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        this.mBluetoothAdapter = adapter;
        adapter.enable();
    }

    @SuppressLint("MissingPermission")
    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        BluetoothGatt bluetoothGatt = this.mBluetoothGatt;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
        }
    }

    private static IntentFilter bluetoothDataIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BLUETOOTH_SEND");
        intentFilter.addAction("BLUETOOTH_UNBIND");
        return intentFilter;
    }

    private static IntentFilter bluetoothDataIntentFilter1() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("BLUETOOTH_SEND1");
        intentFilter.addAction("BLUETOOTH_UNBIND1");
        return intentFilter;
    }

    private void sendBluetoothStatusBroadcast(String str) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(str));
    }

    private void sendBluetoothDataBroadcast(String str, byte[] bArr) {
        Intent intent = new Intent(str);
        intent.putExtra(EXTRA_DATA, bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void bluesend1(String str, byte[] bArr) {
        Intent intent = new Intent(str);
        intent.putExtra("BLUETOOTH_SEND1", bArr);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @SuppressLint("MissingPermission")
    public void writeRXCharacteristic1(byte[] bArr) {
        BluetoothGattService service = this.mBluetoothGatt.getService(this.SERVICE_UUID);
        if (service == null) {
            Log.e(TAG, "Rx service not found! in writeRxChar");
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(this.CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) {
            Log.e(TAG, "Rx charateristic not found!");
            return;
        }
        characteristic.setValue(bArr);
        if (this.mBluetoothGatt.writeCharacteristic(characteristic)) {
            Log.i("BluetoothService1111", "write characteristic success");
        } else {
            Log.e("BluetoothService1111", "write characteristic failed");
        }
    }
}
