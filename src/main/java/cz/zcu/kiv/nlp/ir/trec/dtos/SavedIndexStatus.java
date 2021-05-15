package cz.zcu.kiv.nlp.ir.trec.dtos;

import lombok.Data;

@Data
public class SavedIndexStatus {
    private boolean saved;

    public SavedIndexStatus () {

    }

    public SavedIndexStatus(boolean saved) {
        this.saved = saved;
    }
}
