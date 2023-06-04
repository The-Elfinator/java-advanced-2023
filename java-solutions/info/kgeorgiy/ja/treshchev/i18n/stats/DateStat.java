package info.kgeorgiy.ja.treshchev.i18n.stats;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DateStat {

    private int count;
    private int countDifferent;
    private double minDouble;
    private double maxDouble;
    private double averageDouble;

    public DateStat(final String text, final Locale textLocale) {
        this.count = 0;
        this.countDifferent = 0;
        averageDouble = 0;
        final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, textLocale);
        final BreakIterator breakIterator = BreakIterator.getWordInstance(textLocale);
        breakIterator.setText(text);
        final Set<Date> dates = new HashSet<>();
        int start = breakIterator.first();
        for (int i = breakIterator.next(); i != BreakIterator.DONE; start = i, i = breakIterator.next()) {
            String possibleDate = text.substring(start, i);
            try {
                final Date date = dateFormat.parse(possibleDate);
                final double doubleValue = date.getTime();
                if (this.count == 0) {
                    this.minDouble = doubleValue;
                    this.maxDouble = doubleValue;
                }
                this.count++;
                if (!dates.contains(date)) {
                    dates.add(date);
                    this.countDifferent++;
                }
                if (doubleValue < this.minDouble) {
                    this.minDouble = doubleValue;
                }
                if (doubleValue > this.maxDouble) {
                    this.maxDouble = doubleValue;
                }
                this.averageDouble += doubleValue;
            } catch (ParseException ignored) {
                // do nothing
            }
        }
        this.averageDouble /= this.count;
    }

    public int getCount() {
        return count;
    }

    public int getCountDifferent() {
        return countDifferent;
    }

    public Date getMin() {
        return new Date((long) this.minDouble);
    }

    public Date getMax() {
        return new Date((long) this.maxDouble);
    }

    public Date getAverage() {
        return new Date((long) averageDouble);
    }
}
