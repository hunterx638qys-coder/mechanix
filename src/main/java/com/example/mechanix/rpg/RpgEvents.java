package com.example.mechanix.rpg;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.MechanixMod;
import com.example.mechanix.command.MechanixCommands;
import com.example.mechanix.entity.RpgBoss;
import com.example.mechanix.network.ModNetwork;
import com.example.mechanix.network.OpenScreenPacket;
import com.example.mechanix.registry.ModArmorMaterial;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = MechanixMod.MODID)
public class RpgEvents {

    // ---------------- вход / возрождение ----------------
    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        RpgData d = RpgData.of(p);
        if (d.flightUntil() <= p.level().getGameTime()) {
            d.setFlightUntil(0L);
            revokeFlight(p);
        }
        Rpg.refresh(p);
        if (!d.hasRace()) {
            ModNetwork.sendTo(p, new OpenScreenPacket(0));
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (!(e.getEntity() instanceof ServerPlayer p)) return;
        RpgData d = RpgData.of(p);
        d.setMana(d.maxMana());
        Rpg.refresh(p);
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone e) {
        var old = e.getOriginal().getPersistentData();
        if (old.contains(RpgData.KEY)) {
            e.getEntity().getPersistentData().put(RpgData.KEY, old.getCompound(RpgData.KEY).copy());
        }
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) Rpg.refresh(p);
    }

    private static void revokeFlight(ServerPlayer p) {
        if (!p.isCreative() && !p.isSpectator()) {
            p.getAbilities().mayfly = false;
            p.getAbilities().flying = false;
            p.onUpdateAbilities();
        }
    }

    // ---------------- тик игрока ----------------
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !(e.player instanceof ServerPlayer p)) return;
        RpgData d = RpgData.of(p);
        Race r = d.race();
        if (r == null) return;
        long now = p.level().getGameTime();

        if (now < d.dashUntil()) {
            for (LivingEntity f : SpellUtil.foes(p, p.position().add(0.0D, 1.0D, 0.0D), 2.0D)) {
                SpellUtil.hit(p, f, (float) d.dashDamage());
            }
        }
        if (d.flightUntil() != 0L && now >= d.flightUntil()) {
            d.setFlightUntil(0L);
            revokeFlight(p);
        }

        if (p.tickCount % 20 != 0) return;

        double regen = (1.0D + d.total(Stat.INT) * 0.15D + d.level() * 0.05D)
                * MechanixConfig.MANA_REGEN_PERCENT.get() / 100.0D;
        d.setMana(d.mana() + regen);
        RaceAbilities.passives(p, d, r);
        ArmorSets.tick(p, d);
        Rpg.sync(p);
    }

    // ---------------- урон ----------------
    @SubscribeEvent
    public static void onHurt(LivingHurtEvent e) {
        LivingEntity victim = e.getEntity();
        DamageSource src = e.getSource();
        float amount = e.getAmount();
        long now = victim.level().getGameTime();

        if (victim.getPersistentData().getLong(Rpg.MARK_KEY) > now) {
            amount *= 1.35F;
        }

        if (victim instanceof ServerPlayer vp) {
            RpgData vd = RpgData.of(vp);
            Race vr = vd.race();
            if (vr == Race.DRAGONBORN && src.is(DamageTypeTags.IS_FIRE)) {
                amount *= 0.5F;
            }
            if (vr == Race.UNDEAD && vp.getHealth() - amount <= 0.0F && now >= vd.lastBreathReady()) {
                vd.setLastBreathReady(now + 12000L);
                vp.setHealth(4.0F);
                vp.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 2));
                vp.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 2));
                vp.displayClientMessage(Component.translatable("msg.mechanix.last_breath"), true);
                ((ServerLevel) vp.level()).sendParticles(ParticleTypes.SOUL, vp.getX(), vp.getY() + 1.0D, vp.getZ(),
                        30, 0.5D, 0.8D, 0.5D, 0.05D);
                e.setCanceled(true);
                return;
            }
        }

        if (src.getEntity() instanceof ServerPlayer ap) {
            RpgData ad = RpgData.of(ap);
            Race ar = ad.race();
            if (ar == Race.ORC && ap.getHealth() < ap.getMaxHealth() * 0.5F) amount *= 1.2F;
            if (ar == Race.VAMPIRE) ap.heal(amount * 0.12F);
            if (ArmorSets.has(ap, ModArmorMaterial.BLOODSTONE)) ap.heal(amount * 0.10F);
        }
        e.setAmount(amount);
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) {
            Race r = RpgData.of(p).race();
            if (r == Race.ANGEL) {
                e.setCanceled(true);
            } else if (r == Race.ELF || r == Race.BEASTKIN || r == Race.WEREWOLF || r == Race.GOBLIN) {
                e.setDamageMultiplier(0.5F);
            }
        }
    }

    // ---------------- опыт за убийства ----------------
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent e) {
        LivingEntity victim = e.getEntity();
        if (victim.level().isClientSide || victim instanceof Player || !(victim instanceof Enemy)) return;
        if (!(e.getSource().getEntity() instanceof ServerPlayer killer)) return;

        int base = (int) victim.getMaxHealth();
        if (victim instanceof RpgBoss) base *= 3;
        Rpg.addXp(killer, base);

        if (MechanixConfig.SHARE_XP.get()) {
            for (Player other : victim.level().getEntitiesOfClass(Player.class, victim.getBoundingBox().inflate(24.0D))) {
                if (other != killer && other instanceof ServerPlayer sp) {
                    Rpg.addXp(sp, (int) (base * 0.4D));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent e) {
        if (e.getSource().getEntity() instanceof ServerPlayer p
                && RpgData.of(p).race() == Race.GOBLIN
                && e.getEntity().level().random.nextFloat() < 0.15F) {
            List<ItemEntity> extra = new ArrayList<>();
            for (ItemEntity ie : e.getDrops()) {
                extra.add(new ItemEntity(e.getEntity().level(), ie.getX(), ie.getY(), ie.getZ(), ie.getItem().copy()));
            }
            e.getDrops().addAll(extra);
        }
    }

    // ---------------- духи-волки исчезают по таймеру ----------------
    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent e) {
        if (e.getEntity() instanceof Wolf w && !w.level().isClientSide
                && w.getPersistentData().contains(Rpg.SPIRIT_KEY)
                && w.level().getGameTime() >= w.getPersistentData().getLong(Rpg.SPIRIT_KEY)) {
            ((ServerLevel) w.level()).sendParticles(ParticleTypes.POOF, w.getX(), w.getY() + 0.5D, w.getZ(),
                    10, 0.3D, 0.3D, 0.3D, 0.02D);
            w.discard();
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent e) {
        MechanixCommands.register(e.getDispatcher());
    }
}
