package dbrighthd.wildfiregendermodplugin.wildfire.setup;

import java.util.EnumMap;
import java.util.Map;

public class UVLayout {
    private final Map<UVDirection, UVQuad> quads;

    public UVLayout() {
        this.quads = new EnumMap<>(UVDirection.class);
    }

    public UVLayout(Map<UVDirection, UVQuad> quads) {
        this.quads = new EnumMap<>(quads);
    }

    public Map<UVDirection, UVQuad> getQuads() {
        return quads;
    }

    public void setQuad(UVDirection direction, UVQuad quad) {
        quads.put(direction, quad);
    }
}
