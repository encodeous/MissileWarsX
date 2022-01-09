package ca.encodeous.mwx.core.settings;

import ca.encodeous.mwx.core.game.MissileWarsMatch;

public abstract class Setting<T> {

    private final String name;
    private final SettingUpdateEvent<T> updateEvent;
    private T value;

    public Setting(String name, T defaultValue, SettingUpdateEvent<T> update) {
        this.name = name;
        updateEvent = update;
        setValue(defaultValue);
    }

    public String getName() {
        return name;
    }

    public void CallUpdateEvent(MissileWarsMatch match) {
        updateEvent.update(match, this);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T obj) {
        value = obj;
    }

}
