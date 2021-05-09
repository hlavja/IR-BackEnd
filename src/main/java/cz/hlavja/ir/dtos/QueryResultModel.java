package cz.hlavja.ir.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class QueryResultModel {

    public QueryResultModel() {

    }

    public QueryResultModel(List<ArticleModel> articles, Integer documentsCount) {
        this.articles = articles;
        this.documentsCount = documentsCount;
    }

    private List<ArticleModel> articles;
    private Integer documentsCount;
}
