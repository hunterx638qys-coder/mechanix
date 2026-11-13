package com.example.mechanix.event;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.MechanixMod;
import com.example.mechanix.block.LaunchPadBlock;
import com.example.mechanix.command.MechanixCommands;
import com.example.mechanix.registry.ModEffects;
import com.example.mechanix.registry.ModItems;
import com.example.mechanix.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = MechanixMod.MODID)
public class ModEvents {

    /** Рубка деревьев (присед + топор) и «удачная добыча» руды (бонусный дроп). */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (player.isCreative()) return;

        BlockState state = event.getState();
        BlockPos pos = event.getPos();
        ItemStack tool = player.getMainHandItem();

        // --- Tree felling ---
        if (MechanixConfig.TREE_FELLING.get() && player.isShiftKeyDown()
                && tool.getItem() instanceof AxeItem && state.is(BlockTags.LOGS)) {
            List<BlockPos> logs = BlockUtil.floodFind(level, pos, s -> s.is(BlockTags.LOGS),
                    MechanixConfig.TREE_LIMIT.get(), true);
            for (BlockPos p : logs) {
                if (!BlockUtil.hasDurabilityLeft(tool)) break;
                BlockUtil.breakWithTool(level, player, p, tool);
            }
        }

        // --- Lucky mining ---
        if (MechanixConfig.LUCKY_MINING.get() && state.is(Tags.Blocks.ORES)
                && (!state.requiresCorrectToolForDrops() || tool.isCorrectToolForDrops(state))
                && level.random.nextInt(100) < MechanixConfig.LUCKY_CHANCE.get()) {
            Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, tool);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    10, 0.3D, 0.3D, 0.3D, 0.0D);
            player.displayClientMessage(Component.translatable("message.mechanix.lucky"), true);
        }
    }

    /** Вампиризм: атакующий с эффектом лечится на долю нанесённого урона. */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!MechanixConfig.VAMPIRISM.get()) return;
        if (event.getSource().getEntity() instanceof LivingEntity attacker && !attacker.level().isClientSide) {
            MobEffectInstance effect = attacker.getEffect(ModEffects.VAMPIRISM.get());
            if (effect != null) {
                attacker.heal(event.getAmount() * 0.25F * (effect.getAmplifier() + 1));
            }
        }
    }

    /** Монстры, убитые игроком, с шансом роняют Клык вампира. */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!MechanixConfig.FANG_DROPS.get()) return;
        LivingEntity victim = event.getEntity();
        if (victim instanceof Monster && event.getSource().getEntity() instanceof Player
                && victim.level().random.nextInt(100) < MechanixConfig.FANG_CHANCE.get()) {
            event.getDrops().add(new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(),
                    new ItemStack(ModItems.VAMPIRE_FANG.get())));
        }
    }

    /** Координаты смерти в чат. */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!MechanixConfig.DEATH_COORDS.get()) return;
        if (event.getEntity() instanceof ServerPlayer player) {
            BlockPos p = player.blockPosition();
            player.sendSystemMessage(Component.translatable("message.mechanix.death",
                    p.getX(), p.getY(), p.getZ(), player.level().dimension().location().toString()));
        }
    }

    /** Нет урона от падения после запуска с Launch Pad (в течение 10 секунд). */
    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        long until = entity.getPersistentData().getLong(LaunchPadBlock.NO_FALL_UNTIL);
        if (until >= entity.level().getGameTime()) {
            event.setCanceled(true);
            entity.getPersistentData().remove(LaunchPadBlock.NO_FALL_UNTIL);
        }
    }

    /** Сохраняем «дом» игрока и прочие данные после смерти. */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getPersistentData().merge(event.getOriginal().getPersistentData());
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        MechanixCommands.register(event.getDispatcher());
    }
}
