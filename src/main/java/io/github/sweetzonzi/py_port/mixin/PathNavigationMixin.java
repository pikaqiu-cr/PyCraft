package io.github.sweetzonzi.py_port.mixin;

import io.github.sweetzonzi.py_port.agent.AgentManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigation.class)
public class PathNavigationMixin {
    @Final
    @Shadow
    protected Mob mob;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        if (AgentManager.get(mob.getId()) != null) {
            ci.cancel();
        }
    }
}
