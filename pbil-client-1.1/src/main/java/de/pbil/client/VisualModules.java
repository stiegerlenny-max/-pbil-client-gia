package de.pbil.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

final class VisualModules {
    static void register(List<Module> list) {
        list.add(new CustomCrosshair());
        list.add(new CustomFov());
        list.add(new BlockOverlay());
        list.add(new TargetEsp());
        list.add(new HitParticles());
        list.add(new JumpCircles());
        list.add(new Particles());
        list.add(new TargetSpirit());
        list.add(new ColorGrading());
    }

    static final class CustomCrosshair extends Module {
        private final Setting size = num("Groesse", 6, 2, 20, 1);
        private final Setting gap = num("Luecke", 3, 0, 10, 1);

        CustomCrosshair() {
            super("Custom Crosshair", Category.VISUALS, "Eigenes Fadenkreuz in Gold (Vanilla-Kreuz bleibt darunter sichtbar)");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            int cx = mc.getWindow().getGuiScaledWidth() / 2, cy = mc.getWindow().getGuiScaledHeight() / 2;
            int s = size.asInt(), ga = gap.asInt();
            int c = ClickGui.ACCENT;
            g.fill(cx - ga - s, cy, cx - ga, cy + 1, c);
            g.fill(cx + ga + 1, cy, cx + ga + s + 1, cy + 1, c);
            g.fill(cx, cy - ga - s, cx + 1, cy - ga, c);
            g.fill(cx, cy + ga + 1, cx + 1, cy + ga + s + 1, c);
        }
    }

    static final class CustomFov extends Module {
        private final Setting fov = num("Sichtfeld", 100, 30, 110, 1);
        private int old = -1;

        CustomFov() {
            super("Custom Fov", Category.VISUALS, "Stellt das Sichtfeld ein (30 bis 110)");
        }

        @Override
        public void onEnable() {
            old = mc.options.fov().get();
        }

        @Override
        public void onTick() {
            mc.options.fov().set(fov.asInt());
        }

        @Override
        public void onDisable() {
            if (old > 0) {
                mc.options.fov().set(old);
            }
        }
    }

