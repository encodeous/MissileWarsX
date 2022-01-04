package ca.encodeous.mwx.engines.command;

import org.bukkit.entity.Player;

public abstract class CommandBase {
    public abstract void Initialize();
    public abstract void Disable();
    public abstract void RegisterLobbies();
    public abstract void UpdatePlayer(Player p);
}
