package ca.encodeous.mwx.core.settings;

import java.util.List;

public interface SettingsManager {

    public List<Setting<?>> getSettings();
    public Setting<?> getSetting(String name);

    public BooleanSetting getBooleanSetting(String name);
    public IntegerSetting getIntegerSetting(String name);
    public DoubleSetting getDoubleSetting(String name);

}
