package ca.encodeous.mwx.core.utils;

import ca.encodeous.mwx.core.game.Counter;

public interface Countable {
    public void Count(Counter counter, int count);

    /**
     * Called when the last count is counted
     * @param counter
     */
    public void FinishedCount(Counter counter);
}
