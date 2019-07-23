package kr.djspi.pipe01.dto;

import android.net.Uri;

import java.io.File;
import java.io.Serializable;

public class SpiPhotoObject implements DataItem, Serializable {

    public String uri;
    public String url;
    public File file;

    public Uri getUri() {
        if (uri == null) return null;
        else return Uri.parse(uri);
    }

    public void setUri(Uri uri) {
        if (uri == null) this.uri = null;
        else this.uri = uri.toString();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
