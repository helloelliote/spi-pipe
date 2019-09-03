package kr.djspi.pipe01.fragment

import com.sylversky.indexablelistview.scroller.Indexer
import com.sylversky.indexablelistview.scroller.StringMatcher.match
import com.sylversky.indexablelistview.section.Section

class CustomSection(indexer: Indexer) : Section(indexer) {

    override fun getArraySections(): Array<String?> {
        val len = sections.length
        val arraySections = arrayOfNulls<String>(len)
        for (i in 0 until len) {
            arraySections[i] = sections[i].toString()
        }
        return arraySections
    }

    override fun getPositionForSection(section: Int, totalComponent: Int): Int {
        for (i in 0 until totalComponent) {
            when (section) {
                0 -> { // For numeric section
                    for (j in 0..9) {
                        val value = indexer.getComponentName(j).toUpperCase()
                        if (match(value[0].toString(), j.toString())) {
                            return j
                        }
                    }
                }
                else -> {
                    val value = indexer.getComponentName(i).toUpperCase()
                    if (match(value[0].toString(), sections[section].toString())) {
                        return i
                    }
                }
            }
        }
        return -1
    }

    companion object {
        private const val sections: String = "#ㄱㄴㄷㄹㅁㅂㅅㅇㅈㅊㅋㅌㅍㅎ"
    }
}
