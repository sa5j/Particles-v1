package com.example.particlecustomizer.mixin;

import com.example.particlecustomizer.ParticleCustomizerClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class ClientLevelParticleMixin {

    @Unique
    private static final ThreadLocal<Boolean> PC_REENTRY = ThreadLocal.withInitial(() -> false);

    @Shadow
    public abstract void addParticle(ParticleOptions options, double x, double y, double z, double dx, double dy, double dz);

    @Inject(
        method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onAddParticle(
            ParticleOptions options,
            double x, double y, double z,
            double dx, double dy, double dz,
            CallbackInfo ci
    ) {
        if (PC_REENTRY.get()) return;

        var config = ParticleCustomizerClient.getConfig();
        if (config == null) return;

        ParticleOptions resolved = config.resolveParticle(options);
        float mult = config.getMultiplier(options);

        if (resolved == options && Math.abs(mult - 1.0f) < 0.0001f) {
            return;
        }

        ci.cancel();
        if (mult <= 0.0f) return;

        int whole = (int) Math.floor(mult);
        float frac = mult - whole;
        int totalToSpawn = whole + (Math.random() < frac ? 1 : 0);

        ClientLevel level = (ClientLevel) (Object) this;
        PC_REENTRY.set(true);
        try {
            for (int i = 0; i < totalToSpawn; i++) {
                double jx = (i == 0) ? 0.0 : (Math.random() - 0.5) * 0.25;
                double jy = (i == 0) ? 0.0 : (Math.random() * 0.2);
                double jz = (i == 0) ? 0.0 : (Math.random() - 0.5) * 0.25;
                level.addParticle(resolved, x + jx, y + jy, z + jz, dx, dy, dz);
            }
        } finally {
            PC_REENTRY.set(false);
        }
    }
}
