package info.kgeorgiy.ja.treshchev.i18n.stats;

public class AllStatistic {

    private SentenceStat sentences;
    private WordStat words;
    private NumberStat numbers;
    private CurrencyStat currencies;
    private DateStat dates;

    public void setSentences(SentenceStat sentences) {
        this.sentences = sentences;
    }

    public void setWords(WordStat words) {
        this.words = words;
    }

    public void setNumbers(NumberStat numbers) {
        this.numbers = numbers;
    }

    public void setCurrencies(CurrencyStat currencies) {
        this.currencies = currencies;
    }

    public void setDates(DateStat dates) {
        this.dates = dates;
    }

    public SentenceStat getSentences() {
        return this.sentences;
    }

    public WordStat getWords() {
        return this.words;
    }

    public NumberStat getNumbers() {
        return this.numbers;
    }

    public CurrencyStat getCurrencies() {
        return this.currencies;
    }

    public DateStat getDates() {
        return this.dates;
    }
}
