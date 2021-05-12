/*
 * Copyright (c) 2014, Michal Konkol
 * All rights reserved.
 */
package cz.zcu.kiv.nlp.ir.trec.preprocessing;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Michal Konkol
 */
public class AdvancedTokenizer implements Tokenizer, Serializable {

    public static final String defaultRegex = "([0-3]?\\d\\.{1})([01]?\\d\\.{1})([12]{1}\\d{3})|([0-3]?\\d\\.{1})([01]?\\d\\.{1})|[0-9]\\+[01]|([a-z-A-Z]*[*][a-z-A-Z]*)|((https|http):\\/\\/[-a-zA-Z0-9+&\\/%?=_!:,.;]*[-a-zA-Z0-9+&@#\\/%=~_])|(<.*?>)|(\\d+[.,](\\d+)?)|([\\p{L}\\d]+)|([\\p{Punct}])";
    private static final long serialVersionUID = -2867686429684517437L;

    public static String[] tokenize(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);

        ArrayList<String> words = new ArrayList<>();

        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            words.add(text.substring(start, end));
        }

        String[] ws = new String[words.size()];
        ws = words.toArray(ws);

        return ws;
    }

    public static String removeAccents(String text) {
        return text == null ? null : Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    public String[] tokenize(String text) {
        return tokenize(text, defaultRegex);
    }
}
