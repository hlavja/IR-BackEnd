package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

/**
 * Model of server response to frontend
 * carries information about index in memory
 * @author hlavj on 1.5.2021
 */
@Data
public class IndexStatus {
    private boolean initialized;
    private String indexName;

    public IndexStatus () {

    }

    public IndexStatus(boolean initialized, String indexName) {
        this.initialized = initialized;
        this.indexName = indexName;
    }
}
