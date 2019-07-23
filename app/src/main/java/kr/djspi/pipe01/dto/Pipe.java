package kr.djspi.pipe01.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class Pipe implements DataItem, Serializable {

    public int id = -1;
    public int type_id;
    public int spi_id;
    public double depth;
    public String material;
    public int supervise_id;
    public String supervise_contact;
    public String construction;
    public String construction_contact;
}
