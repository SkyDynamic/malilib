package fi.dy.masa.malilib.mixin;

import javax.annotation.Nullable;

import fi.dy.masa.malilib.network.handler.client.ClientCommonNetworkListener;
import fi.dy.masa.malilib.network.payload.PayloadManager;
import fi.dy.masa.malilib.event.WorldLoadHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 998)
public abstract class MixinClientPlayNetworkHandler
{
    @Shadow
    private ClientWorld world;
    @Unique
    @Nullable
    private ClientWorld worldBefore;

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void malilib$onPreJoinGameHead(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        // Need to grab the old world reference at the start of the method,
        // because the next injection point is right after the world has been assigned,
        // since we need the new world reference for the callback.
        this.worldBefore = this.world;
        PayloadManager.getInstance().resetPayloads();
        PayloadManager.getInstance().verifyAllPayloads();
    }

    @Inject(method = "onGameJoin", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/MinecraftClient;joinWorld(" +
                    "Lnet/minecraft/client/world/ClientWorld;)V"))
    private void malilib$onPreGameJoin(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, this.world, MinecraftClient.getInstance());
    }

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    private void malilib$onPostGameJoin(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, this.world, MinecraftClient.getInstance());
        this.worldBefore = null;

        PayloadManager.getInstance().registerAllHandlers();
    }

    /**
     * This is for "exposing" Custom Payload Packets that are obfuscated behind the Play channel.
     * It also allows for "OpenToLan" functionality to work, because via the Fabric API,
     * the network handlers are set to NULL, and often fail to function.
     * Perhaps it's a bug in the Fabric API for OpenToLan?
     * -
     * You can't use packet.getData() here anymore, it no longer exists.
     */
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void malilib$onCustomPayload(CustomPayload packet, CallbackInfo ci)
    {
        if (!MinecraftClient.getInstance().isOnThread())
        {
            return;
        }

        ClientCommonNetworkListener.getInstance().handleClientPayload((ClientPlayNetworkHandler) (Object) this, packet, ci);
    }
}