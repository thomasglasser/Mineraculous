package dev.thomasglasser.mineraculous.impl.client.gui.kamiko;

import com.mojang.authlib.GameProfile;
import dev.thomasglasser.mineraculous.api.client.gui.selection.PlayerMenuItem;
import dev.thomasglasser.mineraculous.api.client.gui.selection.SelectionMenu;
import dev.thomasglasser.mineraculous.impl.client.MineraculousClientUtils;
import dev.thomasglasser.mineraculous.impl.network.ServerboundSetPlayerAttackTargetPayload;
import dev.thomasglasser.mineraculous.impl.world.entity.Kamiko;
import dev.thomasglasser.tommylib.api.platform.TommyLibServices;
import java.util.Optional;

public class KamikoPlayerMenuItem extends PlayerMenuItem {
    public KamikoPlayerMenuItem(GameProfile profile) {
        super(profile);
    }

    @Override
    public void selectItem(SelectionMenu menu) {
        if (MineraculousClientUtils.getCameraEntity() instanceof Kamiko kamiko) {
            TommyLibServices.NETWORK.sendToServer(new ServerboundSetPlayerAttackTargetPayload(kamiko.getId(), Optional.of(id)));
        }
    }
}
