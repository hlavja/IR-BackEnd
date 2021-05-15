package cz.zcu.kiv.nlp.ir.trec.indexing;

import lombok.Data;
import org.apache.lucene.search.BooleanClause;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Object representing node of boolean query tree
 * @author hlavj on 1.5.2021
 */
@Data
public class BooleanNode {

    /**
     * Searching term
     */
    private String termString;

    /**
     * Determines if is term a boolean operand
     */
    private boolean isTerm;

    /**
     * Hashmap of leaves of this node
     */
    private HashMap<BooleanClause.Occur, List<BooleanNode>> leaf = new HashMap<>();

    /**
     * Method for adding leaf to node
     * @param occur boolean operands
     * @param booleanNode new leaf
     */
    public void addLeaf(BooleanClause.Occur occur, BooleanNode booleanNode) {
        if (!leaf.containsKey(occur)) { // if not operand in tree insert empty
            leaf.put(occur, new ArrayList<>());
        }
        this.leaf.get(occur).add(booleanNode); // insert to occur
    }
}
