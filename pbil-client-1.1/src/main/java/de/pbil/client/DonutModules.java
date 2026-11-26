package de.pbil.client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Server-Helfer (HugoSMP/Donut-Stil): Ancient Debris ab 20 Bloecken sehen, Spawner melden, Spieler-Alarm. */
final class DonutModules {
    static void register(List<Module> list) {
        list.add(new NetheriteFinder());
        list.add(new SpawnerNotifier());
        list.add(new PlayerDetection());
    }

    static final class NetheriteFinder extends RenderModules.BlockScanner {
        private final Set<BlockPos> seen = new HashSet<>();

        NetheriteFinder() {
            super("Netherite Finder", Category.DONUT, "Ancient Debris im Umkreis (Standard 20 Bloecke) markieren und melden");
        }

        @Override
        boolean matches(BlockState s) {
            return s.is(Blocks.ANCIENT_DEBRIS);
        }

        @Override
        int color(BlockState s) {
            return 0xFFE08A3B;
        }

        @Override
        void onScan(List<BlockPos> list) {
            Util.announce(seen, list, "Ancient Debris");
        }

        @Override
        public void onDisable() {
            super.onDisable();
            seen.clear();
        }
    }

    static final class SpawnerNotifier extends RenderModules.BlockScanner {
        private final Set<BlockPos> seen = new HashSet<>();

        SpawnerNotifier() {
            super("Spawner Notifier", Category.DONUT, "Meldet Spawner im Chat und markiert sie");
        }

        @Override
        boolean matches(BlockState s) {
            return s.is(Blocks.SPAWNER);
        }

        @Override
        int color(BlockState s) {
            return 0xFFFF3B3B;
        }

        @Override
        void onScan(List<BlockPos> list) {
            Util.announce(seen, list, "Spawner");
        }

        @Override
        public void onDisable() {
            super.onDisable();
            seen.clear();
        }
    }

    static final class PlayerDetection extends Module {
        private final Setting range = num("Abstand", 64, 16, 256, 16);
        private final Set<String> inside = new HashSet<>();

        PlayerDetection() {
            super("Player Detection", Category.DONUT, "Meldet im Chat, wenn ein Spieler in deine Naehe kommt");
        }

        @Override
        public void onTick() {
            Set<String> now = new HashSet<>();
            for (Entity e : Util.entities(x -> x instanceof Player && x.isAlive())) {
                if (mc.player.distanceTo(e) <= range.get()) {
                    String n = e.getName().getString();
                    now.add(n);
                    if (!inside.contains(n)) {
                        Util.say(n + " ist " + (int) mc.player.distanceTo(e) + " Bloecke entfernt (X " + (int) e.getX()
                            + " Y " + (int) e.getY() + " Z " + (int) e.getZ() + ")");
                    }
                }
            }
            inside.clear();
            inside.addAll(now);
        }

        @Override
        public void onDisable() {
            inside.clear();
        }
    }

    private DonutModules() {
    }
}
