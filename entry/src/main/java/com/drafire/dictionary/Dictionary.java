package com.drafire.dictionary;

import ohos.app.AbilityContext;
import ohos.data.DatabaseHelper;
import ohos.data.rdb.RdbOpenCallback;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.StoreConfig;
import ohos.data.resultset.ResultSet;
import ohos.global.resource.Resource;

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
        if (dbPath.exists()) {
            dbPath.delete();
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
}
