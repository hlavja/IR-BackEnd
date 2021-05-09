package cz.zcu.kiv.nlp.ir.trec.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Article representation of JSON object
 * @author hlavj on 1.5.2021
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ArticleModel implements Serializable {

    private static final long serialVersionUID = -6729626712081187172L;

    public ArticleModel() {
    }

    public ArticleModel(String title, Date downloadDate, String content) {
        this.title = title;
        this.downloadDate = downloadDate;
        this.content = content;
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
