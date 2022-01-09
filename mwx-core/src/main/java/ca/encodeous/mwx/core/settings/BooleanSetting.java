package ca.encodeous.mwx.core.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean defaultValue, SettingUpdateEvent<Boolean> update) {
        super(name, defaultValue, update);
    }
}
