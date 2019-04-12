package com.helloelliote.util.retrofit;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public final class ProgressBody extends RequestBody {

    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private File file;
    private String contentType;
    private UploadCallback callback;

    public ProgressBody(final File file, String contentType, final UploadCallback callback) {
        this.file = file;
        this.contentType = contentType;
        this.callback = callback;
    }

    @Override
    public long contentLength() throws IOException {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType + "/*");
    }

    @Override
    public void writeTo(@NotNull BufferedSink sink) throws IOException {
        final long totalSize = file.length();
        final byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            long uploadSize = 0L;
            int readSize;
            int number = 0;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((readSize = inputStream.read(buffer)) != -1) {
                int progress = (int) (100 * uploadSize / totalSize);
                if (progress > number + 1) {
                    // update progress on UI thread
                    handler.post(new ProgressUpdater(uploadSize, totalSize));
                    number = progress;
                }
                uploadSize += readSize;
                sink.write(buffer, 0, readSize);
            }
        }
    }

    public interface UploadCallback {
        void onInitiate(int percentage);

        void onProgress(int percentage);

        void onError();

        void onFinish(int percentage);
    }

    private class ProgressUpdater implements Runnable {
        private long uploadSize;
        private long totalSize;

        ProgressUpdater(long uploadSize, long totalSize) {
            this.uploadSize = uploadSize;
            this.totalSize = totalSize;
        }

        @Override
        public void run() {
            callback.onProgress((int) (100 * uploadSize / totalSize));
        }
    }
}
