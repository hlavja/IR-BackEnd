package cz.zcu.kiv.nlp.ir.trec.preprocessing;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Tigi on 29.2.2016.
 */
public class BasicPreprocessing implements Preprocessing, Serializable {

    private static final long serialVersionUID = 8801846219057684547L;
    Map<String, Integer> wordFrequencies = new HashMap<>();
    Stemmer stemmer;
    Tokenizer tokenizer;
    Set<String> stopWords;
    boolean removeAccentsBeforeStemming;
    boolean removeAccentsAfterStemming;
    boolean toLowercase;

    public BasicPreprocessing(Stemmer stemmer, Tokenizer tokenizer, Set<String> stopWords, boolean removeAccentsBeforeStemming, boolean removeAccentsAfterStemming, boolean toLowercase) {
        this.stemmer = stemmer;
        this.tokenizer = tokenizer;
        this.stopWords = stopWords;
        this.removeAccentsBeforeStemming = removeAccentsBeforeStemming;
        this.removeAccentsAfterStemming = removeAccentsAfterStemming;
        this.toLowercase = toLowercase;
    }

    @Override
    public void index(String document) {
        this.wordFrequencies.clear(); // clear word frequencies for every one processed document

        if (toLowercase) {
            document = document.toLowerCase();
        }
        if (removeAccentsBeforeStemming) {
            document = removeAccents(document);
        }
        for (String token : tokenizer.tokenize(document)) {
            if (stopWords.contains(token)){
                continue;
            }
            if (stemmer != null) {
                token = stemmer.stem(token);
            }
            if (removeAccentsAfterStemming) {
                token = removeAccents(token);
            }
            if (!wordFrequencies.containsKey(token)) {
                wordFrequencies.put(token, 0);
            }

            wordFrequencies.put(token, wordFrequencies.get(token) + 1);
        }
    }

    @Override
    public String getProcessedForm(String text) {
        if (toLowercase) {
            text = text.toLowerCase();
        }
        if (removeAccentsBeforeStemming) {
            text = removeAccents(text);
        }
        if (stemmer != null) {
            text = stemmer.stem(text);
        }
        if (removeAccentsAfterStemming) {
            text = removeAccents(text);
        }
        return text;
    }

    private String removeAccents(String text) {
        return StringUtils.stripAccents(text);
    }

    public Map<String, Integer> getWordFrequencies() {
        return wordFrequencies;
    }
}
