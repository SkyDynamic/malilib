package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.network.handler.client.ClientCommonNetworkListener;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.packet.CustomPayload;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConfigurationNetworkHandler.class, priority = 998)
public class MixinClientConfigurationNetworkHandler
{
    /**
     * You can't use packet.getData() here anymore, it no longer exists.
     */
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void malilib$onCustomPayload(CustomPayload packet, CallbackInfo ci)
    {
        if (!MinecraftClient.getInstance().isOnThread())
        {
            return;
        }

        ClientCommonNetworkListener.getInstance().handleClientPayload((ClientConfigurationNetworkHandler) (Object) this, packet, ci);
    }
}
