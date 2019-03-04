package kr.djspi.pipe01.dto;

import android.content.res.Resources;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.djspi.pipe01.R;

public class PipeEntry {

    private static final String TAG = PipeEntry.class.getSimpleName();
//    private static JsonObject jsonObject;

    public final String spi_type;
    public final String pipe;
    public final String shape;
    public final String header;
    public final String unit;

    public PipeEntry(String spi_type, String pipe, String shape, String header, String unit) {
//        PipeEntry.jsonObject = jsonObject;
        this.spi_type = spi_type;
        this.pipe = pipe;
        this.shape = shape;
        this.header = header;
        this.unit = unit;
    }

    public static List<PipeEntry> initParserList(Resources resources) {
        InputStream inputStream = resources.openRawResource(R.raw.pipes);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            int pointer;
            while ((pointer = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, pointer);
            }
        } catch (IOException exception) {
            Log.e(TAG, "Error writing/reading from the JSON file.", exception);
        } finally {
            try {
                inputStream.close();
            } catch (IOException exception) {
                Log.e(TAG, "Error closing the input stream.", exception);
            }
        }
        Gson gson = new Gson();
        String jsonString = writer.toString();
//        String jsonString = jsonObject.getInstance("data_new").getAsString();
        Type pipeListType = new TypeToken<ArrayList<PipeEntry>>() {
        }.getType();
        return gson.fromJson(jsonString, pipeListType);
    }
}
