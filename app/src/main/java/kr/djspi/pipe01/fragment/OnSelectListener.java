package kr.djspi.pipe01.fragment;

import androidx.annotation.Nullable;

public interface OnSelectListener {
    void onSelect(String tag, int index, @Nullable String... text);
}
