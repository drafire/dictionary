package com.drafire.dictionary.slice;

import com.drafire.dictionary.Dictionary;
import com.drafire.dictionary.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.Text;
import ohos.agp.components.TextField;

import java.io.IOException;

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
            imageSearch.setClickedListener(new Component.ClickedListener() {
                @Override
                public void onClick(Component component) {
                    //隐藏logo
                    imageLogo.setVisibility(Component.HIDE);
                    textResult.setVisibility(Component.VISIBLE);
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
