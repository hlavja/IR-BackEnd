package cz.zcu.kiv.nlp.ir.trec.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.zcu.kiv.nlp.ir.trec.dtos.ArticleModel;
import cz.zcu.kiv.nlp.ir.trec.utils.Utils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * CrawlerVSCOM class acts as a controller. You should only adapt this file to serve your needs.
 * Created by Tigi on 31.10.2014.
 */
public class CrawlerVSCOM {
    /**
     * Xpath expressions to extract and their descriptions.
     */
    private final static Map<String, String> xpathMap = new HashMap<>();


    private final static  String AUTHOR = "author";
    private final static  String TITLE = "title";
    private final static  String CATEGORY = "category";
    private final static  String PUBLISHED = "published";
    private final static List<String> objectAttributes = Arrays.asList(AUTHOR, TITLE, CATEGORY, PUBLISHED);


    private final static int MAX_ARTICLE_PAGES = 25;

    static {
        xpathMap.put("allText", "//div[@class='entry-content' and @class!='addtoany_content_bottom']/allText()");
        xpathMap.put("html", "//div[@class='entry-content' and @class!='addtoany_content_bottom']/html()");
        xpathMap.put("tidyText", "//div[@class='entry-content' and @class!='addtoany_content_bottom']/tidyText()");


        xpathMap.put(AUTHOR, "//span[@class='author']/a/allText()");
        xpathMap.put(TITLE, "//h1[@class='entry-title']/allText()");
        xpathMap.put(CATEGORY, "//footer[@class='entry-footer']/span[@class='cat-links']/a/allText()");
        xpathMap.put(PUBLISHED, "//span[@class='posted-on']/*/time/@datetime");
    }

    private static final String STORAGE = "./storage/NetworkNews";
    private static final String SITE = "https://cz.cw-nn.com/";
    private static final String SITE_SUFFIX = "page/";


    /**
     * Be polite and don't send requests too often.
     * Waiting period between requests. (in milisec)
     */
    private static final int POLITENESS_INTERVAL = 1200;
    private static final Logger log = Logger.getLogger(CrawlerVSCOM.class);

    /**
     * Main method
     */
    public static void main(String[] args) throws ParseException {
        //Initialization
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        File outputDir = new File(STORAGE);
        if (!outputDir.exists()) {
            boolean mkdirs = outputDir.mkdirs();
            if (mkdirs) {
                log.info("Output directory created: " + outputDir);
            } else {
                log.error("Output directory can't be created! Please either create it or change the STORAGE parameter.\nOutput directory: " + outputDir);
            }
        }

        //HTMLDownloader downloader = new HTMLDownloader();
        AbstractHTMLDownloader downloader = new HTMLDownloaderSelenium();
        Map<String, Map<String, List<String>>> results = new HashMap<>();

        for (String key : xpathMap.keySet()) {
            Map<String, List<String>> map = new HashMap<>();
            results.put(key, map);
        }

        //Collection<String> urlsSet = new ArrayList<String>();
        Collection<String> urlsSet = new HashSet<>();
        Map<String, PrintStream> printStreamMap = new HashMap<>();

        //Try to load links
        File links = new File(STORAGE + "_urls.txt");
        if (links.exists()) {
            try {
                List<String> lines = Utils.readTXTFile(new FileInputStream(links));
                urlsSet.addAll(lines);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            for (int i = 1; i <= MAX_ARTICLE_PAGES; i++) {
                String link = SITE + SITE_SUFFIX + i;
                urlsSet.addAll(downloader.getLinks(link, "//h2[@class='entry-title']/a/@href"));
            }
            Utils.saveFile(new File(STORAGE + Utils.SDF.format(System.currentTimeMillis()) + "_links_size_" + urlsSet.size() + ".txt"), urlsSet);
        }

        for (String key : results.keySet()) {
            if (!objectAttributes.contains(key)){
                File file = new File(STORAGE + "/" + Utils.SDF.format(System.currentTimeMillis()) + "_" + key + ".txt");
                PrintStream printStream = null;
                try {
                    printStream = new PrintStream(new FileOutputStream(file));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                printStreamMap.put(key, printStream);
            }
        }

        int count = 0;
        List<ArticleModel> articles = new ArrayList<>();
        for (String url : urlsSet) {
            String link = url;
            if (!link.contains(SITE)) {
                link = SITE + url;
            }
            //Download and extract data according to xpathMap
            Map<String, List<String>> products = downloader.processUrl(link, xpathMap);
            count++;
            if (count % 100 == 0) {
                log.info(count + " / " + urlsSet.size() + " = " + count / (0.0 + urlsSet.size()) * 100 + "% done.");
            }

            ArticleModel articleModel = new ArticleModel();
            articleModel.setDownloadDate(new Date());
            articleModel.setAuthor(products.get(AUTHOR).get(0));
            articleModel.setCategory(products.get(CATEGORY).get(0));
            articleModel.setTitle(products.get(TITLE).get(0));
            articleModel.setUrl(url);
            articleModel.setPublished(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX").parse(products.get(PUBLISHED).get(0)));
            for (String key : results.keySet()) {
                Map<String, List<String>> map = results.get(key);
                List<String> list = products.get(key);
                if (list != null && !objectAttributes.contains(key)) {
                    map.put(url, list);

                    if (key.equals("allText")){
                        articleModel.setContent(list.get(0));
                    }
                    //log.info(Arrays.toString(list.toArray()));
                    //log.info(url);
                    //print
                    PrintStream printStream = printStreamMap.get(key);
                    for (String result : list) {
                        printStream.println(url + "\t" + result + "\n");
                    }
                }

            }
            articles.add(articleModel);
            try {
                Thread.sleep(POLITENESS_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        saveArticlesModel(articles);

        //close print streams
        for (String key : results.keySet()) {
            PrintStream printStream = printStreamMap.get(key);
            if (printStream != null){
                printStream.close();
            }
        }

        // Save links that failed in some way.
        // Be sure to go through these and explain why the process failed on these links.
        // Try to eliminate all failed links - they consume your time while crawling data.
        reportProblems(downloader.getFailedLinks());
        downloader.emptyFailedLinks();
        log.info("-----------------------------");


//        // Print some information.
//        for (String key : results.keySet()) {
//            Map<String, List<String>> map = results.get(key);
//            Utils.saveFile(new File(STORAGE + "/" + Utils.SDF.format(System.currentTimeMillis()) + "_" + key + "_final.txt"),
//                    map, idMap);
//            log.info(key + ": " + map.size());
//        }
        System.exit(0);
    }


    /**
     * Save file with failed links for later examination.
     *
     * @param failedLinks links that couldn't be downloaded, extracted etc.
     */
    private static void reportProblems(Set<String> failedLinks) {
        if (!failedLinks.isEmpty()) {

            Utils.saveFile(new File(STORAGE + Utils.SDF.format(System.currentTimeMillis()) + "_undownloaded_links_size_" + failedLinks.size() + ".txt"),
                    failedLinks);
            log.info("Failed links: " + failedLinks.size());
        }
    }

    public static void saveArticlesModel(List<ArticleModel> list) {
            File articlesJson = new File(STORAGE + "/" + Utils.SDF.format(System.currentTimeMillis()) + "_articles.json");
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssX"));
            try {
                objectMapper.writeValue(articlesJson, list);
            } catch (IOException e){
                e.printStackTrace();
            }
    }
}
