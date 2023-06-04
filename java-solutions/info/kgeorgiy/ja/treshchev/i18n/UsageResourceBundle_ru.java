package info.kgeorgiy.ja.treshchev.i18n;

import java.util.ListResourceBundle;

public class UsageResourceBundle_ru extends ListResourceBundle {

    private static final Object[][] CONTENTS = {
            {"analyzedFile", "Анализируемый файл: "},
            {"summaryStat", "Сводная статистика"},
            {"sentenceStat", "Статистика по предложениям"},
            {"wordStat", "Статистика по словам"},
            {"numStat", "Статистика по числам"},
            {"curStat", "Статистика по суммам денег"},
            {"dateStat", "Статистика по датам"},
            {"number", "Число"},
            {"numberUnique", "различных"},
            {"min", "Минимальное"},
            {"minA", "Минимальная"},
            {"max", "Максимальное"},
            {"maxA", "Максимальная"},
            {"minLength", "Минимальная длина"},
            {"maxLength", "Максимальная длина"},
            {"averageLength", "Средняя длина"},
            {"average", "Среднее"},
            {"averageA", "Средняя"},
            {"sentences", "предложений"},
            {"sentence", "предложение"},
            {"sentenceA", "предложения"},
            {"words", "слов"},
            {"word", "слово"},
            {"wordA", "слова"},
            {"number", "число"},
            {"numbers", "чисел"},
            {"currencies", "сумм"},
            {"currency", "сумма"},
            {"dates", "дат"},
            {"date", "дата"}
    };

    @Override
    protected Object[][] getContents() {
        return CONTENTS;
    }
}
