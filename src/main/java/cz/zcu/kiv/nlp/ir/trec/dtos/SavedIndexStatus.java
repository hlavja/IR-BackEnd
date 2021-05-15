package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

@Data
public class SavedIndexStatus {
    private boolean saved;
    private String indexName;

    public SavedIndexStatus () {

    }

    public SavedIndexStatus(boolean saved, String indexName) {
        this.saved = saved;
        this.indexName = indexName;
    }
}
