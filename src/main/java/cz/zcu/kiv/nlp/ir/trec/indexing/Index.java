package cz.zcu.kiv.nlp.ir.trec.indexing;

import cz.zcu.kiv.nlp.ir.trec.data.*;
import lombok.Data;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 * @author tigi, hlavj
 *
 * Třída reprezentující index.
 *
 * Tuto třídu doplňte tak aby implementovala rozhranní {@link Indexer} a {@link Searcher}.
 * Pokud potřebujete, přidejte další rozhraní, která tato třída implementujte nebo
 * přidejte metody do rozhraní {@link Indexer} a {@link Searcher}.
 *
 */
@Data
public class Index implements Indexer, Searcher, Serializable {
    private static final long serialVersionUID = 7500132956422082675L;
    private final Logger log = LoggerFactory.getLogger(Index.class);

    /**
     * Instance of index article repository
     */
    private ArticleRepository articleRepository = new ArticleRepository();

    /**
     * Instance of inverted list
     */
    private InvertedList invertedList = new InvertedList();

    /**
     * Instance of Lucene parser
     */
    private final PrecedenceQueryParser parser = new PrecedenceQueryParser();

    /**
     * Method for indexing documents to inverted list
     * @param documents list of documents
     */
    public void index(List<Document> documents) {
        invertedList.addDocumentsToInvertedList(documents);
        invertedList.calculateIdf();
        invertedList.calculateTfIdf();
    }

    /**
     * Search in inverted list
     * @param query query to search for
     * @return list of results for query
     */
    public List<Result> search(String query) {
        return search(query, SearchType.VECTOR_MODEL);
    }

    /**
     * Search in inverted list
     * @param query query to search for
     * @param searchType model of searching
     * @return list of results for query
     */
    public List<Result> search (String query, SearchType searchType) {
        log.info("Searching: " + query);
        switch (searchType) { // determine which model to use
            case VECTOR_MODEL:
                return vectorSearch(query);
            case BOOLEAN_MODEL:
                return booleanSearch(query);
            default:
                return null;
        }
    }

    /**
     * Method to search for query by vector search
     * @param query to search for
     * @return list od results for query
     */
    private List<Result> vectorSearch(String query) {
        log.info("Searching vector!");
        invertedList.addDocumentToInvertedList(new DocumentNew(query, "q01", new Date()), true); // index query
        invertedList.calculateTfIdf(); // recalculate TF-IDF
        HashMap<String, Double> cosineSimilarity = invertedList.calculateCosineSimilarity();
        invertedList.removeQueryFromInvertedList();
        return getResults(cosineSimilarity); // get results from calculated cosine
    }

    /**
     * Method to search for query by boolean search
     * @param query to search for
     * @return list of results for query
     */
    private List<Result> booleanSearch(String query) {
        log.info("Searching boolean!");
        BooleanNode parsedQuery;
        try {
            parsedQuery = parseQuery(query); // parse query and create tree
        } catch (QueryNodeException e) {
            e.printStackTrace();
            return null;
        }
        BooleanSearch booleanSearch = new BooleanSearch();
        booleanSearch.setInvertedList(invertedList);
        return booleanSearch.getResultsForQuery(parsedQuery); // get result for parsed query
    }

    /**
     * Convert cosine similarity to Result object. Then sort by cosine similarity score
     * @param cosineSimilarity calculated cosine similarity
     * @return list of result from cosine similarity
     */
    private List<Result> getResults(HashMap<String, Double> cosineSimilarity) {
        List<Result> results = new ArrayList<>();
        cosineSimilarity.forEach((key, value) -> {
            if (!value.isNaN() && value != 0) {
                results.add(new ResultImpl(key, value.floatValue()));
            }
        });
        Collections.sort(results);
        int rank = 1;
        for (Result result: results) {
            result.setRank(rank);
            rank++;
        }
        return results;
    }

    /**
     * Parse query with Lucene
     * @param query query to parse
     * @return root node of parsed query tree
     * @throws QueryNodeException Lucene exception of wrong query
     */
    public BooleanNode parseQuery(String query) throws QueryNodeException {
        BooleanNode root = new BooleanNode();
        try {
            Query parsedQuery = parser.parse(query, ""); // parse query ba Lucene
            queryTree(root, parsedQuery);
        } catch (QueryNodeException e) {
            throw new QueryNodeException(e);
        }
        return root;
    }

    /**
     * Create tree structure for parsed query.
     * @param root node of tree
     * @param query Lucene parsed query
     */
    private void queryTree(BooleanNode root, Query query) {
        if (query instanceof TermQuery) {
            root.setTerm(true);
            root.setTermString(this.invertedList.getPreprocessing().getProcessedForm(((TermQuery)query).getTerm().text()));
        } else {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            booleanQuery.forEach(booleanClause -> {
                BooleanNode leaf = new BooleanNode();
                root.addLeaf(booleanClause.getOccur(), leaf);

                if (booleanClause.getQuery() instanceof BooleanQuery) { // is operand so need to process again
                    leaf.setTermString(booleanClause.getQuery().toString());
                    queryTree(leaf, booleanClause.getQuery()); // recursion for processing whole query
                } else if (booleanClause.getQuery() instanceof  TermQuery) { // is term so insert in tree
                    leaf.setTerm(true);
                    leaf.setTermString(this.invertedList.getPreprocessing().getProcessedForm(booleanClause.getQuery().toString())); // preprocessing query term
                }
            });
        }
    }

    public List<String> getPreprocessedSearchWords(String query) {
        String[] queryWords = query.split(" ");
        for (int i = 0; i < queryWords.length; i++) {
            if (queryWords[i].equals("NOT")) {
                queryWords[i] = "";
                queryWords[i+1] = "";
            };
            if (!queryWords[i].equals("AND") && !queryWords[i].equals("OR")) {
                queryWords[i] = this.invertedList.getPreprocessing().getProcessedForm(queryWords[i].replace("(", "").replace(")", ""));
            }
        }
        return Arrays.asList(queryWords);
    }
}
