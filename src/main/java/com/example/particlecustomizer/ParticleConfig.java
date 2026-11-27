package com.example.particlecustomizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ParticleConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("particlecustomizer.json");

    public static class Rule {
        public String replacementId = "";
        public float multiplier = 1.0f;

        public Rule() {}

        public Rule(String replacementId, float multiplier) {
            this.replacementId = replacementId;
            this.multiplier = multiplier;
        }
    }

    public Map<String, Rule> rules = new HashMap<>();

    public ParticleConfig() {
        rules.put("minecraft:crit", new Rule("minecraft:flame", 2.0f));
    }

    public Rule getOrCreateRule(String sourceParticleId) {
        return rules.computeIfAbsent(sourceParticleId, k -> new Rule("", 1.0f));
    }

    public ParticleOptions resolveParticle(ParticleOptions original) {
        Identifier sourceId = BuiltInRegistries.PARTICLE_TYPE.getKey(original.getType());
        if (sourceId == null) return original;

        Rule rule = rules.get(sourceId.toString());
        if (rule == null || rule.replacementId == null || rule.replacementId.isBlank()) {
            return original;
        }

        Identifier targetId = Identifier.tryParse(rule.replacementId.trim());
        if (targetId == null) return original;

        ParticleType<?> targetType = BuiltInRegistries.PARTICLE_TYPE.getValue(targetId);
        if (targetType instanceof SimpleParticleType simple) {
            return simple;
        }
        return original;
    }

    public float getMultiplier(ParticleOptions original) {
        Identifier sourceId = BuiltInRegistries.PARTICLE_TYPE.getKey(original.getType());
        if (sourceId == null) return 1.0f;

        Rule rule = rules.get(sourceId.toString());
        return rule != null ? Math.max(0.0f, rule.multiplier) : 1.0f;
    }

    public static ParticleConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                ParticleConfig loaded = GSON.fromJson(Files.readString(CONFIG_PATH), ParticleConfig.class);
                if (loaded != null) {
                    if (loaded.rules == null) loaded.rules = new HashMap<>();
                    return loaded;
                }
            } catch (IOException ignored) {
            }
        }
        ParticleConfig created = new ParticleConfig();
        created.save();
        return created;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }
}
