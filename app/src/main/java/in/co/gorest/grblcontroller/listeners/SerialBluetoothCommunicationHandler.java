package in.co.gorest.grblcontroller.listeners;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import in.co.gorest.grblcontroller.events.ConsoleMessageEvent;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.service.GrblBluetoothSerialService;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class SerialBluetoothCommunicationHandler extends SerialCommunicationHandler {

    private static final String TAG = SerialBluetoothCommunicationHandler.class.getSimpleName();

    private final ExecutorService singleThreadExecutor;
    private ScheduledExecutorService grblStatusUpdater = null;

    private final WeakReference<GrblBluetoothSerialService> mService;

    public SerialBluetoothCommunicationHandler(GrblBluetoothSerialService grblBluetoothSerialService){
        mService = new WeakReference<>(grblBluetoothSerialService);
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void handleMessage(Message msg){

        final GrblBluetoothSerialService grblBluetoothSerialService = mService.get();

        switch(msg.what){
            case Constants.MESSAGE_READ:
                if(msg.arg1 > 0){
                    final String message = (String) msg.obj;
                    if(!singleThreadExecutor.isShutdown()){
                        singleThreadExecutor.submit(() -> onBluetoothSerialRead(message.trim(), grblBluetoothSerialService));
                    }
                }
                break;

            case Constants.MESSAGE_WRITE:
                final String message = (String) msg.obj;
                EventBus.getDefault().post(new ConsoleMessageEvent(message));
                break;
        }

    }

    private void onBluetoothSerialRead(String message, final GrblBluetoothSerialService grblBluetoothSerialService){

        boolean isVersionString = onSerialRead(message);
        if(isVersionString){
            GrblBluetoothSerialService.isGrblFound = true;

            Handler handler = new Handler(Looper.getMainLooper());

            long delayMillis = grblBluetoothSerialService.getStatusUpdatePoolInterval();
            for(final String startUpCommand: this.getStartUpCommands()){
                handler.postDelayed(() -> grblBluetoothSerialService.serialWriteString(startUpCommand), delayMillis);

                delayMillis = delayMillis + grblBluetoothSerialService.getStatusUpdatePoolInterval();
            }

            startGrblStatusUpdateService(grblBluetoothSerialService);
        }

    }

    private void startGrblStatusUpdateService(final GrblBluetoothSerialService grblBluetoothSerialService){

        stopGrblStatusUpdateService();

        grblStatusUpdater = Executors.newScheduledThreadPool(1);
        grblStatusUpdater.scheduleWithFixedDelay(() -> grblBluetoothSerialService.serialWriteByte(GrblUtils.GRBL_STATUS_COMMAND), grblBluetoothSerialService.getStatusUpdatePoolInterval(), grblBluetoothSerialService.getStatusUpdatePoolInterval(), TimeUnit.MILLISECONDS);

    }

    public void stopGrblStatusUpdateService(){
        Log.d(TAG, "stop true");
        if(grblStatusUpdater != null) grblStatusUpdater.shutdownNow();
    }

}
