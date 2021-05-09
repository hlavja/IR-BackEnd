package cz.zcu.kiv.nlp.ir.trec.data;

import cz.zcu.kiv.nlp.ir.trec.dtos.ArticleModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ArticleRepository implements Serializable {

    private final HashMap<Integer, ArticleModel> articles = new HashMap<>();
    private int id = 0;

    public void addArticles(List<ArticleModel> importArticlesFromFile) {
        importArticlesFromFile.forEach(this::addArticle);
    }

    public void addArticle(ArticleModel article) {
        article.setId(id);
        articles.put(id, article);
        id++;
    }

    public ArticleModel getArticleById(Integer id) {
        return articles.getOrDefault(id, null);
    }

    public List<ArticleModel> getAllArticles() {
        List<ArticleModel> articles = new ArrayList<>();
        this.articles.forEach((integer, articleModel) -> articles.add(articleModel));
        return articles;
    }

    public List<Document> getArticlesAsDocument() {
        List<Document> articlesToDocuments = new ArrayList<>();
        this.articles.forEach((integer, articleModel) -> {
            articlesToDocuments.add(new DocumentNew(articleModel.getContent(), Integer.toString(integer), articleModel.getTitle(), articleModel.getPublished()));
        });
        return articlesToDocuments;
    }
}
