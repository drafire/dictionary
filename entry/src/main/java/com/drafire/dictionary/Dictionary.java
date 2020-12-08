package com.drafire.dictionary;

import ohos.app.AbilityContext;
import ohos.global.resource.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Dictionary {
    private AbilityContext context;
    private File dictPath;
    private File dbPath;

    public Dictionary(AbilityContext context) {
        this.context = context;
        dictPath = new File(context.getDataDir().toString() + "/MainAbility/databases/db");
        if (!dictPath.exists()) {
            dictPath.mkdirs();
        }
        dbPath = new File(Paths.get(dictPath.toString(), "dict.sqlite").toString());
    }

    private void extractDb() throws IOException {
        //读取
        Resource resource = context.getResourceManager().getRawFileEntry("resources/rawfile/dictionary.txt").openRawFile();
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
    }
}
