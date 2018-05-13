package com.ctt.format.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jdom.Element;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;
/**
 * Created by Administrator on 2018/5/11 0011.
 */

@State(
        name = "FormatSetting",
        storages = {@Storage(
                id = "other",
                file = "$APP_CONFIG$/format.xml"
        )}
)
public class FormatSetting implements PersistentStateComponent<Element> {
    private String setFormat;

    private String getFormat;

    public FormatSetting() {
    }

    public static FormatSetting getInstance() {
        return (FormatSetting) ServiceManager.getService(FormatSetting.class);
    }

    @Nullable
    public Element getState() {
        Element element = new Element("FormatSetting");
        element.setAttribute("setFormat", this.getSetFormat());
        element.setAttribute("getFormat", this.getGetFormat());
        return element;
    }

    public void loadState(Element state) {
        this.setSetFormat(state.getAttributeValue("setFormat"));
        this.setGetFormat(state.getAttributeValue("getFormat"));
    }

    public String getSetFormat() {
        return this.setFormat == null ? StatementGenerator.defaultSetFormat : this.setFormat;
    }

    public void setSetFormat(String setFormat) {
        this.setFormat = setFormat;
    }

    public String getGetFormat() {
        return this.getFormat == null ? StatementGenerator.defaultGetFormat : this.getFormat;
    }

    public void setGetFormat(String getFormat) {
        this.getFormat = getFormat;
    }
}
