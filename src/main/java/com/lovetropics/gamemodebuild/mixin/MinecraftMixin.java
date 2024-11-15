package com.lovetropics.gamemodebuild.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.lovetropics.gamemodebuild.container.GBStackMarker;
import com.lovetropics.gamemodebuild.message.SetGamemodeBuildSlotPacket;
import com.lovetropics.gamemodebuild.state.GBClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Debug(export = true)
@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow @Nullable public LocalPlayer player;

    @Shadow @Nullable public HitResult hitResult;

    @Shadow @Nullable public ClientLevel level;

    @ModifyVariable(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;", shift = At.Shift.BY, by = 2))
    private boolean flagInsta(boolean value, @Local(ordinal = 0) HitResult.Type type) {
        if(GBClientState.isActive() && type == HitResult.Type.BLOCK) {
            return true;
        }
        return value;
    }

    @Redirect(
            method = "pickBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;hasControlDown()Z")
    )
    private boolean redirectHasControlDown() {
        if(GBClientState.isActive()) {
            return false;
        }
        return Screen.hasControlDown();
    }

    @Inject(method = "pickBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;handleCreativeModeItemAdd(Lnet/minecraft/world/item/ItemStack;I)V"))
    private void handleGamemodeBuildItemAdd(CallbackInfo ci, @Local(ordinal = 0) Inventory inventory) {
        if(this.player != null && !this.player.isCreative() && GBClientState.isActive() && this.hitResult instanceof BlockHitResult hitResult) {
            PacketDistributor.sendToServer(new SetGamemodeBuildSlotPacket((short) (36 + inventory.selected), hitResult.getBlockPos()));
        }
    }
}
