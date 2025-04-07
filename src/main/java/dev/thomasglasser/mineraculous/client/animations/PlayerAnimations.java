package dev.thomasglasser.mineraculous.client.animations;

import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

public class PlayerAnimations {
    private static final List<KeyframeAnimation> CAT_STAFF_PERCH_PRESS_BUTTON = (List<KeyframeAnimation>) new InputStreamReader(Objects.requireNonNull(PlayerAnimations.class.getClassLoader().getResourceAsStream("assets/minejago/animations/player/spinjitzu.animation.json")));

    public enum CatStaffPerchStart {
        START(CAT_STAFF_PERCH_PRESS_BUTTON.getFirst()),
        FLOAT(CAT_STAFF_PERCH_PRESS_BUTTON.get(1)),
        FINISH(CAT_STAFF_PERCH_PRESS_BUTTON.get(2));

        private final KeyframeAnimation animation;

        CatStaffPerchStart(KeyframeAnimation animation) {
            this.animation = animation;
        }

        public KeyframeAnimation getAnimation() {
            return animation;
        }
    }
}
