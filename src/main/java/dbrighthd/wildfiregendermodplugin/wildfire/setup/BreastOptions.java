package dbrighthd.wildfiregendermodplugin.wildfire.setup;

/**
 * @author winnpixie
 */
public record BreastOptions(float bustSize,
                            float xOffset,
                            float yOffset,
                            float zOffset,
                            boolean uniBoob,
                            float cleavage) {
    public static class Builder {
        private float bustSize;
        private float xOffset;
        private float yOffset;
        private float zOffset;
        private boolean uniBoob;
        private float cleavage;

        public Builder setBustSize(float bustSize) {
            this.bustSize = bustSize;
            return this;
        }

        public Builder setXOffset(float xOffset) {
            this.xOffset = xOffset;
            return this;
        }

        public Builder setYOffset(float yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Builder setZOffset(float zOffset) {
            this.zOffset = zOffset;
            return this;
        }

        public Builder setUniBoob(boolean uniBoob) {
            this.uniBoob = uniBoob;
            return this;
        }

        public Builder setCleavage(float cleavage) {
            this.cleavage = cleavage;
            return this;
        }

        public BreastOptions create() {
            return new BreastOptions(bustSize, xOffset, yOffset, zOffset, uniBoob, cleavage);
        }
    }
}
