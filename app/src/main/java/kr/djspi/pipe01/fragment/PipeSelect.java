package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.PipeType;

public class PipeSelect extends DialogFragment implements OnClickListener {

    private static final String TAG = PipeSelect.class.getSimpleName();
    private static ArrayList<String> listItem;
    private OnSelectListener listener;
    /**
     * 아래의 변수들은 내부 클래스에서도 참조하는 변수로, private 선언하지 않는다.
     */
    static int selectIndex = -1;

    public PipeSelect() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectListener) {
            listener = (OnSelectListener) context;
            Log.w(TAG, "OK");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PipeType[] pipes = PipeType.values();
        listItem = new ArrayList<>();
        for (PipeType pipe : pipes) {
            listItem.add(getString(pipe.getNameRes()));
        }
    }

    /**
     * 레이아웃 구성 및 기능 초기화
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pipeselect, container, false);

        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        ListView listView = view.findViewById(R.id.list_common);
        listView.setAdapter(new ListAdapter(getContext(), listItem));
        listView.setOnItemClickListener(new OnItemClickListen());
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                listener.onPipeSelect(selectIndex);
                dismiss();
                break;
            case R.id.btn_cancel:
            case R.id.btn_close:
                dismiss();
                dismiss();
            default:
                break;
        }
    }

    private class ListAdapter extends BaseAdapter {

        private Context context;
        private ArrayList<String> listItem;

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
                convertView = LayoutInflater.from(context).inflate(R.layout.fragment_pipeselect_item, null);
            }
            TextView textView = convertView.findViewById(R.id.txt_name);
            textView.setText(listItem.get(position));
            textView.setOnFocusChangeListener(new OnFocusChangeListen());
            return convertView;
        }
    }

    private class OnItemClickListen implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectIndex = i;
        }
    }

    private class OnFocusChangeListen implements OnFocusChangeListener {

        @Override
        public void onFocusChange(View view, boolean b) {

        }
    }
}