    static final class BlockOverlay extends Module {
        BlockOverlay() {
            super("Block Overlay", Category.VISUALS, "Goldener Rahmen um den Block, auf den du schaust");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr != null && mc.hitResult instanceof BlockHitResult h && h.getType() == HitResult.Type.BLOCK) {
                Draw.box3d(g, pr, new AABB(h.getBlockPos()), ClickGui.ACCENT);
            }
        }
    }

    static final class TargetEsp extends Module {
        TargetEsp() {
            super("Target ESP", Category.VISUALS, "Roter Rahmen um das Lebewesen im Fadenkreuz");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            if (pr != null && mc.hitResult instanceof EntityHitResult r && r.getEntity() instanceof LivingEntity le) {
                Draw.box3d(g, pr, Util.smoothBox(le, pt), 0xFFFF3B3B);
            }
        }
    }

    static final class HitParticles extends Module {
        private boolean wasSwinging;

        HitParticles() {
            super("Hit Particles", Category.VISUALS, "Extra Partikel, wenn du ein Lebewesen triffst");
        }

        @Override
        public void onTick() {
            boolean sw = mc.player.swinging;
            if (sw && !wasSwinging && mc.hitResult instanceof EntityHitResult r) {
                Entity e = r.getEntity();
                Random rnd = new Random();
                for (int i = 0; i < 10; i++) {
                    mc.level.addParticle(ParticleTypes.CRIT, e.getX() + rnd.nextGaussian() * 0.3, e.getY() + e.getBbHeight() * 0.6,
                        e.getZ() + rnd.nextGaussian() * 0.3, rnd.nextGaussian() * 0.2, 0.2, rnd.nextGaussian() * 0.2);
                }
            }
            wasSwinging = sw;
        }
    }

    static final class JumpCircles extends Module {
        private final List<double[]> circles = new ArrayList<>();
        private boolean wasOnGround = true;

        JumpCircles() {
            super("Jump Circles", Category.VISUALS, "Goldener Ring auf dem Boden bei jedem Sprung");
        }

        @Override
        public void onTick() {
            boolean on = mc.player.onGround();
            if (wasOnGround && !on && mc.player.getDeltaMovement().y > 0.2) {
                circles.add(new double[] {mc.player.getX(), mc.player.getY(), mc.player.getZ(), System.currentTimeMillis()});
            }
            wasOnGround = on;
        }

        @Override
        public void onDisable() {
            circles.clear();
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            Proj pr = Draw.proj(pt);
            Iterator<double[]> it = circles.iterator();
            while (it.hasNext()) {
                double[] c = it.next();
                double age = (System.currentTimeMillis() - c[3]) / 1000.0;
                if (age > 0.9) {
                    it.remove();
                    continue;
                }
                if (pr == null) {
                    continue;
                }
                double r = 0.3 + age * 1.8;
                int color = ((int) ((1 - age / 0.9) * 255) << 24) | 0xE0A526;
                int n = 24;
                for (int i = 0; i < n; i++) {
                    double a0 = Math.PI * 2 * i / n, a1 = Math.PI * 2 * (i + 1) / n;
                    double[] s = pr.segment(c[0] + Math.cos(a0) * r, c[1] + 0.03, c[2] + Math.sin(a0) * r,
                        c[0] + Math.cos(a1) * r, c[1] + 0.03, c[2] + Math.sin(a1) * r);
                    if (s != null) {
                        Draw.line(g, s[0], s[1], s[2], s[3], color);
                    }
                }
            }
        }
    }

    static final class Particles extends Module {
        private final Setting amount = num("Menge", 2, 1, 10, 1);
        private final Random rnd = new Random();

        Particles() {
            super("Particles", Category.VISUALS, "Schwebende Partikel um dich herum");
        }

        @Override
        public void onTick() {
            for (int i = 0; i < amount.asInt(); i++) {
                mc.level.addParticle(ParticleTypes.END_ROD, mc.player.getX() + rnd.nextGaussian() * 3, mc.player.getY() + rnd.nextDouble() * 3,
                    mc.player.getZ() + rnd.nextGaussian() * 3, 0, 0.01, 0);
            }
        }
    }

    static final class TargetSpirit extends Module {
        TargetSpirit() {
            super("Target Spirit", Category.VISUALS, "Leuchtende Partikel kreisen um das Ziel im Fadenkreuz");
        }

        @Override
        public void onTick() {
            if (mc.hitResult instanceof EntityHitResult r) {
                Entity e = r.getEntity();
                double t = System.currentTimeMillis() / 250.0;
                for (int i = 0; i < 3; i++) {
                    double a = t + i * Math.PI * 2 / 3;
                    mc.level.addParticle(ParticleTypes.END_ROD, e.getX() + Math.cos(a) * 0.8, e.getY() + e.getBbHeight() / 2 + Math.sin(t * 2 + i) * 0.5,
                        e.getZ() + Math.sin(a) * 0.8, 0, 0, 0);
                }
            }
        }
    }

    /** Farbfilter ueber dem ganzen Bild (kein echtes Shader-Color-Grading). */
    static final class ColorGrading extends Module {
        private final Setting red = num("Rot", 255, 0, 255, 5);
        private final Setting green = num("Gruen", 180, 0, 255, 5);
        private final Setting blue = num("Blau", 60, 0, 255, 5);
        private final Setting strength = num("Staerke %", 12, 1, 60, 1);

        ColorGrading() {
            super("Color Grading", Category.VISUALS, "Farbfilter ueber dem Bild (Rot/Gruen/Blau und Staerke einstellbar)");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            int a = (int) (strength.get() * 2.55);
            int c = (a << 24) | (red.asInt() << 16) | (green.asInt() << 8) | blue.asInt();
            g.fill(0, 0, mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight(), c);
        }
    }

    private VisualModules() {
    }
}
