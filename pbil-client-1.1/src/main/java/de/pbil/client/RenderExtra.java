package de.pbil.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

final class RenderExtra {
    static void register(List<Module> list) {
        list.add(new GlowEsp());
        list.add(new HoleEsp());
        list.add(new LowHealthWarning());
        list.add(new Trajectories(false));
        list.add(new Trajectories(true));
        list.add(new SpawnerNametag());
    }

    static final class GlowEsp extends Module {
        GlowEsp() {
            super("Glow ESP", Category.RENDER, "Spieler leuchten (Umriss durch Waende)");
        }

        @Override
        public void onTick() {
            for (Entity e : Util.entities(x -> x instanceof Player)) {
                e.setGlowingTag(true);
            }
        }

        @Override
        public void onDisable() {
            if (mc.level != null) {
                for (Entity e : Util.entities(x -> x instanceof Player)) {
                    e.setGlowingTag(false);
                }
            }
        }
    }

    static final class HoleEsp extends Module {
        private final Setting radius = num("Radius", 8, 3, 16, 1);
        private List<BlockPos> bedrock = new ArrayList<>();
        private List<BlockPos> obsidian = new ArrayList<>();
        private int ticks;

        HoleEsp() {
            super("Hole ESP", Category.RENDER, "Zeigt sichere Loecher (4 Seiten Obsidian/Bedrock): gruen = Bedrock, gelb = Obsidian");
        }

        private boolean safe(BlockPos p) {
            BlockState s = mc.level.getBlockState(p);
            return s.is(Blocks.OBSIDIAN) || s.is(Blocks.BEDROCK) || s.is(Blocks.CRYING_OBSIDIAN);
        }

        @Override
        public void onTick() {
            if (ticks++ % 10 != 0) {
                return;
            }
            List<BlockPos> b = new ArrayList<>(), o = new ArrayList<>();
            BlockPos c = mc.player.blockPosition();
            int r = radius.asInt();
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    for (int y = -4; y <= 3; y++) {
                        BlockPos p = c.offset(x, y, z);
                        if (!mc.level.getBlockState(p).isAir() || !mc.level.getBlockState(p.above()).isAir()) {
                            continue;
                        }
                        BlockPos[] sides = {p.below(), p.north(), p.south(), p.east(), p.west()};
                        boolean all = true, allBed = true;
                        for (BlockPos s : sides) {
                            if (!safe(s)) {
                                all = false;
                                break;
                            }
                            if (!mc.level.getBlockState(s).is(Blocks.BEDROCK)) {
                                allBed = false;
                            }
                        }
                        if (all && b.size() + o.size() < 80) {
                            (allBed ? b : o).add(p);
                        }
                    }
                }
            }
            bedrock = b;
            obsidian = o;
        }

        @Override
        public void onDisable() {
            bedrock = new ArrayList<>();
            obsidian = new ArrayList<>();
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr == null) {
                return;
            }
            for (BlockPos p : bedrock) {
                Draw.box3d(g, pr, new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 0.1, p.getZ() + 1), 0xFF3CE05A);
            }
            for (BlockPos p : obsidian) {
                Draw.box3d(g, pr, new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 0.1, p.getZ() + 1), 0xFFFFD83B);
            }
        }
    }

    static final class LowHealthWarning extends Module {
        private final Setting hp = num("Leben unter", 6, 2, 19, 1);

        LowHealthWarning() {
            super("Low Health Warning", Category.RENDER, "Roter Bildschirmrand bei wenig Leben");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            if (mc.player.getHealth() > hp.get()) {
                return;
            }
            int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
            int pulse = 40 + (int) (30 * Math.abs(Math.sin(System.currentTimeMillis() / 300.0)));
            int c = (pulse << 24) | 0xFF0000;
            int t = 24;
            g.fill(0, 0, sw, t, c);
            g.fill(0, sh - t, sw, sh, c);
            g.fill(0, t, t, sh - t, c);
            g.fill(sw - t, t, sw, sh - t, c);
        }
    }

    /** Flugbahn von Pfeilen, Tridents, Schneebaellen und Enderperlen (nur Perlen, wenn pearlsOnly). */
    static final class Trajectories extends Module {
        private final boolean pearlsOnly;

        Trajectories(boolean pearlsOnly) {
            super(pearlsOnly ? "Pearl Trajectory" : "Trajectories", Category.RENDER,
                pearlsOnly ? "Zeigt, wo Enderperlen landen" : "Zeigt die Flugbahn von Pfeilen, Tridents, Perlen und Baellen");
            this.pearlsOnly = pearlsOnly;
        }

        private boolean wanted(Entity e) {
            var t = e.getType();
            if (pearlsOnly) {
                return t == EntityType.ENDER_PEARL;
            }
            return t == EntityType.ENDER_PEARL || t == EntityType.ARROW || t == EntityType.SPECTRAL_ARROW
                || t == EntityType.TRIDENT || t == EntityType.SNOWBALL;
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr == null) {
                return;
            }
            for (Entity e : mc.level.entitiesForRendering()) {
                if (!wanted(e) || e.getDeltaMovement().lengthSqr() < 0.001) {
                    continue;
                }
                Vec3 pos = e.position();
                Vec3 v = e.getDeltaMovement();
                double gravity = e.getType() == EntityType.ENDER_PEARL || e.getType() == EntityType.SNOWBALL ? 0.03 : 0.05;
                for (int i = 0; i < 150; i++) {
                    Vec3 next = pos.add(v);
                    BlockPos bp = BlockPos.containing(next);
                    boolean hit = !mc.level.getBlockState(bp).getCollisionShape(mc.level, bp).isEmpty();
                    double[] s = pr.segment(pos.x, pos.y, pos.z, next.x, next.y, next.z);
                    if (s != null) {
                        Draw.line(g, s[0], s[1], s[2], s[3], 0xFFFFE04B);
                    }
                    if (hit) {
                        Draw.box3d(g, pr, new AABB(next.x - 0.25, next.y - 0.25, next.z - 0.25, next.x + 0.25, next.y + 0.25, next.z + 0.25), 0xFFFF5B5B);
                        break;
                    }
                    pos = next;
                    v = v.scale(0.99).add(0, -gravity, 0);
                }
            }
        }
    }

    static final class SpawnerNametag extends RenderModules.BlockScanner {
        SpawnerNametag() {
            super("Spawner Nametag", "Schild \"Spawner\" mit Abstand ueber jedem Spawner");
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
        void draw(GuiGraphics g, Proj pr, BlockPos p, BlockState s) {
            double[] sc = pr.project(p.getX() + 0.5, p.getY() + 1.4, p.getZ() + 0.5);
            if (sc != null) {
                Draw.centered(g, "Spawner " + (int) Math.sqrt(mc.player.blockPosition().distSqr(p)) + "m", sc[0], sc[1], 0xFFFF6B6B);
            }
        }
    }

    private RenderExtra() {
    }
}
