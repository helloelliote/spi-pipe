package kr.djspi.pipe01.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import kr.djspi.pipe01.MainActivity;
import kr.djspi.pipe01.R;

import static android.content.Intent.CATEGORY_DEFAULT;
import static android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS;
import static android.provider.Settings.ACTION_NFC_SETTINGS;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.util.Objects.requireNonNull;

public class MessageDialog extends DialogFragment implements OnClickListener {

    private static boolean isReturnToMain = false;
    private static int issueType = 0;
    private TextView popupTitle;
    private TextView popupText;
    private TextView popupSubText;
    private TextView buttonDismiss;
    private TextView buttonOk;

    public MessageDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isReturnToMain = false;
        if (getArguments() != null) {
            issueType = getArguments().getInt("issueType");
        }
    }

    /**
     * 레이아웃 구성 및 기능 초기화. 상황에 따라 팝업 메시지 전환
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup, container, false);

        popupTitle = view.findViewById(R.id.popup_title);
        popupTitle.setText("알림");
        popupText = view.findViewById(R.id.popup_contents);
        popupSubText = view.findViewById(R.id.popup_contents_sub);
        buttonDismiss = view.findViewById(R.id.btn_dismiss);
        buttonDismiss.setOnClickListener(this); // 취소버튼은 기본적으로 비표시 (invisible) 상태
        buttonOk = view.findViewById(R.id.btn_ok);
        buttonOk.setOnClickListener(v -> dismiss());
        view.findViewById(R.id.btn_close).setOnClickListener(this);

        setPopup(issueType, view);

        return view;
    }

    /**
     * 상황에 따라 알맞는 팝업 메시지를 설정
     *
     * @param issueType 팝업을 띄우는 클래스에 전달하는 상황
     * @see MessageDialog#setVisibilityToGone(View) 보충 설명(Sub) 을 표시하지 않는다.
     * @see #getTag 팝업을 띄우는 클래스에서 전달하는 메시지를 태그로 받아온다.
     */
    private void setPopup(final int issueType, @NonNull final View view) {
        // 팝업창 제목과 내용, 확인 버튼에 Default 값을 먼저 설정
        popupTitle.setText("알림");
        popupText.setText(getTag());
        // 전달된 상황에 맞게 팝업창 커스터마이즈
        switch (issueType) {
            case 0: // 일반 메시지 전달
                setVisibilityToGone(view);
                break;
            case 1: // (MainActivity.class) 위치 기능이 꺼져 있음
                popupTitle.setText("주의");
                popupSubText.setText(fromHtml(getString(R.string.popup_location_on_sub)));
                // 확인버튼 터치 시 위치 설정으로 이동
                buttonOk.setOnClickListener(v -> {
                    startActivity(new Intent(ACTION_LOCATION_SOURCE_SETTINGS).addCategory(CATEGORY_DEFAULT));
                    dismiss();
                });
                break;
            case 2: // (BaseActivity.class) NFC 기능이 꺼져 있음
                popupTitle.setText("주의");
                popupSubText.setText(fromHtml(getString(R.string.popup_nfc_on_sub)));
                // 확인버튼 터치 시 NFC 설정으로 이동
                buttonOk.setOnClickListener(v -> {
                    startActivity(new Intent(ACTION_NFC_SETTINGS).addCategory(CATEGORY_DEFAULT));
                    dismiss();
                });
                break;
            case 3: // (MainActivity.class) 정품 SPI 가 아닌 태그가 태깅되었음
                setVisibilityToGone(view);
                popupTitle.setText("주의");
                // TODO: 2019-01-31 전달: showMessagePopup(3, getString(R.string.popup_not_spi));
                buttonOk.setOnClickListener(v -> {
                    dismiss();
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(1); // 앱 완전종료
                });
                break;
            case 4: // (NfcRecordWrite.class) 쓰기 작업 이후 수정이 불가함을 안내
                setVisibilityToGone(view);
                popupTitle.setText("주의");
                buttonDismiss.setVisibility(VISIBLE);
                buttonDismiss.setText(getString(R.string.popup_pre));
                break;
            case 5: // (NfcRecordWrite.class) 정보가 정상적으로 기록됨
                setVisibilityToGone(view);
                isReturnToMain = true;
                // FIXME: 2019-01-31 NfcRecordWrite.class 에서 창을 먼저 띄우고, 확인을 누르면 MainActivity 으로 가게 수정
                // TODO: 2019-03-05 buttonOk.setOnClickListener(v -> dismiss());
                break;
            default:
                break;
        }
    }

    private static void setVisibilityToGone(@NonNull final View view) {
        view.findViewById(R.id.popup_contents_sub).setVisibility(GONE);
        view.findViewById(R.id.icon_info_s).setVisibility(GONE);
        view.findViewById(R.id.view_border).setVisibility(GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_dismiss:
                requireNonNull(getActivity()).onBackPressed();
                break;
            case R.id.btn_close:
                isReturnToMain = true;
                dismiss();
            default:
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        if (isReturnToMain) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (isAdded()) startActivity(intent);
            else return;
        }
        super.onDismiss(dialogInterface);
    }
}
