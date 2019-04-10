package kr.djspi.pipe01.dto;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

public class SpiPhotoObject implements DataItem, Serializable {

    private String uri;
    private File file;

    public Uri getUri() {
        return Uri.parse(uri);
    }

    public void setUri(Uri uri) {
        if (uri == null) this.uri = null;
        else this.uri = uri.toString();
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
