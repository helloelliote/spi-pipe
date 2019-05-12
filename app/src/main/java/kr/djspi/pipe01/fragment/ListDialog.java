package kr.djspi.pipe01.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.room.Room;

import com.sylversky.indexablelistview.scroller.Indexer;
import com.sylversky.indexablelistview.widget.IndexableListView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.djspi.pipe01.R;
import kr.djspi.pipe01.dto.PipeType.PipeTypeEnum;
import kr.djspi.pipe01.sql.Supervise;
import kr.djspi.pipe01.sql.SuperviseDatabase;

import static android.view.View.TEXT_ALIGNMENT_CENTER;
import static kr.djspi.pipe01.Const.PIPE_SHAPES;
import static kr.djspi.pipe01.Const.PIPE_TYPE_ENUMS;
import static kr.djspi.pipe01.Const.TAG_PIPE;
import static kr.djspi.pipe01.Const.TAG_SHAPE;
import static kr.djspi.pipe01.Const.TAG_SUPERVISE;

/**
 * 관로종류 목록과 관리기관 목록을 보여주는데 공용으로 사용하는 Dialog 클래스
 */
public class ListDialog extends DialogFragment implements OnClickListener {

    private static final String TAG = ListDialog.class.getSimpleName();
    private int selectIndex = -1;
    private String listTag;
    private String dialogTitle;
    private ArrayList<String> listItem;
    private IndexableListView listView;
    private String componentName;
    private Parcelable state;
    private OnSelectListener listener;
    private SuperviseDatabase superviseDb;

    public ListDialog() {
    }

    @Override
    public void onAttach(@NotNull Context context) {
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
                for (PipeTypeEnum pipe : PIPE_TYPE_ENUMS) {
                    listItem.add(pipe.getName());
                }
                dialogTitle = getString(R.string.popup_title_select_pipe);
                break;
            case TAG_SHAPE:
                listItem.addAll(Arrays.asList(PIPE_SHAPES));
                dialogTitle = getString(R.string.popup_title_select_shape);
                break;
            case TAG_SUPERVISE:
                new Thread(() -> {
                    if (superviseDb == null) {
                        superviseDb = Room.databaseBuilder(getContext(), SuperviseDatabase.class, "db_supervise").build();
                    }
                    List<Supervise> dbList = superviseDb.dao().getAll();
                    for (Supervise db : dbList) {
                        listItem.add(db.getSupervise());
                    }
                    superviseDb.close();
                }).start();
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

        listView = view.findViewById(R.id.list_common);
//        listView.setIndexTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/nanumsquareroundr.ttf"));
        if (listTag.equals(TAG_SUPERVISE)) {
            listView.setAdapter(new ListAdapter(getContext(), listItem, true));
        } else {
            listView.setAdapter(new ListAdapter(getContext(), listItem, false));
            listView.setFastScrollEnabled(false);
        }
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            componentName = listItem.get(position);
            selectIndex = position;
        });
        if (state != null) {
            listView.requestFocus();
            listView.onRestoreInstanceState(state);
        }
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                if (selectIndex == -1) {
                    Toast.makeText(getContext(), "항목을 선택해주세요", Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onSelect(listTag, selectIndex, componentName);
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
    public void onPause() {
        state = listView.onSaveInstanceState();
        super.onPause();
    }

    @Override
    public void onDismiss(@NotNull DialogInterface dialog) {
        selectIndex = -1;
        super.onDismiss(dialog);
    }

    /**
     * @see <a href="IndexableListView"></a>https://github.com/sylversky/IndexableListView.git</a>
     */
    private final class ListAdapter extends BaseAdapter implements Indexer {

        private final Context context;
        private final ArrayList<String> listItem;
        private final boolean isListSupervise;
        private final CustomSection customSection;

        ListAdapter(Context context, ArrayList<String> listItem, boolean isListSupervise) {
            this.context = context;
            this.listItem = listItem;
            this.isListSupervise = isListSupervise;
            this.customSection = isListSupervise ? new CustomSection(this) : null;
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
            textView.setTextAlignment(isListSupervise ? View.TEXT_ALIGNMENT_TEXT_START : TEXT_ALIGNMENT_CENTER);
            textView.setOnFocusChangeListener((v, hasFocus) -> {
            });
            return convertView;
        }

        @Override
        public String getComponentName(int position) {
            return listItem.get(position);
        }

        @Override
        @Nullable
        public Object[] getSections() {
            if (isListSupervise) return customSection.getArraySections();
            else return null;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            return isListSupervise ? customSection.getPositionForSection(sectionIndex, getCount()) : 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }
    }
}
