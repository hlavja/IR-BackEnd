package cz.zcu.kiv.nlp.ir.trec.indexing;

import cz.zcu.kiv.nlp.ir.trec.config.Constants;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.AdvancedTokenizer;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.BasicPreprocessing;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.CzechStemmerLight;
import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing whole inverted list with indexed terms
 * Contains fields for needed metrics such as IDF, TF-UDF and whole inverted list
 * @author hlavj on 1.5.2021
 */
@Data
public class InvertedList {
    private final Logger log = LoggerFactory.getLogger(InvertedList.class);

    /**
     * Main map to store inverted index
     * inverted list <term -> <documentId, countOfTermInDocument>>
     */
    private HashMap<String, HashMap<String, Integer>> invertedList = new HashMap<>();

    /**
     * Map for storing IDF values for term
     * idf <term -> countInDocuments>
     */
    private HashMap<String, Double> idf = new HashMap<>();

    /**
     * Map for storing TF-IDF value for terms in document
     * tf-idf <documentId -> <term, tfIdf>>
     */
    private final HashMap<String, HashMap<String, Double>> tfIdf = new HashMap<>();

    /**
     * Count of documents added into inverted index
     */
    private int countOfDocuments = 0;

    /**
     * Preprocessing instance to process text
     * Stemming, StopWords etc.
     */
    private final Preprocessing preprocessing = new BasicPreprocessing(new CzechStemmerLight(), new AdvancedTokenizer(), Utils.readTXTFile(Constants.STOP_WORDS_FILE_PATH), false, true, true);

    /**
     * Batching processing adding document to inverted list
     * @param documents list of added documents
     */
    public void addDocumentsToInvertedList(List<Document> documents) {
        documents.forEach(document -> {
            addDocumentToInvertedList(document, true);
            log.info("Indexed document " + countOfDocuments + " !");
        });
    }

    /**
     * Single document adding to the inverted list. Do not increment number of documents while indexing query.
     * @param document adding document
     * @param isDocument determine if is document or query
     */
    public void addDocumentToInvertedList(Document document, boolean isDocument) {
        if (isDocument) {
            countOfDocuments++;
        }
        preprocessing.index(document.getText());
        Map<String, Integer> words = preprocessing.getWordFrequencies();
        words.forEach((key, value) -> {
            if (invertedList.containsKey(key)) { // insert document to existing key
                invertedList.get(key).put(document.getId(), value);
            } else { // create new key with first document
                HashMap<String, Integer> newDocument = new HashMap<>();
                newDocument.put(document.getId(), value);
                invertedList.put(key, newDocument);
            }
        });
    }

    /**
     * Calculating IDF and store it to idf instance
     */
    public void calculateIdf() {
        clearIdf();
        for (Map.Entry<String, HashMap<String, Integer>> entry : invertedList.entrySet()) {
            double tmp = (double) countOfDocuments / (double) entry.getValue().size();
            if (Math.log10(tmp) != 0.0) {
                idf.put(entry.getKey(), Math.log10(tmp));
            }
        }
    }

    /**
     * Clearing idf instance
     */
    public void clearIdf () {
        idf = new HashMap<>();
    }

    /**
     * Calculating TF-IDF for documents and store it to tfIdf instance
     */
    public void calculateTfIdf() {
        for (Map.Entry<String, HashMap<String, Integer>> term : invertedList.entrySet()) {
            HashMap<String, Integer> termOccurrence = term.getValue();
            for (Map.Entry<String, Integer> document : termOccurrence.entrySet()){
                if(idf.containsKey(term.getKey())){
                    HashMap<String, Double> documentTfIdfList = tfIdf.getOrDefault(document.getKey(), new HashMap<>());
                    documentTfIdfList.put(term.getKey(),(1 + Math.log10(document.getValue())) * idf.get(term.getKey()));
                    tfIdf.put(document.getKey(), documentTfIdfList); // document -> <term, idf>
                }
            }
        }
    }

    /**
     * Iterate throughout all calculated TFIDF and calculate cosine similarity for query
     * @return results with score
     */
    public HashMap<String, Double> calculateCosineSimilarity() {
        HashMap<String, Double> results = new HashMap<>();
        for (Map.Entry<String, HashMap<String, Double>> document: tfIdf.entrySet()) {
            if (!document.getKey().equals("q01")){
                results.put(document.getKey(), documentScore(document.getValue()));
            }
        }
        return results;
    }

    /**
     * Calculate cosine similarity for document and query
     * @param documentTerms terms in documents
     * @return cosine similarity rating
     */
    private Double documentScore(HashMap<String, Double> documentTerms) {
        double dq = 0.0;
        double q = 0.0;
        double d = 0.0;
        for (Map.Entry<String, Double> term : documentTerms.entrySet()) {
            if (tfIdf.get("q01") != null && tfIdf.get("q01").get(term.getKey()) != null) {
                dq = dq + (term.getValue() * tfIdf.get("q01").get(term.getKey())); // cumulate document * query vector
            }
            d = d + Math.pow(term.getValue(), 2.0); // document vector normalized
        }

        if (tfIdf.get("q01") != null) {
            for (Map.Entry<String, Double> queryTerm : tfIdf.get("q01").entrySet()) {
                q = q + (Math.pow(queryTerm.getValue(), 2.0)); // query vector normalized
            }
        }

        return dq / ( Math.pow(d, 0.5) * Math.pow(q, 0.5)); // cosine similarity math
    }

    /**
     * Remove query from indexed documents
     */
    public void removeQueryFromInvertedList() {
        invertedList.forEach((key, value) -> value.remove("q01"));
        tfIdf.remove("q01");
    }
}
