package dev.thomasglasser.mineraculous.client.animations;

public class MineraculousPlayerAnimations {
    public static final String NULL = "null";
    public static final String CAT_STAFF_PERCH_START = "perch_press_button";
    public static final String CAT_STAFF_PERCH_ROTATION_LEFT = "perch_rotation_left";
    public static final String CAT_STAFF_PERCH_ROTATION_BACK = "perch_rotation_back";
    public static final String CAT_STAFF_PERCH_ROTATION_RIGHT = "perch_rotation_right";
    public static final String CAT_STAFF_PERCH_ROTATION_FRONT = "perch_rotation_front";

    public static int lastFrame(String anim) {
        switch (anim) {
            case (CAT_STAFF_PERCH_START) -> {
                return 5;
            }
            case (CAT_STAFF_PERCH_ROTATION_FRONT), (CAT_STAFF_PERCH_ROTATION_LEFT), (CAT_STAFF_PERCH_ROTATION_RIGHT), (CAT_STAFF_PERCH_ROTATION_BACK) -> {
                return 7;
            }
            default -> {
                return 1;
            }
        }
    }
}
