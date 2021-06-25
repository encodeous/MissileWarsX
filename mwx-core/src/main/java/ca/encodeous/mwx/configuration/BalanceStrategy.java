package ca.encodeous.mwx.configuration;

public enum BalanceStrategy {
    /**
     * Allow players to choose their own teams
     */
    PLAYERS_CHOOSE,
    /**
     * Assign players randomly (only if both teams have equal number of players)
     */
    BALANCED_RANDOM,
    /**
     * Assign players based on their UUID (only if both teams have equal number of players)
     */
    BALANCED_FIXED,
    /**
     * Assign players to green (only if both teams have equal number of players)
     */
    BALANCED_GREEN
}
