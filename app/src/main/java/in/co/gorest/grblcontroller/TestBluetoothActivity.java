package in.co.gorest.grblcontroller;

import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class TestBluetoothActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private ArrayAdapter<String> listAdapter;
    private ListView listView;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic writeChar;

    private static final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHARACTERISTIC_UUID_WRITE = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CHARACTERISTIC_UUID_NOTIFY = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testbluetooth);

        listView = findViewById(R.id.device_list);
        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> checkPermissionAndScan());

        Button btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> sendBLEMessage("$J=G91 X10 F5000\n")); // 示例

        Button btnUploadTestFile = findViewById(R.id.btn_upload_file);
        btnUploadTestFile.setOnClickListener(v -> {
            sendBLEMessage("[ESP486]/1111111.nc\r\n");
            uploadTestFile("123.nc"); // 替换为你的实际路径
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice device = deviceList.get(position);
            connectToDevice(device);
        });
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null && !deviceList.contains(device)) {
                deviceList.add(device);
                listAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private void checkPermissionAndScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.BLUETOOTH_CONNECT
                }, 1002);
                return;
            }
        } else {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1002);
                return;
            }
        }
        startBleScan();
    }

    @SuppressLint("MissingPermission")
    private void startBleScan() {
        listAdapter.clear();
        deviceList.clear();

        if (!bluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1001);
            return;
        }

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(scanCallback);
        Toast.makeText(this, "开始扫描...", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bluetoothLeScanner.stopScan(scanCallback);
            Toast.makeText(this, "扫描结束", Toast.LENGTH_SHORT).show();
        }, 8000);
    }

    @SuppressLint("MissingPermission")
    private void connectToDevice(BluetoothDevice device) {
        Toast.makeText(this, "连接设备：" + device.getName(), Toast.LENGTH_SHORT).show();
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "连接成功，发现服务中...");
                bluetoothGatt = gatt;
                bluetoothGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "连接断开");
                runOnUiThread(() -> Toast.makeText(TestBluetoothActivity.this, "连接断开", Toast.LENGTH_SHORT).show());
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(SERVICE_UUID);
                if (service == null) {
                    Log.e("BLE", "服务未找到");
                    return;
                }

                // 设置通知
                BluetoothGattCharacteristic notifyChar = service.getCharacteristic(CHARACTERISTIC_UUID_NOTIFY);
                gatt.setCharacteristicNotification(notifyChar, true);
                BluetoothGattDescriptor descriptor = notifyChar.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_UUID);
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }

                // 保存写入特征
                writeChar = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
                if (writeChar != null) {
                    writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    Log.d("BLE", "写入特征准备完成");
                } else {
                    Log.e("BLE", "写入特征未找到");
                }
            } else {
                Log.e("BLE", "服务发现失败，状态码: " + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            String response = new String(characteristic.getValue(), StandardCharsets.UTF_8);
            Log.d("BLE", "接收返回数据: " + response);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("BLE", "数据发送成功！");
                runOnUiThread(() -> Toast.makeText(TestBluetoothActivity.this, "发送成功", Toast.LENGTH_SHORT).show());
            } else {
                Log.e("BLE", "写入失败，状态码: " + status);
            }
            writeInProgress = false;
        }
    };

    @SuppressLint("MissingPermission")
    private void sendBLEMessage(String message) {
        if (bluetoothGatt == null || writeChar == null) {
            Toast.makeText(this, "未连接或特征未准备好", Toast.LENGTH_SHORT).show();
            return;
        }
        writeChar.setValue(message.getBytes(StandardCharsets.UTF_8));
        boolean success = bluetoothGatt.writeCharacteristic(writeChar);
        Log.d("BLE", "写入数据：" + message + "，结果：" + success);
    }

    private boolean writeInProgress = false;

    private void uploadTestFile(String fileName) {
        new Thread(() -> {
            try {
                File file = new File(getExternalFilesDir("laser"), fileName);
                if (!file.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show());
                    return;
                }

                FileInputStream inputStream = new FileInputStream(file);
                long fileLength = file.length();
                Log.d("BLE", "文件长度: " + fileLength);

                byte[] buffer = new byte[5000];
                int readBytes;

                while ((readBytes = inputStream.read(buffer)) != -1) {
                    byte[] header = new byte[4];
                    if (readBytes >= buffer.length) {
                        header[0] = 0x0A; // 中间数据包
                        header[1] = (byte) 0xA0;
                    } else {
                        header[0] = 0x1A; // 最后一个数据块
                        header[1] = (byte) 0xA2;
                    }
                    header[2] = (byte) ((readBytes >> 8) & 0xFF);
                    header[3] = (byte) (readBytes & 0xFF);

                    sendBLEPacketBlocking(header);

                    int fullPackets = readBytes / 500;
                    int remainder = readBytes % 500;

                    for (int i = 0; i < fullPackets; i++) {
                        byte[] segment = Arrays.copyOfRange(buffer, i * 500, (i + 1) * 500);
                        sendBLEPacketBlocking(segment);
                    }

                    if (remainder > 0) {
                        byte[] lastSegment = Arrays.copyOfRange(buffer, fullPackets * 500, readBytes);
                        sendBLEPacketBlocking(lastSegment);
                    }

                    // 每段后发送结束包
                    byte[] endPacket = new byte[]{(byte) 0xA0, 0x0A};
                    sendBLEPacketBlocking(endPacket);
                }

                inputStream.close();
                Log.d("BLE", "文件上传完成");

                stopSendFile();
                Log.d("BLE", "文件上传完成，已发送停止包");

                runOnUiThread(() -> Toast.makeText(this, "上传完成", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 同步阻塞写入 BLE 包
    private void sendBLEPacketBlocking(byte[] data) throws InterruptedException {
        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) return;
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) return;

        // 设置写入参数
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(data);

        writeInProgress = true;
        @SuppressLint("MissingPermission") boolean success = bluetoothGatt.writeCharacteristic(characteristic);
        Log.d("BLE", "写入 " + data.length + " 字节: " + success);

        // 如果写入失败，直接中断
        if (!success) {
            throw new RuntimeException("写入失败");
        }

        // 等待写完成
        int waitCount = 0;
        while (writeInProgress && waitCount < 20) {
            Thread.sleep(10);
            waitCount++;
        }
    }

//    public void uploadTestFileT(String fileName) {
//        try {
//            File file = new File(getExternalFilesDir("laser"), fileName);
//            if (!file.exists()) {
//                Toast.makeText(this, "文件不存在", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            FileInputStream inputStream = new FileInputStream(file);
//            long totalLength = file.length();
//            Log.d("BLE", "totalLength=" + totalLength);
//
//            boolean sending = true;
//            byte[] readBuffer = new byte[15];  // 一次读15字节
//            int readLen;
//
//            while (sending && (readLen = inputStream.read(readBuffer)) != -1) {
//                byte[] sendPacket = new byte[readLen + 5];
//
//                // 构造包头
//                if (readLen <= 15) {
//                    sendPacket[0] = 0x0A;
//                    sendPacket[1] = (byte) 0xA0;
//                } else {
//                    sendPacket[0] = 0x1A;
//                    sendPacket[1] = (byte) 0xA2;
//                }
//
//                sendPacket[2] = (byte) readLen;
//
//                // 拷贝数据
//                System.arraycopy(readBuffer, 0, sendPacket, 3, readLen);
//
//                // 构造包尾
//                sendPacket[3 + readLen] = (byte) 0xA0;
//                sendPacket[4 + readLen] = 0x0A;
//
//                // 发送
//                boolean success = sendBLEPacket(sendPacket);
//                Log.d("BLE", "写入数据（" + sendPacket.length + "字节）结果：" + success);
//
//                if (!success) {
//                    Log.e("BLE", "发送失败，尝试停止传输");
//                    sending = false;
//                }
//
//                Thread.sleep(10);  // 延迟防设备处理不过来
//            }
//
//            inputStream.close();
////            Log.d("BLE", "文件上传完成");
//            // 修改为：
//            stopSendFile();
//            Log.d("BLE", "文件上传完成，已发送停止包");
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(this, "上传失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }


    @SuppressLint("MissingPermission")
    private boolean sendBLEPacket(byte[] data) {
        if (bluetoothGatt == null) return false;

        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) return false;

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID_WRITE);
        if (characteristic == null) return false;

        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        characteristic.setValue(data);
        boolean result = bluetoothGatt.writeCharacteristic(characteristic);
        return result;
    }

    private void stopSendFile() {
        byte[] stopPacket = new byte[5];
        stopPacket[0] = 0x1A;           // 类似你原来 PREPENDQ
        stopPacket[1] = (byte) 0xA1;    // 停止命令标识
        stopPacket[2] = 0x00;           // 数据长度为 0
        stopPacket[3] = (byte) 0xA0;    // 尾部开始
        stopPacket[4] = 0x0A;           // 尾部结束

        boolean result = sendBLEPacket(stopPacket);
        Log.d("BLE", "发送停止包结果: " + result);
    }




    @SuppressLint("MissingPermission")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }
}



