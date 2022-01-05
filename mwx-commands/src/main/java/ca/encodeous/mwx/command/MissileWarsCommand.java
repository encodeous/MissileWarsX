package ca.encodeous.mwx.command;

public abstract class MissileWarsCommand {

    public abstract RootCommand BuildCommand();
    public abstract String GetCommandName();

}
