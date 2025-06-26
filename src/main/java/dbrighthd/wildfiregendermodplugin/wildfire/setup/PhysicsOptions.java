package dbrighthd.wildfiregendermodplugin.wildfire.setup;

/**
 * @author winnpixie
 */
public record PhysicsOptions(boolean breastPhysics,
                             boolean armorPhysics,
                             float buoyancy,
                             float floppiness) {
    public static class Builder {
        private boolean breastPhysics;
        private boolean armorPhysics;
        private float buoyancy;
        private float floppiness;

        public Builder setBreastPhysics(boolean breastPhysics) {
            this.breastPhysics = breastPhysics;
            return this;
        }

        public Builder setArmorPhysics(boolean armorPhysics) {
            this.armorPhysics = armorPhysics;
            return this;
        }

        public Builder setBuoyancy(float buoyancy) {
            this.buoyancy = buoyancy;
            return this;
        }

        public Builder setFloppiness(float floppiness) {
            this.floppiness = floppiness;
            return this;
        }

        public PhysicsOptions create() {
            return new PhysicsOptions(breastPhysics, armorPhysics, buoyancy, floppiness);
        }
    }
}
