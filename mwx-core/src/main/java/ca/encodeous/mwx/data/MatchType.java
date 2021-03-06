package ca.encodeous.mwx.data;

public enum MatchType {
    NORMAL("Norm"),
    RANKED("Rank"),
    PRACTICE("Prac");
    private final String text;

    MatchType(final String type) {
        this.text = type;
    }

    @Override
    public String toString() {
        return text;
    }
}
