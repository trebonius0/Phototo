package photato.helpers;

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SafeSimpleDateFormat extends SimpleDateFormat {

    private final Object lock = new Object();

    public SafeSimpleDateFormat() {
    }

    public SafeSimpleDateFormat(String pattern) {
        super(pattern);
    }

    public SafeSimpleDateFormat(String pattern, Locale locale) {
        super(pattern, locale);
    }

    public SafeSimpleDateFormat(String pattern, DateFormatSymbols formatSymbols) {
        super(pattern, formatSymbols);
    }

    @Override
    public void setDateFormatSymbols(DateFormatSymbols newFormatSymbols) {
        synchronized (this.lock) {
            super.setDateFormatSymbols(newFormatSymbols);
        }
    }

    @Override
    public DateFormatSymbols getDateFormatSymbols() {
        synchronized (this.lock) {
            return super.getDateFormatSymbols();
        }
    }

    @Override
    public void applyLocalizedPattern(String pattern) {
        synchronized (this.lock) {
            super.applyLocalizedPattern(pattern);
        }
    }

    @Override
    public void applyPattern(String pattern) {
        synchronized (this.lock) {
            super.applyPattern(pattern);

        }
    }

    @Override
    public String toLocalizedPattern() {
        synchronized (this.lock) {
            return super.toLocalizedPattern();
        }
    }

    @Override
    public String toPattern() {
        synchronized (this.lock) {
            return super.toPattern();
        }
    }

    @Override
    public Date parse(String text, ParsePosition pos) {
        synchronized (this.lock) {
            return super.parse(text, pos);
        }
    }

    @Override
    public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
        synchronized (this.lock) {
            return super.formatToCharacterIterator(obj);
        }
    }

    @Override
    public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        synchronized (this.lock) {
            return super.format(date, toAppendTo, pos);
        }
    }

    @Override
    public Date get2DigitYearStart() {
        synchronized (this.lock) {
            return super.get2DigitYearStart();
        }
    }

    @Override
    public void set2DigitYearStart(Date startDate) {
        synchronized (this.lock) {
            super.set2DigitYearStart(startDate);
        }
    }

}
