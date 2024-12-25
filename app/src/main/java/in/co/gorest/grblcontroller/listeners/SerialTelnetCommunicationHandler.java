package in.co.gorest.grblcontroller.listeners;

import android.os.Message;
import org.greenrobot.eventbus.EventBus;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import in.co.gorest.grblcontroller.events.ConsoleMessageEvent;
import in.co.gorest.grblcontroller.model.Constants;
import in.co.gorest.grblcontroller.service.GrblTelnetSerialService;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class SerialTelnetCommunicationHandler extends SerialCommunicationHandler {

    private final ExecutorService singleThreadExecutor;
    private ScheduledExecutorService grblStatusUpdater = null;

    private final WeakReference<GrblTelnetSerialService> mService;

    public SerialTelnetCommunicationHandler(GrblTelnetSerialService grblTelnetSerialService) {
        mService = new WeakReference<>(grblTelnetSerialService);
        singleThreadExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void handleMessage(Message msg) {
        final GrblTelnetSerialService grblTelnetSerialService = mService.get();
        switch (msg.what) {
            case Constants.MESSAGE_READ:
                if (msg.arg1 > 0) {
                    final String message = (String) msg.obj;
                    if (!singleThreadExecutor.isShutdown()) {
                        singleThreadExecutor.submit(() -> onTelnetSerialRead(message.trim(), grblTelnetSerialService));
                    }
                }
                break;

            case Constants.MESSAGE_WRITE:
                final String message = (String) msg.obj;
                EventBus.getDefault().post(new ConsoleMessageEvent(message));
                break;
        }
    }

    private void onTelnetSerialRead(String message, final GrblTelnetSerialService grblTelnetSerialService) {
        boolean isVersionString = onSerialRead(message);
        if (isVersionString) {
            GrblTelnetSerialService.isGrblFound = true;

            long delayMillis = grblTelnetSerialService.getStatusUpdatePoolInterval();
            ExecutorService executorService = Executors.newSingleThreadExecutor();

            for (final String startUpCommand : this.getStartUpCommands()) {
                long finalDelayMillis = delayMillis;
                executorService.execute(() -> {
                    try {
                        Thread.sleep(finalDelayMillis);
                        grblTelnetSerialService.serialWriteString(startUpCommand);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });

                delayMillis = delayMillis + grblTelnetSerialService.getStatusUpdatePoolInterval();
            }

            startGrblStatusUpdateService(grblTelnetSerialService);
        }
    }

//    private void onTelnetSerialRead(String message, final GrblTelnetSerialService grblTelnetSerialService) {
//        boolean isVersionString = onSerialRead(message);
//        if (isVersionString) {
//            GrblTelnetSerialService.isGrblFound = true;
//
//            Handler handler = new Handler(Looper.getMainLooper());
//
//            long delayMillis = grblTelnetSerialService.getStatusUpdatePoolInterval();
//            for (final String startUpCommand : this.getStartUpCommands()) {
//                handler.postDelayed(() -> grblTelnetSerialService.serialWriteString(startUpCommand), delayMillis);
//
//                delayMillis = delayMillis + grblTelnetSerialService.getStatusUpdatePoolInterval();
//            }
//
//            startGrblStatusUpdateService(grblTelnetSerialService);
//        }
//    }

    private void startGrblStatusUpdateService(final GrblTelnetSerialService grblTelnetSerialService) {
        stopGrblStatusUpdateService();

        grblStatusUpdater = Executors.newScheduledThreadPool(1);
        grblStatusUpdater.scheduleWithFixedDelay(() -> grblTelnetSerialService.serialWriteByte(GrblUtils.GRBL_STATUS_COMMAND),
                grblTelnetSerialService.getStatusUpdatePoolInterval(), grblTelnetSerialService.getStatusUpdatePoolInterval(), TimeUnit.MILLISECONDS);
    }

    public void stopGrblStatusUpdateService() {
        if (grblStatusUpdater != null) grblStatusUpdater.shutdownNow();
    }
}