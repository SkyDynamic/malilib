package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;

import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient
{
    @Shadow
    public ClientWorld world;

    @Unique
    private ClientWorld worldBefore;

    @Inject(method = "<init>(Lnet/minecraft/client/RunArgs;)V", at = @At("RETURN"))
    private void malilib$onInitComplete(RunArgs args, CallbackInfo ci)
    {
        // Register all mod handlers
        ((InitializationHandler) InitializationHandler.getInstance()).onGameInitDone();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void malilib$onPostKeyboardInput(CallbackInfo ci)
    {
        KeybindMulti.reCheckPressedKeys();
        TickHandler.getInstance().onClientTick((MinecraftClient)(Object) this);
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void malilib$onLoadWorldPre(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        // Only handle dimension changes/respawns here.
        // The initial join is handled in MixinClientPlayNetworkHandler onGameJoin 
        if (this.world != null)
        {
            this.worldBefore = this.world;
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.world, worldClientIn, (MinecraftClient)(Object) this);
        }
    }

    @Inject(method = "joinWorld(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("RETURN"))
    private void malilib$onLoadWorldPost(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        if (this.worldBefore != null)
        {
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, worldClientIn, (MinecraftClient)(Object) this);
            this.worldBefore = null;
        }
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("HEAD"))
    private void malilib$onDisconnectPre(Screen disconnectionScreen, boolean bl, CallbackInfo ci)
    {
        this.worldBefore = this.world;
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, null, (MinecraftClient)(Object) this);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At("RETURN"))
    private void malilib$onDisconnectPost(Screen disconnectionScreen, boolean bl, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, null, (MinecraftClient)(Object) this);
        this.worldBefore = null;
    }
}
