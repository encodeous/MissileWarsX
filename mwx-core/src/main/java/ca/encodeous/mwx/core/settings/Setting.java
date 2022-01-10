package ca.encodeous.mwx.core.settings;

import ca.encodeous.mwx.core.game.MissileWarsMatch;

public abstract class Setting<T> {

    private final String name;
    private final SettingUpdateEvent<T> updateEvent;
    private T value;

    public Setting(String name, T defaultValue, SettingUpdateEvent<T> update) {
        this.name = name;
        updateEvent = update;
        value = defaultValue;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public boolean setValue(T obj, MissileWarsMatch match) {
        if(updateEvent.update(match, obj)) {
            value = obj;
            return true;
        }
        return false;
    }

}
