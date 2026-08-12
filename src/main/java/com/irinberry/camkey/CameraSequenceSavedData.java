package com.irinberry.camkey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * World-global camera sequences, stored in the overworld data folder so they
 * survive closing and reopening the world.
 */
public final class CameraSequenceSavedData extends SavedData {
    public static final String FILE_ID = "camkey_sequences";
    private static final String TAG_SEQUENCES = "sequences";

    private final Map<String, CameraSequence> sequences = new LinkedHashMap<>();

    public static SavedData.Factory<CameraSequenceSavedData> factory() {
        return new SavedData.Factory<>(CameraSequenceSavedData::new, CameraSequenceSavedData::load);
    }

    public static CameraSequenceSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(factory(), FILE_ID);
    }

    public Optional<CameraSequence> getSequence(String name) {
        return Optional.ofNullable(sequences.get(name));
    }

    public CameraSequence getOrCreate(String name) {
        return sequences.computeIfAbsent(name, CameraSequence::new);
    }

    /**
     * Creates the sequence if needed, appends the keyframe, and marks this data dirty.
     *
     * @return {@code false} if the keyframe's dimension does not match the sequence
     */
    public boolean addKeyframe(String name, CameraKeyframe keyframe, ResourceLocation dimension) {
        CameraSequence sequence = getOrCreate(name);
        if (!sequence.add(keyframe, dimension)) {
            return false;
        }
        setDirty();
        return true;
    }

    public static CameraSequenceSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        CameraSequenceSavedData data = new CameraSequenceSavedData();
        CompoundTag sequencesTag = tag.getCompound(TAG_SEQUENCES);
        for (String name : sequencesTag.getAllKeys()) {
            CameraSequence sequence = CameraSequence.fromTag(name, sequencesTag.getCompound(name));
            data.sequences.put(name, sequence);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag sequencesTag = new CompoundTag();
        for (CameraSequence sequence : sequences.values()) {
            sequencesTag.put(sequence.name(), sequence.toTag());
        }
        tag.put(TAG_SEQUENCES, sequencesTag);
        return tag;
    }
}
