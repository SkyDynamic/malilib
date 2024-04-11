package fi.dy.masa.malilib.mixin;

import com.mojang.authlib.GameProfile;
import fi.dy.masa.malilib.event.PlayerHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.net.SocketAddress;
import java.util.UUID;

/**
 * Interface for processing various server side Player Manager events
 */
@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager
{
    @Unique
    private GameProfile profileTemp;

    public MixinPlayerManager() { super(); }

    @Inject(method = "checkCanJoin", at = @At("RETURN"))
    private void malilib$eventOnClientConnect(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir)
    {
        ((PlayerHandler) PlayerHandler.getInstance()).onClientConnect(address, profile, cir.getReturnValue());
    }

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void malilib$eventOnPlayerJoin(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci)
    {
        ((PlayerHandler) PlayerHandler.getInstance()).onPlayerJoin(connection.getAddress(), clientData.gameProfile(), player);
    }

    @Inject(method = "respawnPlayer", at = @At("RETURN"))
    private void malilib$eventOnPlayerRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir)
    {
        ServerPlayerEntity newPlayer = cir.getReturnValue();

        ((PlayerHandler) PlayerHandler.getInstance()).onPlayerRespawn(newPlayer, player);
    }

    @Inject(method = "addToOperators", at = @At("HEAD"))
    private void malilib$captureGameProfileOp(GameProfile profile, CallbackInfo ci)
    {
        this.profileTemp = profile;
    }

    @Redirect(method = "addToOperators",
            at = @At(value = "INVOKE",
                    target ="Lnet/minecraft/server/PlayerManager;getPlayer(Ljava/util/UUID;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    private ServerPlayerEntity malilib$eventOnPlayerOp(PlayerManager instance, UUID uuid)
    {
        ServerPlayerEntity player = instance.getPlayer(uuid);

        ((PlayerHandler) PlayerHandler.getInstance()).onPlayerOp(this.profileTemp, uuid, player);

        if (this.profileTemp != null)
        {
            this.profileTemp = null;
        }

        return player;
    }

    @Inject(method = "removeFromOperators", at = @At("HEAD"))
    private void malilib$captureGameProfileDeOp(GameProfile profile, CallbackInfo ci)
    {
        this.profileTemp = profile;
    }

    @Redirect(method = "removeFromOperators",
            at = @At(value = "INVOKE",
                    target="Lnet/minecraft/server/PlayerManager;getPlayer(Ljava/util/UUID;)Lnet/minecraft/server/network/ServerPlayerEntity;"))
    private ServerPlayerEntity malilib$eventOnPlayerDeOp(PlayerManager instance, UUID uuid)
    {
        ServerPlayerEntity player = instance.getPlayer(uuid);

        ((PlayerHandler) PlayerHandler.getInstance()).onPlayerDeOp(this.profileTemp, uuid, player);

        if (this.profileTemp != null)
        {
            this.profileTemp = null;
        }
        return player;
    }

    @Inject(method = "remove", at = @At("HEAD"))
    private void malilib$eventOnPlayerLeave(ServerPlayerEntity player, CallbackInfo ci)
    {
        ((PlayerHandler) PlayerHandler.getInstance()).onPlayerLeave(player);
    }

    @Inject(method = "setSimulationDistance", at = @At("HEAD"))
    private void malilib$eventOnSimulDistance(int simulationDistance, CallbackInfo ci)
    {
        ((PlayerHandler) PlayerHandler.getInstance()).onSetSimulDistance(simulationDistance);
    }

    @Inject(method = "setViewDistance", at = @At("HEAD"))
    private void malilib$eventOnViewDistance(int viewDistance, CallbackInfo ci)
    {
        ((PlayerHandler) PlayerHandler.getInstance()).onSetViewDistance(viewDistance);
    }
}
