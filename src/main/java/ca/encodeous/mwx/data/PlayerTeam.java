package ca.encodeous.mwx.data;

public enum PlayerTeam {
    RED,
    GREEN,
    SPECTATOR,
    LOBBY,
    NONE;

    public String getName() {
        return switch (this) {
            case RED -> "&cRed";
            case GREEN -> "&aGreen";
            case SPECTATOR -> "&9Spectator";
            case LOBBY -> "Lobby";
            case NONE -> "None";
        };
    }

    public String getColor() {
        return switch (this) {
            case RED -> "&c";
            case GREEN -> "&a";
            case SPECTATOR -> "&9";
            case LOBBY -> "&7";
            case NONE -> "&f";
        };
    }

    public PlayerTeam getOpponent() {
        return switch (this) {
            case RED -> GREEN;
            case GREEN -> RED;
            default -> throw new IllegalArgumentException("The team " + this + " does not have an opponent.");
        };
    }
}
