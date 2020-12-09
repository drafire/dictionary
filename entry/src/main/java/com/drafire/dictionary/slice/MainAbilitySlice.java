package com.drafire.dictionary.slice;

import com.drafire.dictionary.Dictionary;
import com.drafire.dictionary.ResourceTable;
import com.drafire.dictionary.Word;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;

import java.io.IOException;
import java.util.List;

public class MainAbilitySlice extends AbilitySlice {
    private Dictionary dictionary;
    private TextField textField;
    private Text textResult;
    private Image imageSearch;
    private Image imageLogo;

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
