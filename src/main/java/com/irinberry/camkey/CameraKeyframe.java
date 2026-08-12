package com.irinberry.camkey;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * An immutable camera pose: world position plus yaw/pitch in degrees.
 */
public record CameraKeyframe(double x, double y, double z, float yaw, float pitch) {
    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_YAW = "yaw";
    private static final String TAG_PITCH = "pitch";

    /**
     * Linearly interpolates position and takes the shortest rotation path.
     *
     * @param other the pose at t = 1
     * @param t     blend factor; values outside 0..1 are clamped
     */
    public CameraKeyframe interpolate(CameraKeyframe other, float t) {
        float clamped = Mth.clamp(t, 0.0F, 1.0F);
        return new CameraKeyframe(
                Mth.lerp(clamped, this.x, other.x),
                Mth.lerp(clamped, this.y, other.y),
                Mth.lerp(clamped, this.z, other.z),
                Mth.rotLerp(clamped, this.yaw, other.yaw),
                Mth.rotLerp(clamped, this.pitch, other.pitch)
        );
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(TAG_X, x);
        tag.putDouble(TAG_Y, y);
        tag.putDouble(TAG_Z, z);
        tag.putFloat(TAG_YAW, yaw);
        tag.putFloat(TAG_PITCH, pitch);
        return tag;
    }

    public static CameraKeyframe fromTag(CompoundTag tag) {
        return new CameraKeyframe(
                tag.getDouble(TAG_X),
                tag.getDouble(TAG_Y),
                tag.getDouble(TAG_Z),
                tag.getFloat(TAG_YAW),
                tag.getFloat(TAG_PITCH)
        );
    }
}
