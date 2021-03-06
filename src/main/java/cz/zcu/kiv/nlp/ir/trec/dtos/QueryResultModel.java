package cz.zcu.kiv.nlp.ir.trec.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Model of server response to frontend
 * query evaluation
 * @author hlavj on 1.5.2021
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class QueryResultModel implements Serializable {

    private static final long serialVersionUID = 7376802919774150238L;

    public QueryResultModel() {
    }

    public QueryResultModel(List<ArticleModel> articles, Integer documentsCount, List<String> searchWords) {
        this.articles = articles;
        this.documentsCount = documentsCount;
        this.searchWords = searchWords;
    }

    /**
     * List of articles
     */
    private List<ArticleModel> articles;

    /**
     * List of preprocessed search words
     */
    private List<String> searchWords;

    /**
     * Number of articles for given query
     */
    private Integer documentsCount;
}
