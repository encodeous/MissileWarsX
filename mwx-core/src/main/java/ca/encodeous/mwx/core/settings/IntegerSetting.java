package ca.encodeous.mwx.core.settings;

public class IntegerSetting extends Setting<Integer> {
    public IntegerSetting(String name, int defaultValue, SettingUpdateEvent<Integer> update) {
        super(name, defaultValue, update);
    }
}
