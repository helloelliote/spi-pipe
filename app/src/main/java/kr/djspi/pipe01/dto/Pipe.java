package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Pipe implements DataItem, Serializable {

    private int id = -1;
    private int type_id;
    private int spi_id;
    private double depth;
    private String material;
    private int supervise_id;
    private String supervise_contact;
    private String construction;
    private String construction_contact;
}
