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
    private final Spi spi;
    private final SpiType spi_type;
    private final SpiMemo spi_memo;
    private final SpiPhoto spi_photo;
    private final Pipe pipe;
    private final PipeType pipe_type;
    private final PipeShape pipe_shape;
    private final PipePosition pipe_position;
    private final PipePlan pipe_plan;
    private final PipeSupervise pipe_supervise;
    private SpiLocation spi_location;

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
