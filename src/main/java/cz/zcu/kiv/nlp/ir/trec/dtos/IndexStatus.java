package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

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
