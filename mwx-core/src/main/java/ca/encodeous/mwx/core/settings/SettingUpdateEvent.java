package ca.encodeous.mwx.core.settings;

import ca.encodeous.mwx.core.game.MissileWarsMatch;

@FunctionalInterface
public interface SettingUpdateEvent<T> {

    public void update(MissileWarsMatch match, Setting<T> setting);

}
