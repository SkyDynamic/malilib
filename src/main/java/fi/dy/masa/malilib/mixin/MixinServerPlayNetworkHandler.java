package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.network.handler.server.ServerPlayNetworkListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayNetworkHandler.class)
public class MixinServerPlayNetworkHandler
{
    @Inject(
            method = "onCustomPayload",
            at = @At("HEAD"),
            cancellable = true)
    private void malilib$onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci)
    {
        ServerPlayNetworkListener.getInstance().handleServerPayload((ServerPlayNetworkHandler) (Object) this, packet, null, ci);
    }
}
