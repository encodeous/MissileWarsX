package ca.encodeous.mwx.core.lang;

import ca.encodeous.mwx.core.utils.Chat;

public class Strings {
    public static String MISSILE_WARS_BRAND = Chat.FCL("&c&lMissile&f&lWars");

    public static String TABLIST_HEADER = Chat.FCL("&f&lWelcome to &c&lMissile&f&lWars&6&lX&f&l.");
    public static String TABLIST_FOOTER = Chat.FCL("&fUse &6/lobby &fto navigate between lobbies, and &6/players &fto see all players.\n" +
            "&e&oHelp contribute to MissileWarsX at &6https://git.io/missilewars&e!");
    // Change this if you want to
    public static String JOIN_MESSAGE = Chat.FCL("&f&lWelcome to the &c&lMissile&f&lWars&6&lX &f&ldevelopment and test server!\n" +
            "&eCheck out the MissileWarsX project on GitHub at &6https://git.io/missilewars");
    public static String LEAVE_GAME = Chat.FCL("%s&r has left the game.");
    public static String ENTER_GAME = Chat.FCL("&cYou have entered the game, type &6/lobby &cto return to the lobby.");
    public static String PLAYER_JOIN_TEAM = Chat.FCL("%s &fhas joined the %s &rteam!");
    public static String PLAYER_LEAVE_TEAM = Chat.FCL("%s &fhas left the %s &rteam.");
    public static String PLAYER_JOIN_LOBBY = Chat.FCL("%s &fhas joined the lobby!");
    public static String PLAYER_STOP_SPECTATE = Chat.FCL("%s &fstopped &9spectating.");
    public static String PLAYER_SPECTATE = Chat.FCL("%s &fis now &9spectating.");
    public static String PLAYER_SPECTATE_NOTIF = Chat.FCL("&6You are now &espectating&6. Type &e/lobby &6to return to the lobby.");
    public static String STARTING_GAME_PLURAL = Chat.FCL("&aStarting game in &6%s &aseconds!");
    public static String STARTING_GAME = Chat.FCL("&aStarting game in &6%s &asecond!");
    public static String RESETTING_GAME_PLURAL = Chat.FCL("&aResetting game in &6%s &aseconds!");
    public static String RESETTING_GAME = Chat.FCL("&aResetting game in &6%s &asecond!");
    public static String STARTING_NOW = Chat.FCL("&cStarting now!");
    public static String WIN_GAME_CREDITED = Chat.FCL("&fThe %s &fteam's portal was blown up by %s&r!");
    public static String WIN_GAME = Chat.FCL("&fThe %s &fteam's portal was blown up!");
    public static String CONGRATULATE_WIN = Chat.FCL("&6Congratulations %s &6team!");
    public static String CONGRATULATE_WIN_TITLE = Chat.FCL("&6The %s &6team has won!");
    public static String GAME_DRAW = Chat.FCL("&6The game has ended in a draw!");
    public static String PING_MESSAGE = Chat.FCL("&6Your ping is &2%s &6ms.");
    public static String PING_OTHER_MESSAGE = Chat.FCL("&e%s&6's ping is &2%s &6ms.");
    public static String PLAYERS_ONLINE = Chat.FCL("&2Currently there is/are %s player(s) online.\n");
    public static String AND = "and";
    public static String CONGRATULATIONS = "Congratulations";
    public static String NOT_PLAYER = "You are not a player...";
    public static String MORE_PLAYERS = Chat.FCL("&7%s more players...");

    public static String CANNOT_DEPLOY = Chat.FCL("&cYou cannot deploy that there.");

    public static String RANKED_PLAYER_LEAVE = Chat.FCL("&cA player has left your team, please run &6/ready &cagain when everyone is ready.");
    public static String RANKED_TEAM_READY = Chat.FCL("&fThe %s &fteam is now ready.");
    public static String RANKED_TEAM_READY_NOTIF = Chat.FCL("&cYour team is now ready. &6Once the game is started, the teams will be locked and rankings will be calculated when the game ends.");
    public static String RANKED_LOBBY_NOT_ENOUGH_PLAYERS = Chat.FCL("&9The game needs at least 1 player in each team to start.");
    public static String RANKED_LOBBY_TEAM_WARNING = Chat.FCL("&aBoth teams are now ready. Once the timer reaches 0, the teams will be locked-in and no changes will be allowed.");
    public static String RANKED_LOBBY_READY = Chat.FCL("&9Both teams need to run &6/ready &9to start the game.");
    public static String RANKED_PLAYER_JOIN = Chat.FCL("&cA player has joined the team, please run &6/ready &cwhen everyone is ready.");
    public static String RANKED_LOCKED = Chat.FCL("&cYou cannot join this game because the teams are locked!");

    public static String PRACTICE_INFO = Chat.FCL("&6Welcome to a Practice Lobby!\n" +
            " &7- &6You will be able to enter creative mode using &e/mode&6.\n" +
            " &7- &6Give yourself any item with &e/mwgive&6.\n" +
            " &7- &6Reset the map with &e/wipe&6.\n" +
            " &7- &6Match Statistics will not be saved.\n" +
            "&7&oHave fun!");

    public static String GAME_RESET = Chat.FCL("&9The game has been reset.");
    public static String MAP_WIPED = Chat.FCL("&9The map has been wiped!");
    public static String LOBBY_CLOSE = Chat.FCL("&cLobby Has Closed");
    public static String TEAM_FULL = Chat.FCL("&cThis team is full!");
    public static String GAME_FULL = Chat.FCL("&cThis game is full!");
    public static String KICK_RESET = Chat.FCL("&6You have been kicked from this server because the map is resetting.");
    public static String CLEARING_MAP = Chat.FCL("&9Clearing map!");

    public static String INVENTORY_FULL = Chat.FCL("&cYour inventory does not have enough space to receive items.");
    public static String ITEM_NOT_GIVEN = Chat.FCL("&6You already have a &f%s&6.");
    public static String GAMEMODE_UPDATED = Chat.FCL("&6Your gamemode has been changed to &e%s&6.");
    public static String DRAW_CHECK = Chat.FCL("&f&lChecking for draw...");

    public static String IN_GAME_COMMAND = Chat.FCL("&cYou must be in the game to execute this command!");
    public static String NO_PERMISSION = Chat.FCL("&cYou do not have permission to execute this command!");
    public static String RANKED_PERM_DENIED = Chat.FCL("&cYou are not allowed to execute this command in a ranked match!");
    public static String LOBBY_COMMAND = Chat.FCL("&cThis command must be executed in a lobby.");

    public static String LOBBY_NOT_ENOUGH_PLAYERS = Chat.FCL("&9The game needs at least 1 player in each team to start. To forcefully start a game, run &6/start&9.");
}
