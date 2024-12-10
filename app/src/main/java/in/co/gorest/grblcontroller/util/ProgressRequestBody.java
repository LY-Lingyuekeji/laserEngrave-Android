package in.co.gorest.grblcontroller.util;

import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

public class ProgressRequestBody extends RequestBody {

    private final File file;
    private final String contentType;
    private final UploadCallbacks listener;
    private static final int DEFAULT_BUFFER_SIZE = 64 * 1024;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage, long bytesUploaded, long totalBytes, String  uploadSpeed);
        void onError();
        void onFinish();
    }

    public ProgressRequestBody(final File file, String contentType, final UploadCallbacks listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        long totalBytesRead = 0;
        long startTime = System.nanoTime();

        try (Source source = Okio.source(file); BufferedSource bufferedSource = Okio.buffer(source)) {
            int read;
            while ((read = bufferedSource.read(buffer)) != -1) {
                totalBytesRead += read;
                sink.write(buffer, 0, read);

                // Calculate and update the progress
                int progress = (int) (100 * totalBytesRead / fileLength);

                // Calculate elapsed time in seconds
                long elapsedTime = System.nanoTime() - startTime;
                double elapsedSeconds = elapsedTime / 1_000_000_000.0;

                // Calculate upload speed in bytes per second
                double uploadSpeedBytesPerSecond = totalBytesRead / elapsedSeconds;

                // Convert to KB/s or MB/s with one decimal place
                String uploadSpeed;
                if (uploadSpeedBytesPerSecond >= 1024 * 1024) {
                    uploadSpeed = String.format("%.1f MB/s", uploadSpeedBytesPerSecond / (1024 * 1024));
                } else {
                    uploadSpeed = String.format("%.1f KB/s", uploadSpeedBytesPerSecond / 1024);
                }
                long finalTotalBytesRead = totalBytesRead;
                new Handler(Looper.getMainLooper()).post(() ->
                        listener.onProgressUpdate(progress, finalTotalBytesRead, fileLength, uploadSpeed)
                );
            }
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(listener::onError);
            throw e;
        }
        new Handler(Looper.getMainLooper()).post(listener::onFinish);
    }
}
