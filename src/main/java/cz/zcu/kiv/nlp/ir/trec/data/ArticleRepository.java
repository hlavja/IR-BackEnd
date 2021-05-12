package cz.zcu.kiv.nlp.ir.trec.data;

import cz.zcu.kiv.nlp.ir.trec.dtos.ArticleModel;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Repository of articles saved in actual index
 * @author hlavj on 1.5.2021
 */
@Data
public class ArticleRepository implements Serializable {

    private static final long serialVersionUID = -8927068736199058148L;
    /**
     * Representation of database table [id, value]
     */
    private HashMap<Integer, ArticleModel> articles = new HashMap<>();
    /**
     * Auto increment index of repository
     */
    private int id = 1;

    /**
     * Method for batch process of articles
     * @param importArticlesFromFile list of articles to index
     */
    public void addArticles(List<ArticleModel> importArticlesFromFile) {
        importArticlesFromFile.forEach(this::addArticle);
    }

    /**
     * Add one article to repository
     * @param article article to add
     */
    public void addArticle(ArticleModel article) {
        article.setId(id);
        articles.put(id, article);
        id++;
    }

    /**
     * Getting article object by id
     * @param id articleId
     * @return article of given id
     */
    public ArticleModel getArticleById(Integer id) {
        return articles.getOrDefault(id, null);
    }

    /**
     * Returns all articles in repository
     * @return list of all articles
     */
    public List<ArticleModel> getAllArticles() {
        List<ArticleModel> articles = new ArrayList<>();
        this.articles.forEach((integer, articleModel) -> articles.add(articleModel));
        return articles;
    }

    /**
     * Convert article to Document object
     * @return list of document objects
     */
    public List<Document> getArticlesAsDocument() {
        List<Document> articlesToDocuments = new ArrayList<>();
        this.articles.forEach((integer, articleModel) -> articlesToDocuments.add(new DocumentNew(articleModel.getContent(), Integer.toString(integer), articleModel.getTitle(), articleModel.getPublished())));
        return articlesToDocuments;
    }

    /**
     * Remove article from repository
     * @param id of removed article
     */
    public void removeById(int id) {
        articles.remove(id);
    }

    /**
     * Update article in repository
     * @param article article to update
     */
    public void updateArticle(ArticleModel article) {
        articles.put(article.getId(), article);
    }
}
