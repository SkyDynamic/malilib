package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.network.handler.server.ServerCommonNetworkListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonNetworkHandler;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerCommonNetworkHandler.class)
public class MixinServerCommonNetworkHandler
{
    @Shadow @Final protected MinecraftServer server;

    @Inject(
            method = "onCustomPayload",
            at = @At("HEAD"),
            cancellable = true)
    private void malilib$onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci)
    {
        ServerCommonNetworkListener.getInstance().handleServerPayload((ServerCommonNetworkHandler) (Object) this, packet, this.server, ci);
    }
}
