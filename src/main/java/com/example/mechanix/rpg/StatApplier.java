package com.example.mechanix.rpg;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** Переносит характеристики/расу в атрибуты игрока (здоровье, скорость, урон...). */
public final class StatApplier {
    private static final UUID HEALTH = UUID.fromString("5c1f0e3a-1b7d-4d1e-9f3a-0a1b2c3d4e01");
    private static final UUID SPEED = UUID.fromString("5c1f0e3a-1b7d-4d1e-9f3a-0a1b2c3d4e02");
    private static final UUID DAMAGE = UUID.fromString("5c1f0e3a-1b7d-4d1e-9f3a-0a1b2c3d4e03");
    private static final UUID ARMOR = UUID.fromString("5c1f0e3a-1b7d-4d1e-9f3a-0a1b2c3d4e04");
    private static final UUID ATK_SPEED = UUID.fromString("5c1f0e3a-1b7d-4d1e-9f3a-0a1b2c3d4e05");

    private StatApplier() {}

    public static void apply(ServerPlayer p) {
        RpgData d = RpgData.of(p);
        Race r = d.race();

        double health = 0.0D;
        double speed = 0.0D;
        double damage = 0.0D;
        double armor = 0.0D;
        double atkSpeed = 0.0D;

        if (r != null) {
            health += r.health;
            speed += r.speedPct / 100.0D;
            damage += r.damage;
            armor += r.armor;
        }
        int str = d.total(Stat.STR);
        int agi = d.total(Stat.AGI);
        int vit = d.total(Stat.VIT);

        health += vit;
        damage += str * 0.3D + d.level() * 0.04D;
        speed += agi * 0.004D;
        atkSpeed += agi * 0.006D;
        armor += vit * 0.12D;

        set(p, Attributes.MAX_HEALTH, HEALTH, "mechanix_health", health, AttributeModifier.Operation.ADDITION);
        set(p, Attributes.MOVEMENT_SPEED, SPEED, "mechanix_speed", speed, AttributeModifier.Operation.MULTIPLY_BASE);
        set(p, Attributes.ATTACK_DAMAGE, DAMAGE, "mechanix_damage", damage, AttributeModifier.Operation.ADDITION);
        set(p, Attributes.ARMOR, ARMOR, "mechanix_armor", armor, AttributeModifier.Operation.ADDITION);
        set(p, Attributes.ATTACK_SPEED, ATK_SPEED, "mechanix_atkspeed", atkSpeed, AttributeModifier.Operation.MULTIPLY_BASE);

        if (p.getHealth() > p.getMaxHealth()) {
            p.setHealth(p.getMaxHealth());
        }
    }

    private static void set(ServerPlayer p, Attribute attr, UUID id, String name, double amount, AttributeModifier.Operation op) {
        AttributeInstance inst = p.getAttribute(attr);
        if (inst == null) return;
        inst.removeModifier(id);
        if (amount != 0.0D) {
            inst.addPermanentModifier(new AttributeModifier(id, name, amount, op));
        }
    }
}
