package cz.zcu.kiv.nlp.ir.trec.indexing;

import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.data.ResultImpl;
import lombok.Data;
import org.apache.lucene.search.BooleanClause;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Class for evaluation boolean model
 * @author hlavj on 1.5.2021
 */
@Data
public class BooleanQuerySearch {
    /**
     * Inverted index with indexed data
     */
    private InvertedList invertedList;

    /**
     * Entry method for evaluation of parsed boolean query
     * @param parsedQuery input parsed query
     * @return list of matching documents as result
     */
    public List<Result> getResultsForQuery(BooleanNode parsedQuery) {
        List<String> documentIds = processQuery(parsedQuery, false);
        List<Result> results = new ArrayList<>();
        documentIds.forEach(documentId -> {
            Result newResult = new ResultImpl(documentId, 0);
            results.add(newResult);
        });
        return results;
    }

    /**
     * Recursively process the tree of boolean querry
     * @param node node of tree
     * @param notOperator if is used NOT operator
     * @return list of documents for query
     */
    private List<String> processQuery(BooleanNode node, boolean notOperator) {
        if (node.isTermBoolean()) {
            if (notOperator) {
                HashMap<String, Integer> allDocuments = new HashMap<>();
                invertedList.getInvertedList().forEach((key, value) -> {
                    allDocuments.putAll(value);
                });
                HashMap<String, Integer> documentsWithTerm = getDocumentsWithTerm(node.getTerm());
                documentsWithTerm.forEach((key, value) -> allDocuments.remove(key));
                List<String> documents = new ArrayList<>();
                allDocuments.forEach((key, value) -> documents.add(key));
                return documents;
            } else {
                HashMap<String, Integer> documentsWithTerm = getDocumentsWithTerm(node.getTerm());
                List<String> documents = new ArrayList<>();
                documentsWithTerm.forEach((key, value) -> documents.add(key));
                return documents;
            }
        } else {
            Collection<BooleanNode> treeQuery;
            List<String> documents = new ArrayList<>();
            for (BooleanClause.Occur occur : node.getLeaf().keySet()) {
                treeQuery = node.getLeaf().get(occur);

                switch (occur) {
                    case MUST:
                        if (notOperator) {
                            for(BooleanNode booleanNode: treeQuery) {
                                documents = or(documents, processQuery(booleanNode, true));
                            }
                        } else {
                            for(BooleanNode booleanNode: treeQuery) {
                                documents = and(documents, processQuery(booleanNode, false));
                            }
                        }
                        break;
                    case SHOULD:
                        if (notOperator) {
                            for(BooleanNode booleanNode: treeQuery) {
                                documents = and(documents, processQuery(booleanNode, true));
                            }
                        } else {
                            for(BooleanNode booleanNode: treeQuery) {
                                documents = or(documents, processQuery(booleanNode, false));
                            }
                        }
                        break;
                    case MUST_NOT:
                        for(BooleanNode booleanNode: treeQuery) {
                            documents = not(documents, processQuery(booleanNode, true));
                        }
                        break;
                }

            }
          return documents;
        }
    }

    /**
     * If not empty lists evaluate AND operator
     * @param documents first list of documents
     * @param processQuery second list of documents
     * @return intersect of set A and B
     */
    private List<String> and(List<String> documents, List<String> processQuery) {
        List<String> workingList = checkEmptyLists(documents, processQuery);
        if (workingList != null) {
            return workingList;
        }
        List<String> finalWorkingList = new ArrayList<>();
        documents.forEach(documentId -> {
            if (processQuery.contains(documentId)) { //if document is also in second list add it to result
                finalWorkingList.add(documentId);
            }
        });
        return finalWorkingList;
    }

    /**
     * If not empty lists evaluate OR operator
     * @param documents first list of documents
     * @param processQuery second list of documents
     * @return unification of set A and B
     */
    private List<String> or(List<String> documents, List<String> processQuery) {
        List<String> workingList = checkEmptyLists(documents, processQuery);
        if (workingList != null) {
            return workingList;
        }
        processQuery.forEach(documentId -> {
            if (!documents.contains(documentId)) {
                documents.add(documentId);
            }
        });
        return documents;
    }

    /**
     * If not empty lists evaluate NOT operator
     * @param documents first list of documents
     * @param processQuery second list of documents
     * @return list of documents without
     */
    private List<String> not(List<String> documents, List<String> processQuery) {
        List<String> workingList = checkEmptyLists(documents, processQuery);
        if (workingList != null) {
            return workingList;
        }
        List<String> finalWorkingList = new ArrayList<>();
        processQuery.forEach(documentId -> {
            if (documents.contains(documentId)) {
                finalWorkingList.add(documentId);
            }
        });
        return finalWorkingList;
    }

    /**
     * Checking if any of lists is empty
     * @param firstList first list of documents
     * @param secondList second list of documents
     * @return if at least one not empty return list, otherwise null
     */
    private List<String> checkEmptyLists(List<String> firstList, List<String> secondList) {
        if (firstList.isEmpty() && secondList.isEmpty()) {
            return new ArrayList<>();
        } else if (firstList.isEmpty()) {
            return secondList;
        } else if (secondList.isEmpty()) {
            return firstList;
        } else {
            return null;
        }
    }

    /**
     * Finds documents with given term
     * @param term looked term
     * @return list of documents with term
     */
    private HashMap<String, Integer> getDocumentsWithTerm(String term) {
        if (invertedList.getInvertedList().containsKey(term)) {
            return invertedList.getInvertedList().get(term);
        } else {
            return new HashMap<>();
        }
    }
}
