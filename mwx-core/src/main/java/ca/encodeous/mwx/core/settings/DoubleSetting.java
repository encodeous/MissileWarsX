package ca.encodeous.mwx.core.settings;

public class DoubleSetting extends Setting<Double> {
    public DoubleSetting(String name, double defaultValue, SettingUpdateEvent<Double> update) {
        super(name, defaultValue, update);
    }
}
