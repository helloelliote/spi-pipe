package kr.djspi.pipe01.fragment;

import android.support.annotation.Nullable;

public interface OnSelectListener {
    void onSelect(String tag, int index, @Nullable String ... text);
}
