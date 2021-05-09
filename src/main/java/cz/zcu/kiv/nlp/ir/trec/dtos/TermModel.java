package cz.zcu.kiv.nlp.ir.trec.dtos;


import lombok.Data;

@Data
public class TermModel {
    private String documentId;

    private int count;

    private double tfidf;

    public TermModel(String documentId, int count) {
        this.documentId = documentId;
        this.count = count;
    }

    public TermModel() {
    }
}
