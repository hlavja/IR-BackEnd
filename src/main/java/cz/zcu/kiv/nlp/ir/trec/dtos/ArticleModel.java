package cz.zcu.kiv.nlp.ir.trec.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ArticleModel implements Serializable {

    public ArticleModel() {

    }

    public ArticleModel(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public ArticleModel(Integer id, String author, Date downloadDate, String title, String url, String content, String category, Date published, Integer rank, float score) {
        this.id = id;
        this.author = author;
        this.downloadDate = downloadDate;
        this.title = title;
        this.url = url;
        this.content = content;
        this.category = category;
        this.published = published;
        this.rank = rank;
        this.score = score;
    }

    private Integer id = null;

    private String author;

    private Date downloadDate;

    private String title;

    private String url;

    private String content;

    private String category;

    private Date published;

    private Integer rank = -1;

    private float score = -1;

    @Override
    public String toString() {
        return "ArticleModel{" +
                "downloadDate='" + downloadDate + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", content='" + content + '\'' +
                ", category='" + category + '\'' +
                ", url='" + url + '\'' +
                ", published='" + published + '\'' +
                '}';
    }
}
