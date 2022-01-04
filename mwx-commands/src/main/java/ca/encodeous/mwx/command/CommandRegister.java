package ca.encodeous.mwx.command;

import ca.encodeous.mwx.core.utils.Utils;
import org.bukkit.entity.Player;

public class CommandRegister {
    public CommandRegister(String name, String help, boolean isRestricted) {
        Help = help;
        Name = name;
        this.isRestricted = isRestricted;
    }

    public void Create(RegisterCallback callback){
        CommandCore.Instance.CreateCommand(
                callback.run(
                        CommandCore.Instance.GetCommand(Name)
                                .withShortDescription(Help))
                        .withRequirement(cs->{
                            if(isRestricted){
                                if(cs instanceof Player p){
                                    return Utils.CheckPrivPermissionSilent(p);
                                }
                            }
                            return true;
                        })
        );
        CommandCore.Instance.AddInfo(this);
    }

    public String Help, Name;
    boolean isRestricted;
}
