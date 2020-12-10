package com.drafire.dictionary.slice;

import com.drafire.dictionary.Dictionary;
import com.drafire.dictionary.ResourceTable;
import com.drafire.dictionary.SearchWordCallback;
import com.drafire.dictionary.Word;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;

import java.io.IOException;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {
    private Dictionary dictionary;
    private TextField textField;
    private Text textResult;
    private Image imageSearch;
    private Image imageLogo;
    private static final int SEARCH_RESULT = 100;

    private class SearchWordCallbackImpl implements SearchWordCallback {

        @Override
        public void onResult(List<Word> list) {
            //得到当前的线程的runner
            EventRunner eventRunner=EventRunner.getMainEventRunner();
            DictionaryEvenHandler dictionaryEvenHandler=new DictionaryEvenHandler(eventRunner,list);
            dictionaryEvenHandler.sendEvent(SEARCH_RESULT);
            eventRunner=null;   //释放对象空间
        }
    }

    //当前的Ability，只能在当前类处理
    private class DictionaryEvenHandler extends EventHandler {
        private List<Word> list;

        public DictionaryEvenHandler(EventRunner runner, List<Word> list) throws IllegalArgumentException {
            super(runner);
            this.list = list;
        }

        @Override
        protected void processEvent(InnerEvent event) {
            if (null == event) {
                return;
            }

            int eventId = event.eventId;
            switch (eventId) {
                case SEARCH_RESULT: {
                    if (null == list || list.size() == 0) {
                        textResult.setText("查不都到该单词，请检查是否输入错误");
                        return;
                    }
                    textResult.setText("");
                    for (Word word : list) {
                        textResult.append(word.getType() + " " + word.getMeanings() + "\r\n");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);

        dictionary = new Dictionary(this);
        try {
            dictionary.init();
        } catch (IOException e) {
            e.printStackTrace();
            terminateAbility();
        }

        textField = (TextField) findComponentById(ResourceTable.Id_text);
        textResult = (Text) findComponentById(ResourceTable.Id_textResult);
        imageSearch = (Image) findComponentById(ResourceTable.Id_btnSearch);
        imageLogo = (Image) findComponentById(ResourceTable.Id_image);
        if (null != imageSearch) {
            imageSearch.setClickable(true);
            imageSearch.setClickedListener(component -> {
                //隐藏logo
                imageLogo.setVisibility(Component.HIDE);
                textResult.setVisibility(Component.VISIBLE);
                List<Word> list = dictionary.search(textField.getText());
                if (null != list && !list.isEmpty()) {
                    textResult.setText(null);
                    for (Word word : list) {
                        //使用append，可以解析html代码
                        textResult.append(word.getType() + " " + word.getMeanings() + "\r\n");
                    }
                } else {
                    textResult.setText("本地词库没有该单词，正在连接网络查询...");
                    dictionary.serachWebDict(textField.getText(),new SearchWordCallbackImpl());
                }
            });

        }
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
