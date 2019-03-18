//package kr.djspi.pipe01;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView.ViewHolder;
//import android.telephony.PhoneNumberFormattingTextWatcher;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.TextView;
//
//import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
//
//public class PipeCardViewHolder extends ViewHolder implements OnClickListener {
//
//    TextView pipe;
//    ExtendedEditText shape;
//    ExtendedEditText horizontal;
//    ExtendedEditText vertical;
//    ExtendedEditText depth;
//    ExtendedEditText spec;
//    ExtendedEditText material;
//    ExtendedEditText supervise;
//    ExtendedEditText supervise_contact;
//    ExtendedEditText spi_memo;
//    ExtendedEditText construction;
//    ExtendedEditText construction_contact;
//    ExtendedEditText spi_photo;
//
//    // TODO: 2019-02-23 관로특성: 관로 종류에 따라 prefix suffix hint 조정
//
//    PipeCardViewHolder(@NonNull View itemView) {
//        super(itemView);
//        pipe = itemView.findViewById(R.id.pipe);
//        shape = itemView.findViewById(R.id.shape);
//        horizontal = itemView.findViewById(R.id.horizontal);
//        vertical = itemView.findViewById(R.id.vertical);
//        depth = itemView.findViewById(R.id.depth);
//        spec = itemView.findViewById(R.id.spec);
//        material = itemView.findViewById(R.id.material);
//        supervise = itemView.findViewById(R.id.supervise);
//        supervise_contact = itemView.findViewById(R.id.supervise_contact);
//        supervise_contact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
//        spi_memo = itemView.findViewById(R.id.spi_memo);
//        construction = itemView.findViewById(R.id.construction);
//        construction_contact = itemView.findViewById(R.id.construction_contact);
//        supervise_contact.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
//        spi_photo = itemView.findViewById(R.id.spi_photo);
//        spi_photo.setOnClickListener(this);
//
////        title_attr.setSimpleTextChangeWatcher((s, isError) -> {
////            if (isError) {
////                title_attr.setError("글자수를 초과하였습니다", false);
////            }
////        });
////        edit_attr.setSimpleTextChangeWatcher(new SimpleTextChangedWatcher() {
////            @Override
////            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
////
////            }
////
////            @Override
////            public void onTextChanged(CharSequence s, int start, int before, int count) {
////
////            }
////
////            @Override
////            public void afterTextChanged(Editable s) {
//////                if (s.length() > title_attr.getMaxCharacters()) {
//////                    title_attr.setError("글자수를 초과하였습니다", false);
//////                } else {
//////                    title_attr.setError(null, false);
//////                }
////            }
////        });
//    }
//
//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.spi_photo:
//                break;
//            default:
//                break;
//        }
//    }
//}
