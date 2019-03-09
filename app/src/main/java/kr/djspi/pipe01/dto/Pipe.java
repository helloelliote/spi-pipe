package kr.djspi.pipe01.dto;

import lombok.Data;

@Data
public class Pipe implements DataItem {

    private int id = -1;
    private int type_id;
    private int spi_id;
    private double depth;
    private int spec;
    private String material;
    private int supervise_id;
    private String construction;
    private String constructionContact;
}
