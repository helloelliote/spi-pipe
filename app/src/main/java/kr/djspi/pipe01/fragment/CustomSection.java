package kr.djspi.pipe01.fragment;

import com.sylversky.indexablelistview.scroller.Indexer;
import com.sylversky.indexablelistview.scroller.StringMatcher;
import com.sylversky.indexablelistview.section.Section;

public class CustomSection extends Section {

    private String sections = "#ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ";

    CustomSection(Indexer indexer) {
        super(indexer);
    }

    @Override
    public Object[] getArraySections() {
        final int len = sections.length();
        String[] arraySections = new String[len];
        for (int i = 0; i < len; i++) {
            arraySections[i] = String.valueOf(sections.charAt(i));
        }
        return arraySections;
    }

    @Override
    public int getPositionForSection(int section, int totalComponent) {
        for (int j = 0; j < totalComponent; j++) {
            if (section == 0) {
                // For numeric section
                for (int k = 0; k <= 9; k++) {
                    String value = getIndexer().getComponentName(j).toUpperCase();
                    if (StringMatcher.match(String.valueOf(value.charAt(0)), String.valueOf(k)))
                        return j;
                }
            } else {
                String value = getIndexer().getComponentName(j).toUpperCase();
                if (StringMatcher.match(String.valueOf(value.charAt(0)), String.valueOf(sections.charAt(section))))
                    return j;
            }
        }
        return -1;
    }
}
