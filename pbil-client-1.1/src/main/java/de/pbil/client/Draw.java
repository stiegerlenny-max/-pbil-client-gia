package de.pbil.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Zeichen-Helfer fuer ESP, Tracer und Nametags (alles 2D mit fill, daher unabhaengig von der Render-Pipeline). */
final class Draw {
    /** Projektion fuer das aktuelle Bild, null in der 3rd-Person-Ansicht. */
    static Proj proj(float partial) {
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if (p == null || !mc.options.getCameraType().isFirstPerson()) {
            return null;
        }
        var eye = p.getEyePosition(partial);
        return new Proj(eye.x, eye.y, eye.z, p.getViewYRot(partial), p.getViewXRot(partial),
            mc.options.fov().get(), mc.getWindow().getGuiScaledWidth(), mc.getWindow().getGuiScaledHeight());
    }

    static void outline(GuiGraphics g, double x0, double y0, double x1, double y1, int color) {
        int a = (int) Math.floor(x0), b = (int) Math.floor(y0), c = (int) Math.ceil(x1), d = (int) Math.ceil(y1);
        if (c - a > 4000 || d - b > 4000) {
            return;
        }
        g.fill(a, b, c, b + 1, color);
        g.fill(a, d - 1, c, d, color);
        g.fill(a, b, a + 1, d, color);
        g.fill(c - 1, b, c, d, color);
    }

    static void line(GuiGraphics g, double x0, double y0, double x1, double y1, int color) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth(), sh = mc.getWindow().getGuiScaledHeight();
        double dx = x1 - x0, dy = y1 - y0;
        int n = (int) Math.min(2000, Math.max(Math.abs(dx), Math.abs(dy)) / 2);
        for (int i = 0; i <= n; i++) {
            double t = n == 0 ? 0 : (double) i / n;
            int x = (int) Math.round(x0 + dx * t), y = (int) Math.round(y0 + dy * t);
            if (x < -2 || y < -2 || x > sw || y > sh) {
                continue;
            }
            g.fill(x, y, x + 2, y + 2, color);
        }
    }

    static void centered(GuiGraphics g, String text, double cx, double y, int color) {
        Minecraft mc = Minecraft.getInstance();
        int w = mc.font.width(text);
        g.fill((int) cx - w / 2 - 2, (int) y - 1, (int) cx + w / 2 + 2, (int) y + 9, 0x90000000);
        g.drawString(mc.font, text, (int) cx - w / 2, (int) y, color, false);
    }

    private static final int[][] EDGES = {
        {0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    /** Echte 3D-Hitbox (12 Kanten), auch wenn die Kamera nah dran oder innerhalb der Box ist. */
    static void box3d(GuiGraphics g, Proj pr, AABB bb, int color) {
        double[][] c = new double[8][];
        for (int i = 0; i < 8; i++) {
            c[i] = new double[] {(i & 1) == 0 ? bb.minX : bb.maxX, (i & 2) == 0 ? bb.minY : bb.maxY, (i & 4) == 0 ? bb.minZ : bb.maxZ};
        }
        for (int[] e : EDGES) {
            double[] s = pr.segment(c[e[0]][0], c[e[0]][1], c[e[0]][2], c[e[1]][0], c[e[1]][1], c[e[1]][2]);
            if (s != null) {
                line(g, s[0], s[1], s[2], s[3], color);
            }
        }
    }

    /** Kasten-ESP um eine Welt-Box. Gibt das Bildschirmrechteck zurueck (oder null). */
    static double[] box(GuiGraphics g, Proj pr, AABB bb, int color) {
        double[] r = pr.box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
        if (r != null) {
            outline(g, r[0], r[1], r[2], r[3], color);
        }
        return r;
    }

    private Draw() {
    }
}
