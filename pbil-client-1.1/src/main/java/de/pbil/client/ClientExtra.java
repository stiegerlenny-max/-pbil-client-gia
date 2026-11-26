package de.pbil.client;

import java.util.List;

import net.minecraft.client.ParticleStatus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

final class ClientExtra {
    static void register(List<Module> list) {
        list.add(new Radar());
        list.add(new FpsBoost());
    }

    /** Draufsicht auf Spieler in der Naehe (oben = deine Blickrichtung). */
    static final class Radar extends Module {
        private final Setting range = num("Reichweite", 48, 16, 128, 8);

        Radar() {
            super("Radar", Category.CLIENT, "Radar mit Spielern in der Naehe");
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            int size = 64;
            int x0 = 4, y0 = mc.getWindow().getGuiScaledHeight() / 2 - 100;
            g.fill(x0, y0, x0 + size, y0 + size, 0x90000000);
            g.fill(x0 + size / 2, y0, x0 + size / 2 + 1, y0 + size, 0x40FFFFFF);
            g.fill(x0, y0 + size / 2, x0 + size, y0 + size / 2 + 1, 0x40FFFFFF);
            double yaw = Math.toRadians(mc.player.getYRot());
            double fx = -Math.sin(yaw), fz = Math.cos(yaw), rx = -Math.cos(yaw), rz = -Math.sin(yaw);
            double scale = (size / 2.0 - 2) / range.get();
            for (Entity e : Util.entities(x -> x instanceof Player && x.isAlive())) {
                double dx = e.getX() - mc.player.getX(), dz = e.getZ() - mc.player.getZ();
                double sx = (dx * rx + dz * rz) * scale, sy = -(dx * fx + dz * fz) * scale;
                if (Math.abs(sx) > size / 2.0 - 2 || Math.abs(sy) > size / 2.0 - 2) {
                    continue;
                }
                int px = x0 + size / 2 + (int) sx, py = y0 + size / 2 + (int) sy;
                g.fill(px - 1, py - 1, px + 2, py + 2, 0xFFFF3B3B);
            }
            g.fill(x0 + size / 2 - 1, y0 + size / 2 - 1, x0 + size / 2 + 2, y0 + size / 2 + 2, ClickGui.ACCENT);
        }
    }

    /** Stellt Optionen auf "schnell" und setzt sie beim Ausschalten zurueck. */
    static final class FpsBoost extends Module {
        private final Setting dist = num("Sichtweite", 8, 2, 32, 1);
        private int oldDist = -1;
        private double oldEntity = -1;
        private ParticleStatus oldParticles;

        FpsBoost() {
            super("FPS Boost", Category.CLIENT, "Weniger Sichtweite, weniger Partikel und Entities fuer mehr FPS");
        }

        @Override
        public void onEnable() {
            oldDist = mc.options.renderDistance().get();
            oldEntity = mc.options.entityDistanceScaling().get();
            oldParticles = mc.options.particles().get();
            apply();
        }

        private void apply() {
            mc.options.renderDistance().set(dist.asInt());
            mc.options.entityDistanceScaling().set(0.5);
            mc.options.particles().set(ParticleStatus.MINIMAL);
        }

        @Override
        public void onTick() {
            if (mc.options.renderDistance().get() != dist.asInt()) {
                mc.options.renderDistance().set(dist.asInt());
            }
        }

        @Override
        public void onDisable() {
            if (oldDist > 0) {
                mc.options.renderDistance().set(oldDist);
                mc.options.entityDistanceScaling().set(oldEntity);
                mc.options.particles().set(oldParticles);
            }
        }
    }

    private ClientExtra() {
    }
}
