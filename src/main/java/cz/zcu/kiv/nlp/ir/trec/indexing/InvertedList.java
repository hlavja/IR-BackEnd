package cz.zcu.kiv.nlp.ir.trec.indexing;

import cz.zcu.kiv.nlp.ir.trec.config.Constants;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.dtos.TermModel;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.BasicPreprocessing;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.CzechStemmerLight;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvertedList {
    private final Logger log = LoggerFactory.getLogger(InvertedList.class);

    // inverted list <term -> <documentId, countOfTermInDocument>>
    private final HashMap<String, HashMap<String, TermModel>> invertedList = new HashMap<>();

    // idf <term -> countInDocuments>
    private final HashMap<String, Double> idf = new HashMap<>();

    // tf-idf <documentId -> <term, tfIdf>>
    private final HashMap<String, HashMap<String, Double>> tfIdf = new HashMap<>();
    
    private int countOfDocuments = 0;

    private final Preprocessing preprocessing = new BasicPreprocessing(new CzechStemmerLight(), new AdvancedTokenizer(), Utils.readTXTFile(Constants.STOP_WORDS_FILE_PATH), false, true, true);;

    public void addDocumentsToInvertedList(List<Document> documents) {
        int tmp = 0;
        for (Document document: documents) {
            addDocumentToInvertedList(document);
            log.info("Indexed document " + tmp + " !");
            tmp++;
        }
        log.info("Indexation complete!");
    }

    public void addDocumentToInvertedList(Document document) {
        countOfDocuments++;
        preprocessing.index(document.getText());
        Map<String, Integer> words = preprocessing.getWordFrequencies();
        for (Map.Entry<String, Integer> entry : words.entrySet()) {
            if (invertedList.containsKey(entry.getKey())) {
                invertedList.get(entry.getKey()).put(document.getId(), new TermModel(document.getId(), entry.getValue()));
            } else {
                HashMap<String, TermModel> newDocument = new HashMap<>();
                newDocument.put(document.getId(), new TermModel(document.getId(), entry.getValue()));
                invertedList.put(entry.getKey(), newDocument);
            }
        }
    }

    public void calculateIdf() {
        for (Map.Entry<String, HashMap<String, TermModel>> entry : invertedList.entrySet()) {
            double tmp = (double) countOfDocuments / (double) entry.getValue().size();
            if (Math.log10(tmp) != 0.0) {
                idf.put(entry.getKey(), Math.log10(tmp));
            }
        }
    }

    public void calculateTfIdf() {
        for (Map.Entry<String, HashMap<String, TermModel>> term : invertedList.entrySet()) {
            HashMap<String, TermModel> termOccurrence = term.getValue();
            for (Map.Entry<String, TermModel> document : termOccurrence.entrySet()){
                if(idf.containsKey(term.getKey())){
                    HashMap<String, Double> documentTfIdfList = tfIdf.getOrDefault(document.getKey(), new HashMap<>());
                    documentTfIdfList.put(term.getKey(),(1 + Math.log10(document.getValue().getCount())) * idf.get(term.getKey()));
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
