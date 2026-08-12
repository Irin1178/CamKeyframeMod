package com.irinberry.camkey.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.irinberry.camkey.CameraClientPlayback;
import com.irinberry.camkey.CameraKeyframe;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;

@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setPosition(double x, double y, double z);

    @Shadow
    protected abstract void setRotation(float yRot, float xRot, float roll);

    @Inject(method = "setup", at = @At("TAIL"))
    private void camkey$applyCinematicPose(
            BlockGetter level,
            Entity entity,
            boolean detached,
            boolean thirdPersonReverse,
            float partialTick,
            CallbackInfo ci
    ) {
        CameraKeyframe pose = CameraClientPlayback.currentPose(partialTick);
        if (pose == null) {
            return;
        }
        // Keyframes store feet position; vanilla first-person is at eye height.
        this.setPosition(pose.x(), pose.y() + entity.getEyeHeight(), pose.z());
        this.setRotation(pose.yaw(), pose.pitch(), 0.0F);
    }
}
