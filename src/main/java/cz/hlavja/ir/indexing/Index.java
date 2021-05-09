package cz.hlavja.ir.indexing;

import cz.hlavja.ir.data.*;

import java.util.*;

/**
 * @author tigi
 *
 * Třída reprezentující index.
 *
 * Tuto třídu doplňte tak aby implementovala rozhranní {@link cz.hlavja.ir.indexing.Indexer} a {@link Searcher}.
 * Pokud potřebujete, přidejte další rozhraní, která tato třída implementujte nebo
 * přidejte metody do rozhraní {@link cz.hlavja.ir.indexing.Indexer} a {@link Searcher}.
 *
 *
 */
public class Index implements Indexer, Searcher {

    private final InvertedList invertedList = new InvertedList();

    public void index(List<Document> documents) {
        invertedList.addDocumentsToInvertedList(documents);
        invertedList.calculateIdf();
        invertedList.calculateTfIdf();
    }

    public List<Result> search(String query) {
        List<Document> queryDocuments = new ArrayList<>();
        queryDocuments.add(new DocumentNew(query, "q01", new Date()));
        invertedList.addDocumentsToInvertedList(queryDocuments);
        invertedList.calculateTfIdf(); // improve to not calculate whole tf-idf again
        HashMap<String, Double> cosineSimilarity = invertedList.calculateCosineSimilarity();
        invertedList.removeQueryFromInvertedList();
        return getResults(cosineSimilarity);
    }

    private List<Result> getResults(HashMap<String, Double> cosineSimilarity) {
        List<Result> results = new ArrayList<>();
        for (Map.Entry<String, Double> entry : cosineSimilarity.entrySet()) {
            results.add(new ResultImpl(entry.getKey(), entry.getValue().floatValue()));
        }
        Collections.sort(results);
        int rank = 1;
        for (Result result: results) {
            result.setRank(rank);
            rank++;
        }
        return results;
    }

}
