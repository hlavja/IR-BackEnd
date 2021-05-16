package cz.zcu.kiv.nlp.ir.trec.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.zcu.kiv.nlp.ir.trec.data.ArticleRepository;
import cz.zcu.kiv.nlp.ir.trec.dtos.ArticleModel;
import cz.zcu.kiv.nlp.ir.trec.indexing.InvertedList;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Utils for I/O operations
 * Created by Tigi on 22.9.2014.
 */
public class Utils {
    public static final java.text.DateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");

    /**
     * Import articles from file.
     *
     * @param fileName path to articles json file
     * @return list of loaded articles
     */
    public static List<ArticleModel> importArticlesFromFile(String fileName) {
        try {
            return Arrays.asList(new ObjectMapper().readValue(Paths.get(fileName).toFile(), ArticleModel[].class));
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Read txt file with splitting by lines and return set with lines string.
     * @param filePath path to file
     * @return set of lines
     */
    public static Set<String> readTXTFile(String filePath) {
        Set<String> stopWords = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(stopWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stopWords;
    }

    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return list of lines
     */
    public static List<String> readLines(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            List<String> result = new ArrayList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }
            inputStream.close();
            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return text
     */
    public static String readFile(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            inputStream.close();
            return sb.toString().trim();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Saves lines from the list into given file; each entry is saved as a new line.
     *
     * @param file file to save
     * @param list lines of text to save
     */
    public static void saveFile(File file, Collection<String> list) {
        try (PrintStream printStream = new PrintStream(new FileOutputStream(file), true, StandardCharsets.UTF_8)) {
            for (String text : list) {
                printStream.println(text);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves lines from the list into given file; each entry is saved as a new line.
     *
     * @param file file to save
     * @param text text to save
     */
    public static void saveFile(File file, String text) {
        try (PrintStream printStream = new PrintStream(new FileOutputStream(file), true, StandardCharsets.UTF_8)) {
            printStream.println(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Save in memory index to file
     * @param index index to save
     * @param path filename of file to save index
     * @return if succeeded true, than false
     */
    public static boolean saveIndex(InvertedList index, String path) {
        File file = new File(path);
        try {
            Files.deleteIfExists(file.toPath());
            if (file.createNewFile()) {
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                objectOutputStream.writeObject(index);
                objectOutputStream.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method for loading index from file
     * @param path filename of stored index
     * @return loaded index or null if not loaded
     */
    public static InvertedList loadIndex(String path) {
        Object invertedList;
        try {
            File file = new File(path);
            if (file.isFile()) {
                final ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                invertedList = objectInputStream.readObject();
                objectInputStream.close();
                return (InvertedList) invertedList;
            } else {
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save in memory repository to file
     * @param repository repository to save
     * @param path path of file
     * @return if succeeded true, than false
     */
    public static boolean saveRepo(HashMap<Integer, ArticleModel> repository, String path) {
        File file = new File(path);
        try {
            Files.deleteIfExists(file.toPath());
            if (file.createNewFile()) {
                final ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(file));
                objectOutputStream.writeObject(repository);
                objectOutputStream.close();
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method for loading repository from file
     * @param path filename of stored repository
     * @return loaded repository or null if not loaded
     */
    public static HashMap<Integer, ArticleModel> loadRepository(String path) {
        Object repository;
        try {
            File file = new File(path);
            if (file.isFile()) {
                final ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                repository = objectInputStream.readObject();
                objectInputStream.close();
                return (HashMap<Integer, ArticleModel>) repository;
            } else {
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read lines from the stream; lines are trimmed and empty lines are ignored.
     *
     * @param inputStream stream
     * @return list of lines
     */
    public static List<String> readTXTFile(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Cannot locate stream");
        }
        try {
            List<String> result = new ArrayList<String>();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }
            inputStream.close();

            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
