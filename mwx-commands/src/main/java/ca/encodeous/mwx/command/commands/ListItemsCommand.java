package ca.encodeous.mwx.command.commands;

import ca.encodeous.mwx.command.MissileWarsCommand;
import ca.encodeous.mwx.command.RootCommand;
import ca.encodeous.mwx.core.game.CoreGame;

import java.util.List;
import java.util.stream.Collectors;

import static ca.encodeous.mwx.command.ExecutionSource.NONE;

public class ListItemsCommand extends MissileWarsCommand {

    @Override
    public RootCommand BuildCommand() {
        return new RootCommand("mwitems", "items").Executes(NONE, context -> {
            List<String> results = CoreGame.Instance.mwConfig.Items.stream()
                    .map(x -> "&cId: "+x.MissileWarsItemId + " - Stack Size: " + x.StackSize + " - Max Stack Size: " + x.MaxStackSize)
                    .collect(Collectors.toList());

            String result = String.join("\n", results);
            context.SendMessage(result);
            return results.size();
        });
    }

    @Override
    public String GetCommandName() {
        return "mwitems";
    }
}
