package dev.thomasglasser.mineraculous.impl.plugins.voicechat;

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.VoiceDistanceEvent;
import dev.thomasglasser.mineraculous.api.MineraculousConstants;
import dev.thomasglasser.mineraculous.api.world.attachment.MineraculousAttachmentTypes;
import dev.thomasglasser.mineraculous.api.world.level.storage.AbilityEffectData;
import dev.thomasglasser.tommylib.api.client.ClientUtils;
import net.minecraft.world.entity.player.Player;

@ForgeVoicechatPlugin
public class MineraculousVoiceChatPlugin implements VoicechatPlugin {
    @Override
    public String getPluginId() {
        return MineraculousConstants.MOD_ID;
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, event -> {
            if (!AbilityEffectData.isInPrivateChat(ClientUtils.getLocalPlayer(), event.getEntityId())) {
                System.out.println("Cancelled!");
                event.setRawAudio(new short[] {});
            }
        });
        registration.registerEvent(VoiceDistanceEvent.class, event -> {
            if (event.getSenderConnection().getPlayer().getPlayer() instanceof Player player && player.getData(MineraculousAttachmentTypes.ABILITY_EFFECTS).privateChat().isPresent()) {
                System.out.println("Boom!");
                event.setDistance(Float.MAX_VALUE);
            }
        });
    }
}
