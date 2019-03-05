package kr.djspi.pipe01;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 숫자 입력 필터 설정 클래스
 */
@SuppressWarnings("ALL")
public class DecimalFilter implements InputFilter {

    private final Pattern mPattern;

    public DecimalFilter(int beforeZero, int afterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (beforeZero - 1) + "}+((\\.[0-9]{0," + (afterZero - 1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        Matcher matcher = mPattern.matcher(dest);
        if (!matcher.matches()) {
            return "";
        }
        return null;
    }
}
