package info.kgeorgiy.ja.treshchev.i18n.stats;

import java.text.BreakIterator;
import java.text.Collator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class SentenceStat {

    private int count;
    private int countDifferent;
    private String min;
    private String max;
    private String minLength;
    private String maxLength;
    private double averageLength;


    public SentenceStat(final String text, final Locale textLocale) {
        this.count = 0;
        this.countDifferent = 0;
        this.averageLength = 0;
        Collator cmp = Collator.getInstance(textLocale);
        Set<String> sentences = new HashSet<>();
        final BreakIterator breakIterator = BreakIterator.getSentenceInstance(textLocale);
        breakIterator.setText(text);
        int start = breakIterator.first();
        for (int i = breakIterator.next(); i != BreakIterator.DONE; start = i, i = breakIterator.next()) {
            String sentence = text.substring(start, i).trim();
            if (sentence.isEmpty()) {
                continue;
            }
            if (this.count == 0) {
                this.min = sentence;
                this.max = sentence;
                this.minLength = sentence;
                this.maxLength = sentence;
            }
            this.count++;
            if (!sentences.contains(sentence)) {
                sentences.add(sentence);
                this.countDifferent++;
            }
            if (cmp.compare(this.min, sentence) > 0) {
                this.min = sentence;
            }
            if (cmp.compare(this.max, sentence) < 0) {
                this.max = sentence;
            }
            if (this.maxLength.length() < sentence.length()) {
                this.maxLength = sentence;
            }
            if (this.minLength.length() > sentence.length()) {
                this.minLength = sentence;
            }
            this.averageLength += sentence.length();
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
