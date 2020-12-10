package com.drafire.dictionary;

import ohos.app.AbilityContext;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.Resource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Dictionary {
    private AbilityContext context;
    private File dictPath;
    private File dbPath;

    private RdbStore rdbStore;
    private StoreConfig storeConfig = StoreConfig.newDefaultConfig("dictionary.db");
    private static final RdbOpenCallback callback = new RdbOpenCallback() {
        @Override
        public void onCreate(RdbStore rdbStore) {

        }

        @Override
        public void onUpgrade(RdbStore rdbStore, int i, int i1) {

        }
    };

    public Dictionary(AbilityContext context) {
        this.context = context;
        dictPath = new File(context.getDataDir().toString() + "/MainAbility/databases/db");
        if (!dictPath.exists()) {
            dictPath.mkdirs();
        }
        dbPath = new File(Paths.get(dictPath.toString(), "dictionary.db").toString());
    }

    private void extractDb() throws IOException {
        //读取
        Resource resource = context.getResourceManager().getRawFileEntry("resources/rawfile/dictionary.db").openRawFile();
        //这里修改下，如果已经有数据库，就不再导入
        if (dbPath.exists()) {
            return;
            //dbPath.delete();
        }

        FileOutputStream outputStream = new FileOutputStream(dbPath);
        byte[] buff = new byte[4 * 1024];
        int count = 0;
        while ((count = resource.read(buff)) > 0) {
            outputStream.write(buff, 0, count);
        }
        resource.close();
        outputStream.close();
    }

    public void init() throws IOException {
        extractDb();
        //打开数据库
        DatabaseHelper helper = new DatabaseHelper(context);
        rdbStore = helper.getRdbStore(storeConfig, 1, callback);
    }

    public List<Word> search(String word) {
        //先把单词变成小写
        word = word.toLowerCase();
        String[] params = new String[]{word};
        ResultSet resultSet = rdbStore.querySql("select * from t_dictionary where word =?", params);
        List<Word> list = new ArrayList<>();
        while (resultSet.goToNextRow()) {
            Word currentWord = new Word();
            currentWord.setMeanings(resultSet.getString(2));
            currentWord.setType(resultSet.getString(3));
            list.add(currentWord);
        }
        //关掉数据库
        resultSet.close();
        return list;
    }

    //异步查询网络
    public void serachWebDict(String word, SearchWordCallback callback) {
        word = word.toLowerCase();
        new AsnycSearchWord(word, rdbStore, callback).start();
    }
}

class AsnycSearchWord extends Thread {
    private String word;
    private RdbStore rdbStore;
    private SearchWordCallback callback;

    public AsnycSearchWord(String word, RdbStore rdbStore, SearchWordCallback callback) {
        this.word = word;
        this.callback = callback;
        this.rdbStore = rdbStore;
    }

    @Override
    public void run() {
        // 获取结果的html形式
        try {
            //这里要使用https。使用http的时候，会报permission 的问题，为什么呢？
            Document document = Jsoup.connect("https://www.iciba.com/word?w=" + this.word).get();
            Elements ulElements = document.getElementsByClass("Mean_part__1RA2V");
            if (null == ulElements || ulElements.size() == 0) {
                callback.onResult(null);
                return;
            }

            List<Word> list = new ArrayList<>();
            //只有一个ul
            Element ulElement = ulElements.get(0);

            String insertWord = "insert into t_dictionary(word,type,meanings) values(?,?,?)";
            //每个li都是一个词性
            Elements liElements = ulElement.getElementsByTag("li");
            for (Element liElement : liElements) {
                Word word = new Word();
                //词性
                Elements iElements = liElement.getElementsByTag("i");
                word.setType(iElements.get(0).text());
                //词义
                Elements divElements = liElement.getElementsByTag("div");
                Element divElement = divElements.get(0);

                Elements spanElements = divElement.getElementsByTag("span");
                String meanings = "";
                //同一个词性，只返回3个意义
                int length = spanElements.size() > 3 ? 3 : spanElements.size();
                for (int i = 0; i < length; i++) {
                    meanings += spanElements.get(i).text();
                }
                word.setMeanings(meanings);
                //写入数据库
                String[] args = new String[]{this.word, word.getType(), word.getMeanings()};
                this.rdbStore.executeSql(insertWord, args);
                //返回结果
                list.add(word);
            }


            //写入回调数据
            if (null != callback) {
                callback.onResult(list);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
