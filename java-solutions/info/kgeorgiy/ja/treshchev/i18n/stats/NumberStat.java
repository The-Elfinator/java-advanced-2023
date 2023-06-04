package info.kgeorgiy.ja.treshchev.i18n.stats;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class NumberStat {

    private int count;
    private int countDifferent;
    private double min;
    private double max;
    private double average;

    public NumberStat(final String text, final Locale textLocale) {
        final NumberFormat numberFormat = NumberFormat.getNumberInstance(textLocale);
        final BreakIterator breakIterator = BreakIterator.getWordInstance(textLocale);
        breakIterator.setText(text);
        this.count = 0;
        this.countDifferent = 0;
        this.average = 0;
        final Set<Double> numbers = new HashSet<>();
        int start = breakIterator.first();
        for (int i = breakIterator.next(); i != BreakIterator.DONE; start = i, i = breakIterator.next()) {
            String possibleNumber = text.substring(start, i);
            try {
                final Number number = numberFormat.parse(possibleNumber);
                final double doubleValue = number.doubleValue();
                if (this.count == 0) {
                    this.min = doubleValue;
                    this.max = doubleValue;
                }
                this.count++;
                if (!numbers.contains(doubleValue)) {
                    this.countDifferent++;
                    numbers.add(doubleValue);
                }
                if (doubleValue < this.min) {
                    this.min = doubleValue;
                }
                if (doubleValue > this.max) {
                    this.max = doubleValue;
                }
                this.average += doubleValue;
            } catch (ParseException ignored) {
                // do nothing
            }
        }
        this.average /= this.count;
    }

    public int getCount() {
        return count;
    }

    public int getCountDifferent() {
        return countDifferent;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getAverage() {
        return average;
    }
}
