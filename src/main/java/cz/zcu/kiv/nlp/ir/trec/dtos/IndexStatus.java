package cz.zcu.kiv.nlp.ir.trec.dtos;/*
 * @version 12.05.2021
 * @author hlavj
 */

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
