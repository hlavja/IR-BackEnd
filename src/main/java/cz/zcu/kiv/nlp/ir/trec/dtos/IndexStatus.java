package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

@Data
public class IndexStatus {
    private boolean initialized;

    public IndexStatus () {

    }

    public IndexStatus(boolean initialized) {
        this.initialized = initialized;
    }
}
