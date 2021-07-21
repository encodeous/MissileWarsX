package ca.encodeous.mwx.configuration;

public enum MissileWarsCoreItem {
    GUNBLADE("Gunblade"), SHIELD("Shield"), ARROW("Arrow"), FIREBALL("Fireball");
    private final String id;
    MissileWarsCoreItem(String id) {
        this.id = id;
    }
    public String getValue() {
        return id;
    }
    public String toString(){
        return id;
    }
}
