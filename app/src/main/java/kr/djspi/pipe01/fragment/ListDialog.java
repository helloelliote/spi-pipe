package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.RecordInputActivity;
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;

import static kr.djspi.pipe01.Const.PIPE_SHAPES;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;
import static kr.djspi.pipe01.RecordInputActivity.pipes;

/**
 * 관로종류 목록과 관리기관 목록을 보여주는데 공용으로 사용하는 Dialog 클래스
 */
public class ListDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = ListDialog.class.getSimpleName();
    private static ListDialog listDialog = null;
    private static String listTag;
    private static String dialogTitle;
    private static int selectIndex = -1;
    private static ArrayList<String> listItem;
    private static OnSelectListener listener;

    public ListDialog() {
    }

    public synchronized static ListDialog get() {
        if (listDialog == null) {
            listDialog = new ListDialog();
        }
        return listDialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listTag = getTag();
        listItem = new ArrayList<>();
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        switch (listTag) {
            case TAG_PIPE:
                for (PipeTypeEnum pipe : pipes) {
                    listItem.add(pipe.getName());
                }
                dialogTitle = getString(R.string.popup_title_select_pipe);
                break;
            case TAG_SHAPE:
                listItem.addAll(Arrays.asList(PIPE_SHAPES));
                dialogTitle = getString(R.string.popup_title_select_shape);
                break;
            case TAG_SUPERVISE:
                listItem = RecordInputActivity.superviseList;
                dialogTitle = getString(R.string.popup_title_select_supervise);
                break;
            default:
                break;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);

        TextView titleView = view.findViewById(R.id.popup_title);
        titleView.setText(dialogTitle);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        ListView listView = view.findViewById(R.id.list_common);
        listView.setAdapter(new ListAdapter(getContext(), listItem));
        listView.setOnItemClickListener((parent, view1, position, id) -> selectIndex = position);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                listener.onSelect(listTag, selectIndex);
                dismissAllowingStateLoss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
                dismissAllowingStateLoss();
            default:
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        selectIndex = -1;
        super.onDismiss(dialog);
    }

    private class ListAdapter extends BaseAdapter {

        private final Context context;
        private final ArrayList<String> listItem;

        ListAdapter(Context context, ArrayList<String> listItem) {
            this.context = context;
            this.listItem = listItem;
        }

        @Override
        public int getCount() {
            return listItem.size();
        }

        @Override
        public Object getItem(int position) {
            return listItem.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.fragment_list_item, null);
            }
            TextView textView = convertView.findViewById(R.id.txt_name);
            textView.setText(listItem.get(position));
            textView.setOnFocusChangeListener((v, hasFocus) -> {
            });
            return convertView;
        }
    }
}
