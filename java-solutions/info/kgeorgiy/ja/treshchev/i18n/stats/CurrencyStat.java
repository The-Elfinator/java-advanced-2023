package info.kgeorgiy.ja.treshchev.i18n.stats;

import java.text.BreakIterator;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CurrencyStat {

    private int count;
    private int countDifferent;
    private double min;
    private double max;
    private double average;

    public CurrencyStat(final String text, final Locale textLocale) {
        final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(textLocale);
        final BreakIterator breakIterator = BreakIterator.getWordInstance(textLocale);
        breakIterator.setText(text);
        this.count = 0;
        this.countDifferent = 0;
        final Set<Double> currencies = new HashSet<>();
        int start = breakIterator.first();
        for (int i = breakIterator.next(); i != BreakIterator.DONE; start = i, i = breakIterator.next()) {
            String possibleCurrency = text.substring(start, i);
            try {
                final Number currency = currencyFormat.parse(possibleCurrency);
                final double doubleValue = currency.doubleValue();
                if (this.count == 0) {
                    this.min = doubleValue;
                    this.max = doubleValue;
                }
                this.count++;
                if (!currencies.contains(doubleValue)) {
                    currencies.add(doubleValue);
                    this.countDifferent++;
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

    public Number getMin() {
        return min;
    }

    public Number getMax() {
        return max;
    }

    public Number getAverage() {
        return average;
    }
}
