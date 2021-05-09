package cz.zcu.kiv.nlp.ir.trec.data;

/**
 * Created by Tigi on 8.1.2015.
 *
 * Třída představuje výsledek vrácený po vyhledávání.
 * Třídu můžete libovolně upravovat, popř. si můžete vytvořit vlastní třídu,
 * která dědí od abstraktní třídy {@link AbstractResult}
 */
public class ResultImpl extends AbstractResult {

    /**
     * Constructor with result properties
     * @param documentID documentId
     * @param score score of document
     */
    public ResultImpl(String documentID, float score) {
        this.documentID = documentID;
        this.score = score;
    }

    /**
     * Empty construcotr
     */
    public ResultImpl() {
    }

    /**
     * Compare teo documents by its score
     * @param result comparing result to this one
     * @return number if this result is better than compared to
     */
    @Override
    public int compareTo(Result result) {
        int compareResult = Double.compare(this.score, result.getScore());
        if (compareResult == 1) {
            return -1;
        } else if (compareResult == -1) {
            return 1;
        } else {
            return 0;
        }
    }
}
