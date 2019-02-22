package kr.djspi.pipe01;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class PipeCardViewHolder extends ViewHolder {

    public TextFieldBoxes title_attr;
    public ExtendedEditText edit_attr;

    // TODO: 2019-02-23 관로특성: 관로 종류에 따라 prefix suffix hint 조정

    public PipeCardViewHolder(@NonNull View itemView) {
        super(itemView);
//        title_attr = itemView.findViewById(R.id.title_attr);
//        edit_attr = itemView.findViewById(R.id.edit_attr);
//        title_attr.setSimpleTextChangeWatcher((s, isError) -> {
//            if (isError) {
//                title_attr.setError("글자수를 초과하였습니다", false);
//            }
//        });
//        edit_attr.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
////                if (s.length() > title_attr.getMaxCharacters()) {
////                    title_attr.setError("글자수를 초과하였습니다", false);
////                } else {
////                    title_attr.setError(null, false);
////                }
//            }
//        });
    }
}
