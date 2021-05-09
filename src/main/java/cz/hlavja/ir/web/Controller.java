package cz.hlavja.ir.web;

import cz.hlavja.ir.data.ArticleRepository;
import cz.hlavja.ir.data.Document;
import cz.hlavja.ir.data.Result;
import cz.hlavja.ir.dtos.ArticleModel;
import cz.hlavja.ir.dtos.Query;
import cz.hlavja.ir.dtos.QueryResultModel;
import cz.hlavja.ir.indexing.Index;
import cz.hlavja.ir.config.Constants;
import cz.hlavja.ir.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
@RequestMapping("/api")
public class Controller {

    private final Logger log = LoggerFactory.getLogger(Controller.class);
    ArticleRepository articleRepository = new ArticleRepository();
    Index index = new Index();

    /**
     *
     * @return
     */
    @GetMapping("/initData")
    public ResponseEntity<String> initData () {
        index = new Index();
        articleRepository = new ArticleRepository();
        articleRepository.addArticles(Utils.importArticlesFromFile(Constants.DATA_FILE_PATH));
        index.index(articleRepository.getArticlesAsDocument());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     *
     * @param query
     * @return
     */
    @PostMapping("/query")
    public QueryResultModel search(@RequestBody Query query) {
        List<Result> results = index.search(query.getQuery());
        List<Result> filteredResults = results.stream().limit(query.getNumberOfResults()).collect(Collectors.toList());
        List<ArticleModel> articles = new ArrayList<>();
        filteredResults.forEach(result -> {
            ArticleModel article = articleRepository.getArticleById(Integer.parseInt(result.getDocumentID()));
            article.setScore(result.getScore());
            article.setRank(result.getRank());
            articles.add(article);
        });
        return new QueryResultModel(articles, results.size());
    }
}
