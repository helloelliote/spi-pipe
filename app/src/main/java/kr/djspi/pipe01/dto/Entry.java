package kr.djspi.pipe01.dto;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.Data;

/**
 * (중요) DTO 클래스는 Proguard 난독화 대상에서 제외시켜야 Json 파싱 과정에서 각종 변수명들이 유지된다.
 * proguard-rules.pro: -keep class kr.djspi.pipe01.dto.** { *; }
 */
@Data
public class Entry implements Serializable {
    public final Spi spi;
    public final SpiType spi_type;
    public final SpiMemo spi_memo;
    public final SpiPhoto spi_photo;
    public final Pipe pipe;
    public final PipeType pipe_type;
    public final PipeShape pipe_shape;
    public final PipePosition pipe_position;
    public final PipePlan pipe_plan;
    public final PipeSupervise pipe_supervise;
    public SpiLocation spi_location;

    public Entry(Spi spi, SpiType spi_type, SpiMemo spi_memo, SpiPhoto spi_photo, Pipe pipe, PipeType pipe_type, PipeShape pipe_shape, PipePosition pipe_position, PipePlan pipe_plan, PipeSupervise pipe_supervise) {
        this.spi = spi;
        this.spi_type = spi_type;
        this.spi_memo = spi_memo;
        this.spi_photo = spi_photo;
        this.pipe = pipe;
        this.pipe_type = pipe_type;
        this.pipe_shape = pipe_shape;
        this.pipe_position = pipe_position;
        this.pipe_plan = pipe_plan;
        this.pipe_supervise = pipe_supervise;
    }

    @SuppressWarnings("SameParameterValue")
    public static JsonObject parseEntry(@NonNull ArrayList entries, int index, String... strings) {
        return ((Entry) entries.get(index)).parseToSingleJsonObject(strings);
    }

    private JsonObject parseToSingleJsonObject(@NonNull String[] strings) {
        JsonObject object = new JsonObject();
        object.addProperty("spi_id", spi.getId());
        object.addProperty("supervise", pipe_supervise.getSupervise());
        object.addProperty("supervise_contact", pipe.getSupervise_contact());
        object.addProperty("construction", pipe.getConstruction());
        object.addProperty("construction_contact", pipe.getConstruction_contact());
        object.addProperty("spi_type", spi_type.getType());
        object.addProperty("pipe", pipe_type.getPipe());
        object.addProperty("shape", pipe_shape.getShape());
        object.addProperty("header", pipe_type.getHeader());
        object.addProperty("spec", pipe_shape.getSpec());
        object.addProperty("unit", pipe_type.getUnit());
        object.addProperty("material", pipe.getMaterial());
        object.addProperty("position", pipe_position.getPosition());
        object.addProperty("direction", pipe_position.getDirection());
        object.addProperty("horizontal", pipe_position.getHorizontal());
        object.addProperty("vertical", pipe_position.getVertical());
        object.addProperty("horizontal_form", strings[0]);
        object.addProperty("vertical_form", strings[1]);
        object.addProperty("depth", pipe.getDepth());
        object.addProperty("spi_memo", spi_memo.getMemo());
        object.addProperty("file_plane", pipe_plan.getFile_plane());
        object.addProperty("file_section", pipe_plan.getFile_section());
        object.addProperty("latitude", spi_location.getLatitude());
        object.addProperty("longitude", spi_location.getLongitude());
        return object;
    }
}
