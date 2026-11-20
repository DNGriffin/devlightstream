package com.limelight.binding.input;

/** Radial deadzone -> full remaining travel -> curve -> [minimum, maximum]. */
final class StickResponse {
    // Matches physical/USB normalization in ControllerHandler; OSC may reach +/-32768.
    static final int FULL_SCALE = 0x7FFE;
    private final double deadzone, minimum, maximum;
    private final String curve;

    StickResponse(int deadzone, int minimum, int maximum, String curve) {
        this.deadzone = Math.max(0, Math.min(99, deadzone)) / 100.0;
        this.maximum = Math.max(0, Math.min(100, maximum)) / 100.0;
        this.minimum = Math.max(0, Math.min(this.maximum, minimum / 100.0));
        this.curve = curve == null ? "linear" : curve;
    }

    // Packs two signed shorts without allocating on every input event.
    int apply(short x, short y) {
        double magnitude = Math.hypot((double) x, (double) y) / FULL_SCALE;
        if (magnitude <= deadzone || maximum == 0) {
            return 0;
        }
        double t = (Math.min(1.0, magnitude) - deadzone) / (1.0 - deadzone);
        switch (curve) {
            case "quadratic": t *= t; break;
            case "cubic": t = t * t * t; break;
            case "sqrt": t = Math.sqrt(t); break;
            case "smoothstep": t = t * t * (3 - 2 * t); break;
            default: break; // Unknown persisted curves safely fall back to linear.
        }
        double output = minimum + (maximum - minimum) * t;
        double factor = output / magnitude;
        short outX = (short) Math.round(x * factor);
        short outY = (short) Math.round(y * factor);
        return ((outX & 0xffff) << 16) | (outY & 0xffff);
    }

    static short x(int result) { return (short) (result >>> 16); }
    static short y(int result) { return (short) result; }
}
