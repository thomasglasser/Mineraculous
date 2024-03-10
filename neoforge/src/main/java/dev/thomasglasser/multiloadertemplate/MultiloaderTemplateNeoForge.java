package dev.thomasglasser.multiloadertemplate;

import net.neoforged.fml.common.Mod;

@Mod(MultiloaderTemplate.MOD_ID)
public class MultiloaderTemplateNeoForge
{
    public MultiloaderTemplateNeoForge() {
        MultiloaderTemplate.init();
    }
}