package com.drafire.dictionary.slice;

import com.drafire.dictionary.Dictionary;
import com.drafire.dictionary.ResourceTable;
import com.drafire.dictionary.Word;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Text;

import java.util.List;

public class WatchSearchResultSlice extends AbilitySlice {
    private Text text;

    @Override
    protected void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_watch_search_result);
        //要先setUIContent，才能通过findComponentById找到对应的标签
        //不同的layout，可以有相同的id，无妨
        text = (Text) findComponentById(ResourceTable.Id_text);

        Object dictionaryResult = intent.getParams().getParam("dictionaryResult");
        if (null != dictionaryResult) {
            List<Word> list = (List<Word>) dictionaryResult;
            if (null != list && !list.isEmpty()) {
                if (null != text) {
                    text.setText("");
                    for (int i = 0; i < list.size(); i++) {
                        text.append(list.get(i).getType() + " " + list.get(i).getMeanings() + "\r\n");
                    }
                }
            }
        } else {
            text.setText("本地词库没有该单词，正在连接网络查询...");
        }
    }
}
