package cz.zcu.kiv.nlp.ir.trec.indexing;

import cz.zcu.kiv.nlp.ir.trec.preprocessing.Preprocessing;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.precedence.PrecedenceQueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for parsing query with boolean logic. Uses Lucene library.
 * @author hlavj on 1.5.2021
 */
public class BooleanParser {
    private final Logger log = LoggerFactory.getLogger(BooleanParser.class);

    /**
     * Instance for preprocessing parsed terms in query
     */
    private final Preprocessing preprocessing;

    /**
     * Instance of Lucene parser
     */
    private final PrecedenceQueryParser parser = new PrecedenceQueryParser();

    public BooleanParser(Preprocessing preprocessing) {
        this.preprocessing = preprocessing;
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
            root.setTermBoolean(true);
            root.setTerm(preprocessing.getProcessedForm(((TermQuery)query).getTerm().text()));
        } else {
            BooleanQuery booleanQuery = (BooleanQuery) query;
            booleanQuery.forEach(booleanClause -> {
                BooleanNode leaf = new BooleanNode();
                root.addLeaf(booleanClause.getOccur(), leaf);

                if (booleanClause.getQuery() instanceof BooleanQuery) { // is operand so need to process again
                    leaf.setTerm(booleanClause.getQuery().toString());
                    queryTree(leaf, booleanClause.getQuery()); // recursion for processing whole query
                } else if (booleanClause.getQuery() instanceof  TermQuery) { // is term so insert in tree
                    leaf.setTermBoolean(true);
                    leaf.setTerm(preprocessing.getProcessedForm(booleanClause.getQuery().toString())); // preprocessing query term
                }
            });
        }
    }
}
