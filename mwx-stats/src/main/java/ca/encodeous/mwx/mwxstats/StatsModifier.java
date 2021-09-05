package ca.encodeous.mwx.mwxstats;

public interface StatsModifier<T> {
    public void run(T value);
}
