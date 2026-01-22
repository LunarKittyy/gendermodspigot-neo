package dbrighthd.wildfiregendermodplugin.wildfire.setup;

public enum UVDirection {
    EAST,
    WEST,
    DOWN,
    UP,
    NORTH;

    public static UVDirection byId(int id) {
        if (id < 0 || id >= values().length) return NORTH;
        return values()[id];
    }
}
