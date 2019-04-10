package kr.djspi.pipe01.dto;


import java.io.File;
import java.io.Serializable;
import java.net.URI;

import lombok.Data;

@Data
public class SpiPhotoObject implements DataItem, Serializable {

    private URI uri;
    private File file;
}
