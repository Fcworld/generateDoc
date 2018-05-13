package com.ctt.format.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by admin on 2016/12/18.
 */
public class FormatConfigurable implements SearchableConfigurable {

    private FormatForm formatForm;
    private FormatSetting formatSetting = FormatSetting.getInstance();

    public FormatConfigurable() {
    }

    @NotNull
    public String getId() {
        return "DocFormat";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    public String getDisplayName() {
        return this.getId();
    }

    @Nullable
    public String getHelpTopic() {
        return this.getId();
    }

    @Nullable
    public JComponent createComponent() {
        if(null == this.formatForm) {
            this.formatForm = new FormatForm();
        }

        return this.formatForm.mainPanel;
    }

    public boolean isModified() {
        return !this.formatSetting.getGetFormat().equals(this.formatForm.getFormatTextArea.getText()) || !this.formatSetting.getSetFormat().equals(this.formatForm.setFormatTextArea.getText());
    }

    public void apply() throws ConfigurationException {
        this.formatSetting.setGetFormat(this.formatForm.getFormatTextArea.getText());
        this.formatSetting.setSetFormat(this.formatForm.setFormatTextArea.getText());
    }

    public void reset() {
        this.formatForm.getFormatTextArea.setText(this.formatSetting.getGetFormat());
        this.formatForm.setFormatTextArea.setText(this.formatSetting.getSetFormat());
    }

    @Override
    public void disposeUIResources() {

    }

}
