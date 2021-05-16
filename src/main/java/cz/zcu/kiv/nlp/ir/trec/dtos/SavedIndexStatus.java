package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

/**
 * Model of server response to frontend
 * carries information about index saved in file
 * @author hlavj on 12.5.2021
 */
@Data
public class SavedIndexStatus {
    private boolean saved;
    private String indexName;

    public SavedIndexStatus () {

    }

    /**
     * Constructor
     * @param saved if saved
     * @param indexName index name
     */
    public SavedIndexStatus(boolean saved, String indexName) {
        this.saved = saved;
        this.indexName = indexName;
    }
}
