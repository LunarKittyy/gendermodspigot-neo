package dbrighthd.wildfiregendermodplugin.wildfire.setup;

public record UVLayouts(Layer skin, Layer overlay) {
    public record Layer(UVLayout left, UVLayout right) {
    }
}
