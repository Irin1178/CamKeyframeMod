package com.irinberry.camkey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

/**
 * A named, ordered list of camera keyframes captured in one dimension.
 */
public final class CameraSequence {
    private static final String TAG_DIMENSION = "dimension";
    private static final String TAG_KEYFRAMES = "keyframes";

    private final String name;
    private final List<CameraKeyframe> keyframes = new ArrayList<>();
    private ResourceLocation dimension;

    public CameraSequence(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String name() {
        return name;
    }

    /**
     * Dimension of the first accepted keyframe, or {@code null} if the sequence is empty.
     */
    public ResourceLocation dimension() {
        return dimension;
    }

    public int size() {
        return keyframes.size();
    }

    public List<CameraKeyframe> keyframes() {
        return Collections.unmodifiableList(keyframes);
    }

    /**
     * Appends a keyframe. The first successful add stores {@code sourceDimension}.
     * Later adds from a different dimension are rejected.
     *
     * @return {@code true} if the keyframe was appended
     */
    public boolean add(CameraKeyframe keyframe, ResourceLocation sourceDimension) {
        Objects.requireNonNull(keyframe, "keyframe");
        Objects.requireNonNull(sourceDimension, "sourceDimension");
        if (dimension == null) {
            dimension = sourceDimension;
        } else if (!dimension.equals(sourceDimension)) {
            return false;
        }
        keyframes.add(keyframe);
        return true;
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        if (dimension != null) {
            tag.putString(TAG_DIMENSION, dimension.toString());
        }
        ListTag list = new ListTag();
        for (CameraKeyframe keyframe : keyframes) {
            list.add(keyframe.toTag());
        }
        tag.put(TAG_KEYFRAMES, list);
        return tag;
    }

    public static CameraSequence fromTag(String name, CompoundTag tag) {
        CameraSequence sequence = new CameraSequence(name);
        if (tag.contains(TAG_DIMENSION, Tag.TAG_STRING)) {
            sequence.dimension = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
        }
        ListTag list = tag.getList(TAG_KEYFRAMES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            sequence.keyframes.add(CameraKeyframe.fromTag(list.getCompound(i)));
        }
        return sequence;
    }
}
