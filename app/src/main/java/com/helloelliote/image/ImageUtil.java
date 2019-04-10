package com.helloelliote.image;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_DCIM;
import static android.provider.MediaStore.MediaColumns.DATA;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static java.util.Objects.requireNonNull;

public class ImageUtil {

    private static final String[] PROJECTION_DATA = {DATA};
    private static final String[] PROJECTION_NAME = {DISPLAY_NAME};

    private static String uriToFilePath(@NotNull Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, PROJECTION_DATA, null, null, null);
        requireNonNull(cursor).moveToNext();
        String path = cursor.getString(cursor.getColumnIndex(DATA));
        cursor.close();
        return path;
    }

    @Nullable
    public static String uriToFileName(@NotNull Context context, @NotNull Uri uri) {
        if (!requireNonNull(uri.getScheme()).equals("file"))
            try (Cursor cursor = context.getContentResolver().query(uri, PROJECTION_NAME, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    return cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                }
            }
        else return uri.getLastPathSegment();
        return null;
    }

    @NotNull
    @Contract("_, _ -> new")
    public static File uriToFile(Context context, Uri uri) {
        return new File(uriToFilePath(context, uri));
    }

    public static File prepareFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = String.format("IMG_%s_", timeStamp);
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DCIM), "Camera");
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }
}
