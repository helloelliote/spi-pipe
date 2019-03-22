package kr.djspi.pipe01.tab;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.Contract;

import kr.djspi.pipe01.R;

import static android.view.View.VISIBLE;
import static kr.djspi.pipe01.BaseActivity.packageName;

public class SectionTab extends Fragment {

    private static final String TAG = SectionTab.class.getSimpleName();
    private static JsonObject jsonObject;

    public SectionTab() {
    }

    private static class LazyHolder {
        static final SectionTab INSTANCE = new SectionTab();
    }

    @Contract(pure = true)
    public static SectionTab getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRecordListener) {
            OnRecordListener listener = (OnRecordListener) context;
            jsonObject = listener.getJsonObjectRecord();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_section, container, false);
        try {
            final String resId = jsonObject.get("file_section").getAsString().replace(".png", "");
            ImageView imageView = view.findViewById(R.id.planeImageView);
            imageView.setImageResource(getResources().getIdentifier(resId, "drawable", packageName));
        } catch (UnsupportedOperationException e) {
            LinearLayout lay_empty = view.findViewById(R.id.lay_empty);
            lay_empty.setVisibility(VISIBLE);
        }
        return view;
    }
}
