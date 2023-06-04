package info.kgeorgiy.ja.treshchev.i18n.stats;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class WordStat {

    private int count;
    private int countDifferent;
    private String min;
    private String max;
    private String minLength;
    private String maxLength;
    private double averageLength;

    public WordStat(final String text, final Locale textLocale) {
        this.count = 0;
        this.countDifferent = 0;
        this.averageLength = 0;
        final Collator cmp = Collator.getInstance(textLocale);
        final Set<String> words = new HashSet<>();
        final BreakIterator breakIterator = BreakIterator.getWordInstance(textLocale);
        breakIterator.setText(text);
        int start = breakIterator.first();
        for (int i = breakIterator.next(); i != BreakIterator.DONE; start = i, i = breakIterator.next()) {
            String word = text.substring(start, i).trim();
            if (word.isEmpty()) {
                continue;
            }
            if (this.count == 0) {
                this.min = word;
                this.max = word;
                this.minLength = word;
                this.maxLength = word;
            }
            this.count++;
            if (!words.contains(word)) {
                words.add(word);
                this.countDifferent++;
            }
            if (cmp.compare(this.min, word) > 0) {
                this.min = word;
            }
            if (cmp.compare(this.max, word) < 0) {
                this.max = word;
            }
            if (this.maxLength.length() < word.length()) {
                this.maxLength = word;
            }
            if (this.minLength.length() > word.length()) {
                this.minLength = word;
            }
            this.averageLength += word.length();
        }
        this.averageLength /= this.count;
    }

    public int getCount() {
        return count;
    }

    public int getCountDifferent() {
        return countDifferent;
    }

    public String getMin() {
        return min;
    }

    public String getMax() {
        return max;
    }

    public String getMinLength() {
        return minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public double getAverage() {
        return averageLength;
    }
}
