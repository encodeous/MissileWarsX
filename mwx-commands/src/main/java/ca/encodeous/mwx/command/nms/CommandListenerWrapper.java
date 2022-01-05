package ca.encodeous.mwx.command.nms;

import ca.encodeous.simplenms.NMSProxy;
import ca.encodeous.simplenms.annotations.NMSClass;
import ca.encodeous.simplenms.annotations.NMSField;
import ca.encodeous.simplenms.annotations.NMSMethod;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

@NMSClass(type = NMSClass.NMSType.NMS, value = "commands.CommandListenerWrapper")
public interface CommandListenerWrapper extends NMSProxy {
    @NMSMethod
    public World getBukkitWorld();
    @NMSMethod
    public Location getBukkitLocation();
    @NMSMethod
    public boolean hasPermission(int level, String bukkitPerm);
    @NMSMethod
    public CommandSender getBukkitSender();
}
