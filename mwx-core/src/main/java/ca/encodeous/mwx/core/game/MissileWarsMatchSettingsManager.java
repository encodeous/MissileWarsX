package ca.encodeous.mwx.core.game;

import ca.encodeous.mwx.core.settings.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MissileWarsMatchSettingsManager implements SettingsManager {

    private final ArrayList<Setting<?>> settings = new ArrayList<>();

    public MissileWarsMatchSettingsManager() {
        settings.add(new BooleanSetting("NoHitDelay", false, (match, newValue) -> {match.UpdatePlayers(); return true;}));
        settings.add(new BooleanSetting("DisableItemLimit", false, (match, newValue) -> true));
        settings.add(new IntegerSetting("ItemDelay", CoreGame.Instance.mwConfig.ResupplySeconds, (match, newValue) -> newValue > 0));
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    @Override
    public Setting<?> getSetting(String name) {
        for(Setting<?> setting : settings) {
            if(setting.getName().equals(name)) {
                return setting;
            }
        }
        return null;
    }

    @Override
    public BooleanSetting getBooleanSetting(String name) {
        Setting<?> setting = getSetting(name);
        if(setting instanceof BooleanSetting s) return s;
        return null;
    }

    @Override
    public IntegerSetting getIntegerSetting(String name) {
        Setting<?> setting = getSetting(name);
        if(setting instanceof IntegerSetting s) return s;
        return null;
    }

    @Override
    public DoubleSetting getDoubleSetting(String name) {
        Setting<?> setting = getSetting(name);
        if(setting instanceof DoubleSetting s) return s;
        return null;
    }

}
