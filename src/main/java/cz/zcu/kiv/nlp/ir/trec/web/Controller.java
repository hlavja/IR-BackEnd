package cz.zcu.kiv.nlp.ir.trec.web;

import cz.zcu.kiv.nlp.ir.trec.data.ArticleRepository;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.dtos.ArticleModel;
import cz.zcu.kiv.nlp.ir.trec.dtos.QueryModel;
import cz.zcu.kiv.nlp.ir.trec.dtos.QueryResultModel;
import cz.zcu.kiv.nlp.ir.trec.indexing.Index;
import cz.zcu.kiv.nlp.ir.trec.config.Constants;
import cz.zcu.kiv.nlp.ir.trec.indexing.InvertedList;
import cz.zcu.kiv.nlp.ir.trec.indexing.SearchType;
import cz.zcu.kiv.nlp.ir.trec.utils.SerializedDataHelper;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application controller for comunication with forntend
 */
@RestController
@RequestMapping("/api")
public class Controller {
    private final Logger log = LoggerFactory.getLogger(Controller.class);
    ArticleRepository articleRepository = new ArticleRepository();
    Index index = new Index();

    /**
     * Init crawled data
     * @return http status determining successful/unsuccessful
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
     * Init data from TREC file
     * @return http status determining successful/unsuccessful
     */
    @GetMapping("/initTrec")
    public ResponseEntity<String> initTrecData () {
        index = new Index();
        articleRepository = new ArticleRepository();

        File serializedData = new File(Constants.OUTPUT_DIR + "/czechData.bin");
        List<Document> documents = new ArrayList<>();
        try {
            if (serializedData.exists()) {
                documents = SerializedDataHelper.loadDocument(serializedData);
            } else {
                log.error("Cannot find " + serializedData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<ArticleModel> articles = new ArrayList<>();

        if (documents != null) {
            documents.forEach(document -> {
                ArticleModel newArticle = new ArticleModel(document.getTitle(), document.getDate(), document.getText());
                articles.add(newArticle);
            });
        }

        index.index(documents);
        articleRepository.addArticles(articles);
        index.index(articleRepository.getArticlesAsDocument());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Searching controller
     * @param queryModel query object
     * @return result model (results, number of documents)
     */
    @PostMapping("/query")
    public QueryResultModel search(@RequestBody QueryModel queryModel) {
        List<Result> results;
        if (queryModel.isVectorModel()) {
            results = index.search(queryModel.getQuery());
        } else {
            results = index.search(queryModel.getQuery(), SearchType.BOOLEAN_MODEL);
        }
        int numberOfResults;
        if (queryModel.getNumberOfResults() == null || queryModel.getNumberOfResults() < Constants.MIN_RESULTS) {
            numberOfResults = Constants.MIN_RESULTS;
        } else {
            numberOfResults = queryModel.getNumberOfResults();
        }
        List<Result> filteredResults = results.stream().limit(numberOfResults).collect(Collectors.toList());
        List<ArticleModel> articles = new ArrayList<>();
        filteredResults.forEach(result -> {
            ArticleModel article = articleRepository.getArticleById(Integer.parseInt(result.getDocumentID()));
            article.setScore(result.getScore());
            article.setRank(result.getRank());
            articles.add(article);
        });
        return new QueryResultModel(articles, results.size());
    }

    /**
     * Save index to file
     * @param fileName filename to save index to
     * @return http status determining successful/unsuccessful
     */
    @GetMapping("/saveIndex")
    public ResponseEntity<String> saveIndex (@RequestParam String fileName) {
        if (Utils.saveIndex(index, fileName)){
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Restore index from file
     * @param fileName filename to load index from
     * @return http status determining successful/unsuccessful
     */
    @GetMapping("/loadIndex")
    public ResponseEntity<String> loadIndex (@RequestParam String fileName) {
        InvertedList loadedInvertedList;
        loadedInvertedList = Utils.loadIndex(fileName);
        if (loadedInvertedList != null){
            index = new Index();
            index.setInvertedIndex(loadedInvertedList);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
