package com.lovetropics.gamemodebuild.mixin;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "hasInfiniteMaterials", cancellable = true)
    private void infiniteMaterialsInBuild(CallbackInfoReturnable<Boolean> cir) {
        if (GamemodeBuild.isActive((Player) (Object) this)) {
            cir.setReturnValue(true);
        }
    }
}
