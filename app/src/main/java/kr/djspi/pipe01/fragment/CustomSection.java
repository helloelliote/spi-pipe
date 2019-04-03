package kr.djspi.pipe01.fragment;

import com.sylversky.indexablelistview.scroller.Indexer;
import com.sylversky.indexablelistview.section.Section;

import static com.sylversky.indexablelistview.scroller.StringMatcher.match;
import static java.lang.String.valueOf;

class CustomSection extends Section {

    private final String sections = "#ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ";

    CustomSection(Indexer indexer) {
        super(indexer);
    }

    @Override
    public Object[] getArraySections() {
        final int len = sections.length();
        String[] arraySections = new String[len];
        for (int i = 0; i < len; i++) {
            arraySections[i] = valueOf(sections.charAt(i));
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
                    if (match(valueOf(value.charAt(0)), valueOf(k)))
                        return j;
                }
            } else {
                String value = getIndexer().getComponentName(j).toUpperCase();
                if (match(valueOf(value.charAt(0)), valueOf(sections.charAt(section))))
                    return j;
            }
        }
        return -1;
    }
}
