package info.kgeorgiy.ja.treshchev.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_en extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            {"analyzedFile", "Analyzed file: "},
            {"summaryStat", "Summary statistics"},
            {"sentenceStat", "Sentences statistics"},
            {"wordStat", "Words statistics"},
            {"numStat", "Numbers statistics"},
            {"curStat", "Currencies statistics"},
            {"dateStat", "Dates statistics"},
            {"number", "Number of"},
            {"numberUnique", "unique"},
            {"min", "Minimal"},
            {"minA", "Minimal"},
            {"max", "Maximal"},
            {"maxA", "Maximal"},
            {"minLength", "Minimal length of"},
            {"maxLength", "Maximal length of"},
            {"averageLength", "Average length of"},
            {"average", "Average of"},
            {"averageA", "Average of"},
            {"sentences", "sentences"},
            {"sentence", "sentence"},
            {"sentenceA", "sentence"},
            {"words", "words"},
            {"word", "word"},
            {"wordA", "word"},
            {"number", "number"},
            {"numbers", "numbers"},
            {"currencies", "currencies"},
            {"currency", "currency"},
            {"dates", "dates"},
            {"date", "date"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
