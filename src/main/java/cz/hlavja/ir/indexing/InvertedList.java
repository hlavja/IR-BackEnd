package cz.hlavja.ir.indexing;

import cz.hlavja.ir.config.Constants;
import cz.hlavja.ir.data.Document;
import cz.hlavja.ir.preprocessing.*;
import cz.hlavja.ir.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedList {

    // inverted list <term -> <documentId, countOfTermInDocument>>
    private final HashMap<String, HashMap<String, Integer>> invertedList = new HashMap<>();

    // idf <term -> countInDocuments>
    private final HashMap<String, Double> idf = new HashMap<>();

    // tf-idf <documentId -> <term, tfIdf>>
    private final HashMap<String, HashMap<String, Double>> tfIdf = new HashMap<>();
    
    private int countOfDocuments = 0;

    private final Preprocessing preprocessing = new BasicPreprocessing(new CzechStemmerLight(), new AdvancedTokenizer(), Utils.readTXTFile(Constants.STOP_WORDS_FILE_PATH), false, true, true);;

    public void addDocumentsToInvertedList(List<Document> documents) {
        for (Document document: documents) {
            countOfDocuments++;
            String [] words = document.getText().split("\\s+");
            for (String word: words) {
                if (invertedList.containsKey(word)) {
                    HashMap<String, Integer> documentsWithWord = invertedList.get(word);
                    if (documentsWithWord.get(document.getId()) != null){
                        documentsWithWord.put(document.getId(), documentsWithWord.get(document.getId()) + 1);
                    } else {
                        documentsWithWord.put(document.getId(), 1);
                    }
                    invertedList.put(word, documentsWithWord);
                } else {
                    HashMap<String, Integer> newDocument = new HashMap<>();
                    newDocument.put(document.getId(), 1);
                    invertedList.put(word, newDocument);
                }
            }
        }
    }

    public void calculateIdf() {
        for (Map.Entry<String, HashMap<String, Integer>> entry : invertedList.entrySet()) {
            double tmp = (double) countOfDocuments / (double) entry.getValue().size();
            if (Math.log10(tmp) != 0.0) {
                idf.put(entry.getKey(), Math.log10(tmp));
            }
        }
    }

    public void calculateTfIdf() {
        for (Map.Entry<String, HashMap<String, Integer>> term : invertedList.entrySet()) {
            HashMap<String, Integer> termOccurrence = term.getValue();
            for (Map.Entry<String, Integer> document : termOccurrence.entrySet()){
                if(idf.containsKey(term.getKey())){
                    HashMap<String, Double> documentTfIdfList = tfIdf.getOrDefault(document.getKey(), new HashMap<>());
                    documentTfIdfList.put(term.getKey(),(1 + Math.log10(document.getValue())) * idf.get(term.getKey()));
                    tfIdf.put(document.getKey(), documentTfIdfList);
                }
            }
        }
    }

    public HashMap<String, Double> calculateCosineSimilarity() {
        HashMap<String, Double> results = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Double>> document: tfIdf.entrySet()) {
            if (!document.getKey().equals("q01")){
                results.put(document.getKey(), documentScore(document.getValue()));
            }
        }
        return results;
    }

    private Double documentScore(HashMap<String, Double> documentTerms) {
        double dq = 0.;
        double q = 0.;
        double d = 0.;
        for (Map.Entry<String, Double> term : documentTerms.entrySet()) {
            if (tfIdf.get("q01") != null && tfIdf.get("q01").get(term.getKey()) != null) {
                dq = dq + (term.getValue() * tfIdf.get("q01").get(term.getKey()));
            }
            d = d + Math.pow(term.getValue(), 2);
        }

        if (tfIdf.get("q01") != null){
            for (Map.Entry<String, Double> queryTerm : tfIdf.get("q01").entrySet()) {
                q = q + Math.pow(queryTerm.getValue(), 2);
            }
        }

        return dq / ( Math.pow(d, 0.5) * Math.pow(q, 0.5));
    }

    public void removeQueryFromInvertedList() {
        invertedList.forEach((key, value) -> value.remove("q01"));
        tfIdf.remove("q01");
    }
}
