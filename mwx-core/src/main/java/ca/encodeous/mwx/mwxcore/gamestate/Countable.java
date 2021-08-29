package ca.encodeous.mwx.mwxcore.gamestate;

public interface Countable {
    public void Count(Counter counter, int count);

    /**
     * Called when the last count is counted
     * @param counter
     */
    public void FinishedCount(Counter counter);
}
