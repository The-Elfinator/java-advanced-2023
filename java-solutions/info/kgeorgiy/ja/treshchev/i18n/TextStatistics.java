package info.kgeorgiy.ja.treshchev.i18n;

import info.kgeorgiy.ja.treshchev.i18n.stats.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.*;

public class TextStatistics {

    private static Locale getLocale(String argTextLocale) throws NoSuchElementException {
        return Arrays.stream(
                Locale.getAvailableLocales()).filter(
                l -> l.toString().equals(argTextLocale)
        ).findFirst().orElseThrow();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Usage: java TextStatistics <locale of the text> <locale of the output> " +
                    "<input file> <output file>");
            return;
        }
        final Locale textLocale;
        final Locale outputLocale;
        try {
            textLocale = getLocale(args[0]);
            outputLocale = getLocale(args[1]);
            if (!outputLocale.getLanguage().equals("ru") && !outputLocale.getLanguage().equals("en")) {
                System.err.println("Required only russian or english locale for output!");
                return;
            }
        } catch (NoSuchElementException e) {
            System.err.println("Locale not found! Please use one of the existing locales: " + e.getMessage());
            return;
        }
        final Path inputPath;
        final String outputPath = args[3];
        try {
            inputPath = Path.of(args[2]);
        } catch (InvalidPathException e) {
            System.err.println("Path is not valid: " + e.getMessage());
            return;
        }
        final String text;
        try {
            text = Files.readString(inputPath);
        } catch (IOException e) {
            System.err.println("Error reading from input file: " + e.getMessage());
            return;
        }
        final AllStatistic allStatistic = createStatistic(text, textLocale);

        final SentenceStat sentenceStat = allStatistic.getSentences();
        final WordStat wordStat = allStatistic.getWords();
        final NumberStat numberStat = allStatistic.getNumbers();
        final CurrencyStat currencyStat = allStatistic.getCurrencies();
        final DateStat dateStat = allStatistic.getDates();

        ResourceBundle bundle;
        if (outputLocale.getLanguage().equals("ru")) {
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.treshchev.i18n.UsageResourceBundle_ru");
        } else if (outputLocale.getLanguage().equals("en")) {
            bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.treshchev.i18n.UsageResourceBundle_en");
        } else {
            System.err.println("Error! Unsupported locale!");
            return;
        }

        try (final BufferedWriter writer = new BufferedWriter(
                new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            writer.write(bundle.getString("analyzedFile") + " " + inputPath);
            writer.newLine();
            writer.write(bundle.getString("summaryStat"));
            writer.newLine();
            writer.write("\t" + bundle.getString("number") + " " + bundle.getString("sentences")
                    + ": " + sentenceStat.getCount() + ".");
            writer.newLine();
            writer.write("\t" + bundle.getString("number") + " " + bundle.getString("words")
                    + ": " + wordStat.getCount() + ".");
            writer.newLine();
            writer.write("\t" + bundle.getString("number") + " " + bundle.getString("numbers")
                    + ": " + numberStat.getCount() + ".");
            writer.newLine();
            writer.write("\t" + bundle.getString("number") + " " + bundle.getString("currencies")
                    + ": " + currencyStat.getCount() + ".");
            writer.newLine();
            writer.write("\t" + bundle.getString("number") + " " + bundle.getString("dates")
                    + ": " + dateStat.getCount() + ".");
            writer.newLine();
            printStatSentence(sentenceStat, bundle, writer);


        } catch (IOException e) {
            System.err.println("Failed to write to output file: " + e.getMessage());
        }


    }

    private static void printStatSentence(SentenceStat sentenceStat, ResourceBundle bundle, BufferedWriter writer) throws IOException {
        writer.write(bundle.getString("sentenceStat"));
        writer.newLine();
        writer.write("\t" + bundle.getString("number") + " " + bundle.getString("sentences")
                + ": " + sentenceStat.getCount() + " (" + sentenceStat.getCountDifferent() + " " +
                bundle.getString("numberUnique") + ").");
        writer.newLine();
        writer.write("\t" + bundle.getString("min") + " " + bundle.getString("sentence")
                + ": \"" + sentenceStat.getMin() + "\".");
        writer.newLine();
        writer.write("\t" + bundle.getString("max") + " " + bundle.getString("sentence")
                + ": \"" + sentenceStat.getMax() + "\".");
        writer.newLine();
        writer.write("\t" + bundle.getString("minLength") + " " + bundle.getString("sentenceA")
                + ": " + sentenceStat.getMinLength().length() + " (\"" + sentenceStat.getMinLength() + "\").");
        writer.newLine();
        writer.write("\t" + bundle.getString("maxLength") + " " + bundle.getString("sentenceA")
                + ": " + sentenceStat.getMaxLength().length() + " (\"" + sentenceStat.getMaxLength() + "\").");
        writer.newLine();
        writer.write("\t" + bundle.getString("averageLength") + " " + bundle.getString("wordA") + ": " +
                sentenceStat.getAverage());
        writer.newLine();
    }

    private static AllStatistic createStatistic(final String text, final Locale textLocale) {
        AllStatistic allStatistic = new AllStatistic();
        allStatistic.setSentences(new SentenceStat(text, textLocale));
        allStatistic.setWords(new WordStat(text, textLocale));
        allStatistic.setNumbers(new NumberStat(text, textLocale));
        allStatistic.setDates(new DateStat(text, textLocale));
        allStatistic.setCurrencies(new CurrencyStat(text, textLocale));
        return allStatistic;
    }


}
