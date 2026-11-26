package de.pbil.client;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Spieler-ESP: 3D-Hitbox, die dem Gegner folgt, roter Chunk bis zur untersten Ebene (Bedrock) und
 * dauerhafte Koordinaten (inkl. Hoehe) aller gesehenen Spieler. Taste O loescht die Liste.
 */
final class PlayerEsp extends Module {
    private static final int CYAN = 0xFF3BD1FF;
    private static final int RED = 0xFFFF2B2B;
    private static final Map<String, double[]> MARKS = new LinkedHashMap<>();

    private final Setting chunk = bool("Roter Chunk", true);
    private final Setting coords = bool("Koordinaten merken", true);
    private final Setting items = bool("Items ueber Kopf", true);
    private final Setting health = bool("Lebensbalken", true);

    PlayerEsp() {
        super("PlayerESP", Category.RENDER, "3D-Hitbox, roter Chunk bis Bedrock, Koordinaten + Items ueber dem Kopf");
    }

    static void clearMarks() {
        MARKS.clear();
    }

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND,
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Override
    public void onRender(GuiGraphics g, float pt) {
        int n = 0;
        Proj pr = Draw.proj(pt);
        for (Entity e : Util.entities(x -> x instanceof Player && x.isAlive())) {
            if (coords.on()) {
                MARKS.put(e.getName().getString(), new double[] {e.getX(), e.getY(), e.getZ()});
            }
            if (pr == null || n++ >= 25) {
                continue;
            }
            AABB bb = Util.smoothBox(e, pt);
            if (chunk.on()) {
                int cx = Math.floorDiv((int) Math.floor(e.getX()), 16) * 16;
                int cz = Math.floorDiv((int) Math.floor(e.getZ()), 16) * 16;
                int minY = mc.level.dimensionType().minY();
                Draw.box3d(g, pr, new AABB(cx, minY, cz, cx + 16, bb.maxY + 1, cz + 16), RED);
            }
            Draw.box3d(g, pr, bb, CYAN);
            double[] r = pr.box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            if (r != null && health.on() && e instanceof LivingEntity le) {
                double frac = Math.max(0, Math.min(1, le.getHealth() / Math.max(1f, le.getMaxHealth())));
                int hc = frac > 0.6 ? 0xFF3CE05A : frac > 0.3 ? 0xFFFFC832 : 0xFFFF3C3C;
                int x = (int) r[0] - 4;
                int top = (int) r[1], bottom = (int) r[3];
                g.fill(x, top, x + 2, bottom, 0xA0000000);
                g.fill(x, bottom - (int) ((bottom - top) * frac), x + 2, bottom, hc);
            }
            if (items.on() && e instanceof LivingEntity le) {
                double[] head = pr.project((bb.minX + bb.maxX) / 2, bb.maxY + 0.3, (bb.minZ + bb.maxZ) / 2);
                if (head != null) {
                    int x = (int) head[0] - SLOTS.length * 9;
                    int y = (int) head[1] - 36;
                    for (EquipmentSlot s : SLOTS) {
                        ItemStack st = le.getItemBySlot(s);
                        if (!st.isEmpty()) {
                            g.renderItem(st, x, y);
                            g.renderItemDecorations(mc.font, st, x, y);
                        }
                        x += 18;
                    }
                }
            }
        }
        if (!MARKS.isEmpty()) {
            int y = 70;
            g.drawString(mc.font, "Spieler gefunden (O = loeschen)", 4, y, ClickGui.ACCENT, true);
            y += 10;
            for (Map.Entry<String, double[]> en : MARKS.entrySet()) {
                double[] c = en.getValue();
                String s = String.format(java.util.Locale.ROOT, "%s  X %d  Y %d  Z %d", en.getKey(), (int) c[0], (int) c[1], (int) c[2]);
                g.drawString(mc.font, s, 4, y, 0xFFFF6B6B, true);
                y += 10;
            }
        }
    }
}
