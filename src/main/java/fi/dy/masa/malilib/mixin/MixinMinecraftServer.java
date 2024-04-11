package fi.dy.masa.malilib.mixin;

import fi.dy.masa.malilib.MaLiLibReference;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.ServerHandler;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, priority = 998)
public abstract class MixinMinecraftServer
{
    /**
     * For invoking IntegratedServer() and DedicatedServer() calls --
     * Works much better for Network API and Mod initialization
     */
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;setupServer()Z"), method = "runServer")
    private void malilib$onServerStarting(CallbackInfo ci)
    {
        if (MaLiLibReference.isServer())
        {
            // This initializes MaLiLib's Server Environment Mode
            ((InitializationHandler) InitializationHandler.getInstance()).onGameInitDone();
        }

        ((ServerHandler) ServerHandler.getInstance()).onServerStarting((MinecraftServer) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;createMetadata()Lnet/minecraft/server/ServerMetadata;", ordinal = 0), method = "runServer")
    private void malilib$onServerStarted(CallbackInfo ci)
    {
        ((ServerHandler) ServerHandler.getInstance()).onServerStarted((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "shutdown")
    private void malilib$onServerStopping(CallbackInfo info)
    {
        ((ServerHandler) ServerHandler.getInstance()).onServerStopping((MinecraftServer) (Object) this);
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    private void malilib$onServerStopped(CallbackInfo info)
    {
        ((ServerHandler) ServerHandler.getInstance()).onServerStopped((MinecraftServer) (Object) this);
    }
}
