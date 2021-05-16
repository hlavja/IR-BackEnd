package cz.zcu.kiv.nlp.ir.trec.web;

import cz.zcu.kiv.nlp.ir.trec.data.ArticleRepository;
import cz.zcu.kiv.nlp.ir.trec.data.Document;
import cz.zcu.kiv.nlp.ir.trec.data.DocumentNew;
import cz.zcu.kiv.nlp.ir.trec.data.Result;
import cz.zcu.kiv.nlp.ir.trec.dtos.*;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application controller for comunication with forntend
 */
@RestController
@RequestMapping("/api")
public class Controller {
    private final Logger log = LoggerFactory.getLogger(Controller.class);
    HashMap<String, Index> indexes = new HashMap<>();
    HashMap<String, ArticleRepository> repositories = new HashMap<>();
    private static final String MY_DATA_INDEX_NAME = "myData";
    private static final String TREC_DATA_INDEX_NAME = "trecData";


    /**
     * Init crawled data
     * @return http status determining successful/unsuccessful
     */
    @GetMapping(value="/initData", produces={"application/json"})
    public ResponseEntity<String> initData () {
        indexes.put(MY_DATA_INDEX_NAME, new Index());
        repositories.put(MY_DATA_INDEX_NAME, new ArticleRepository());
        repositories.get(MY_DATA_INDEX_NAME).addArticles(Utils.importArticlesFromFile(Constants.DATA_FILE_PATH));
        indexes.get(MY_DATA_INDEX_NAME).index(repositories.get(MY_DATA_INDEX_NAME).getArticlesAsDocument());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Init data from TREC file
     * @return http status determining successful/unsuccessful
     */
    @GetMapping(value="/initTrec", produces={"application/json"})
    public ResponseEntity<String> initTrecData () {
        indexes.put(TREC_DATA_INDEX_NAME, new Index());
        repositories.put(TREC_DATA_INDEX_NAME, new ArticleRepository());

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

        repositories.get(TREC_DATA_INDEX_NAME).addArticles(articles);
        indexes.get(TREC_DATA_INDEX_NAME).index(repositories.get(TREC_DATA_INDEX_NAME).getArticlesAsDocument());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Searching controller
     * @param queryModel query object
     * @return result model (results, number of documents)
     */
    @PostMapping(value="/query", produces={"application/json"})
    public QueryResultModel search(@RequestBody QueryModel queryModel) {
        List<Result> results;
        if (queryModel.isVectorModel()) {
            results = indexes.get(queryModel.getIndexName()).search(queryModel.getQuery());
        } else {
            results = indexes.get(queryModel.getIndexName()).search(queryModel.getQuery(), SearchType.BOOLEAN_MODEL);
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
            ArticleModel article = repositories.get(queryModel.getIndexName()).getArticleById(Integer.parseInt(result.getDocumentID()));
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
    @GetMapping(value="/saveIndex", produces={"application/json"})
    public ResponseEntity<String> saveIndex (@RequestParam String fileName) {
        if (Utils.saveIndex(indexes.get(fileName).getInvertedList(), fileName) && Utils.saveRepo(repositories.get(fileName).getArticles(), "repo_"+ fileName)){
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Clearing indexes and repositories
     * @return http status determining successful/unsuccessful
     */
    @GetMapping(value="/clearIndex", produces={"application/json"})
    public ResponseEntity<String> clearIndex() {
        indexes.clear();
        repositories.clear();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Restore index from file
     * @param fileName filename to load index from
     * @return http status determining successful/unsuccessful
     */
    @GetMapping(value="/loadIndex", produces={"application/json"})
    public ResponseEntity<String> loadIndex (@RequestParam String fileName) {
        InvertedList loadedInvertedList;
        HashMap<Integer, ArticleModel> articleModelHashMap;
        loadedInvertedList = Utils.loadIndex(fileName);
        articleModelHashMap = Utils.loadRepository("repo_"+ fileName);
        if (loadedInvertedList != null && articleModelHashMap != null){
            indexes.put(fileName, new Index());
            indexes.get(fileName).setInvertedIndex(loadedInvertedList);
            repositories.put(fileName, new ArticleRepository());
            repositories.get(fileName).setArticles(articleModelHashMap);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get article by ID
     * @param id article id
     * @param indexName name of index
     * @return article in response object
     */
    @GetMapping(value="/article/{id}", produces={"application/json"})
    public QueryResultModel getArticleById(@PathVariable int id, @RequestParam String indexName) {
        QueryResultModel response = new QueryResultModel();
        List<ArticleModel> articles = new ArrayList<>();
        articles.add(repositories.get(indexName).getArticleById(id));
        response.setArticles(articles);
        return response;
    }

    /**
     * Get all articles
     * @param indexName name of index
     * @return articles in response object
     */
    @GetMapping(value="/articles", produces={"application/json"})
    public QueryResultModel getArticles(@RequestParam String indexName) {
        QueryResultModel response = new QueryResultModel();
        List<ArticleModel> articles = repositories.get(indexName).getAllArticles();
        if (indexName.equals(TREC_DATA_INDEX_NAME)) {
            response.setArticles(articles.subList(0, 200));
        } else {
            response.setArticles(articles);
        }
        return response;
    }

    /**
     * Delete article by id
     * @param id artidle id to delete
     * @param indexName name of index
     * @return http status determining successful/unsuccessful
     */
    @DeleteMapping(value="/article/{id}", produces={"application/json"})
    public ResponseEntity<String> deleteArticle(@PathVariable int id, @RequestParam String indexName) {
        repositories.get(indexName).removeById(id);
        indexes.put(indexName, new Index());
        indexes.get(indexName).index(repositories.get(indexName).getArticlesAsDocument());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(value = "/article", produces = "application/json")
    public ArticleModel addArticle(@RequestBody ArticleModel article, @RequestParam String indexName) {
        article.setDownloadDate(new Date());
        article.setRank(-1);
        article.setScore(-1);
        int articleId = repositories.get(indexName).addArticle(article);
        article.setId(articleId);
        List<Document> articlesToDocuments = new ArrayList<>();
        articlesToDocuments.add(new DocumentNew(article.getContent(), Integer.toString(articleId), article.getTitle(), article.getPublished()));
        indexes.get(indexName).index(articlesToDocuments);
        return article;
    }

    /**
     * Update existing article in repository
     * @param article article object to update
     * @param indexName name of index
     * @return updated article
     */
    @PostMapping(value="/article", produces={"application/json"})
    public ArticleModel updateArticle(@RequestBody ArticleModel article, @RequestParam String indexName) {
        repositories.get(indexName).updateArticle(article);
        indexes.put(indexName, new Index());
        indexes.get(indexName).index(repositories.get(indexName).getArticlesAsDocument());
        return repositories.get(indexName).getArticleById(article.getId());
    }

    /**
     * Get info if backend have indexed data
     * @return status of index
     */
    @GetMapping(value="/indexStatus", produces={"application/json"})
    public List<IndexStatus> checkIndexStatus() {
        List<IndexStatus> indexStatusList = new ArrayList<>();
        indexes.forEach((key, index) -> indexStatusList.add(new IndexStatus(index.getInvertedList().getInvertedList().size() > 0, key)));
        return indexStatusList;
    }

    /**
     * Get info if server has saved index
     * @return sate of saved index
     */
    @GetMapping(value = "/savedIndex", produces = "application/json")
    public List<SavedIndexStatus> checkSavedIndexState() {
        List<SavedIndexStatus> savedIndexStatusList = new ArrayList<>();
        if (new File(MY_DATA_INDEX_NAME).exists()) {
            savedIndexStatusList.add(new SavedIndexStatus(true, MY_DATA_INDEX_NAME));
        }
        if (new File(TREC_DATA_INDEX_NAME).exists()) {
            savedIndexStatusList.add(new SavedIndexStatus(true, TREC_DATA_INDEX_NAME));
        }
        return savedIndexStatusList;
    }
}
