package de.pbil.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

final class MiscModules {
    static void register(List<Module> list) {
        list.add(new AutoClicker());
        list.add(new LegitAutoTool());
        list.add(new CoordSnapper());
        list.add(new WeatherNotifier());
        list.add(new BlockNotifier());
    }

    static final class AutoClicker extends Module {
        AutoClicker() {
            super("AutoClicker", Category.MISC, "Haelt du Angriff gedrueckt, schlaegt er im richtigen Tempo zu");
        }

        @Override
        public void onTick() {
            if (mc.screen == null && mc.options.keyAttack.isDown() && mc.hitResult instanceof EntityHitResult r
                && CombatModules.ready(0.95f)) {
                CombatModules.hit(r.getEntity());
            }
        }
    }

    static final class LegitAutoTool extends Module {
        LegitAutoTool() {
            super("LegitAutoTool", Category.MISC, "Waehlt beim Abbauen automatisch das schnellste Werkzeug");
        }

        @Override
        public void onTick() {
            if (mc.screen != null || !mc.options.keyAttack.isDown()
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
                return;
            }
            BlockState state = mc.level.getBlockState(hit.getBlockPos());
            int best = mc.player.getInventory().getSelectedSlot();
            float bestSpeed = mc.player.getInventory().getItem(best).getDestroySpeed(state);
            for (int i = 0; i < 9; i++) {
                float s = mc.player.getInventory().getItem(i).getDestroySpeed(state);
                if (s > bestSpeed) {
                    bestSpeed = s;
                    best = i;
                }
            }
            mc.player.getInventory().setSelectedSlot(best);
        }
    }

    static final class CoordSnapper extends Module {
        CoordSnapper() {
            super("CoordSnapper", Category.MISC, "Kopiert deine Koordinaten in die Zwischenablage (schaltet sich selbst aus)");
        }

        @Override
        public void onEnable() {
            if (mc.player == null) {
                return;
            }
            String s = (int) mc.player.getX() + " " + (int) mc.player.getY() + " " + (int) mc.player.getZ();
            mc.keyboardHandler.setClipboard(s);
            Util.say("Koordinaten kopiert: " + s);
            setEnabled(false);
        }
    }

    static final class WeatherNotifier extends Module {
        private boolean rain;
        private boolean thunder;

        WeatherNotifier() {
            super("WeatherNotifier", Category.MISC, "Meldet Regen und Gewitter im Chat");
        }

        @Override
        public void onEnable() {
            if (mc.level != null) {
                rain = mc.level.isRaining();
                thunder = mc.level.isThundering();
            }
        }

        @Override
        public void onTick() {
            if (mc.level.isRaining() != rain) {
                rain = mc.level.isRaining();
                Util.say(rain ? "Es faengt an zu regnen" : "Der Regen hoert auf");
            }
            if (mc.level.isThundering() != thunder) {
                thunder = mc.level.isThundering();
                Util.say(thunder ? "Gewitter" : "Das Gewitter ist vorbei");
            }
        }
    }

    static final class BlockNotifier extends RenderModules.BlockScanner {
        private final Setting diamond = bool("Diamant", true);
        private final Setting debris = bool("Netherit", true);
        private final Setting spawner = bool("Spawner", true);
        private final Setting emerald = bool("Smaragd", false);
        private final Set<BlockPos> seen = new HashSet<>();

        BlockNotifier() {
            super("BlockNotifier", Category.MISC, "Meldet gefundene Erze und Spawner im Chat und markiert sie");
        }

        @Override
        boolean matches(BlockState s) {
            return (diamond.on() && s.is(BlockTags.DIAMOND_ORES)) || (debris.on() && s.is(Blocks.ANCIENT_DEBRIS))
                || (spawner.on() && s.is(Blocks.SPAWNER)) || (emerald.on() && s.is(BlockTags.EMERALD_ORES));
        }

        @Override
        int color(BlockState s) {
            return s.is(Blocks.SPAWNER) ? 0xFFFF3B3B : s.is(Blocks.ANCIENT_DEBRIS) ? 0xFFB5651D : 0xFF4BE3FF;
        }

        @Override
        void onScan(List<BlockPos> list) {
            Util.announce(seen, list, "Fund");
        }

        @Override
        public void onDisable() {
            super.onDisable();
            seen.clear();
        }
    }

    private MiscModules() {
    }
}
