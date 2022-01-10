package ca.encodeous.mwx.core.settings;

import ca.encodeous.mwx.core.game.MissileWarsMatch;

@FunctionalInterface
public interface SettingUpdateEvent<T> {

    public boolean update(MissileWarsMatch match, T newValue);

}
