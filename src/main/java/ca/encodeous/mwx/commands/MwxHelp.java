package ca.encodeous.mwx.commands;

import ca.encodeous.mwx.util.Msg;
import org.bukkit.command.CommandSender;

final class MwxHelp {
    private MwxHelp() {
    }

    static void send(final CommandSender sender, final boolean isAdmin) {
        sender.sendMessage(Msg.component("&6/mwx start &7- Force start the game"));
        sender.sendMessage(Msg.component("&6/mwx spectate &7- Become a spectator"));
        sender.sendMessage(Msg.component("&6/mwx lobby &7- Return to the lobby"));
        if (!isAdmin) {
            sender.sendMessage(Msg.component("&7(Other subcommands require &e" + "mwx.admin" + "&7)"));
            return;
        }
        sender.sendMessage(Msg.component("&6/mwx end &7- End the current game"));
        sender.sendMessage(Msg.component("&6/mwx reload &7- Reload config"));
        sender.sendMessage(Msg.component("&6/mwx setmap <name> &7- Change map"));
        sender.sendMessage(Msg.component("&6/mwx info &7- Show game info"));
        sender.sendMessage(Msg.component("&6/mwx team <player> <red|green> &7- Debug add player"));
        sender.sendMessage(Msg.component("&6/mwx give [targets] <item|all> [amount] &7- Give items"));
        sender.sendMessage(Msg.component("&6/mwx wipe &7- Wipe the current map"));
    }
}
