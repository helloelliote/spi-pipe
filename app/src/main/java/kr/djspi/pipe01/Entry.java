package kr.djspi.pipe01;

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

public class Entry {

    private static final String TAG = "Entry";
    public final String id;
    public final String serial;
    public final String type;

    public Entry(String id, String serial, String type) {
        this.id = id;
        this.serial = serial;
        this.type = type;
    }

    public static List<Entry> initEntryList(Resources resources) {
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
        String jsonProductsString = writer.toString();
        Gson gson = new Gson();
        Type productListType = new TypeToken<ArrayList<Entry>>() {
        }.getType();
        return gson.fromJson(jsonProductsString, productListType);
    }
}
