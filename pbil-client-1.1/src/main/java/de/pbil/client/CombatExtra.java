package de.pbil.client;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

final class CombatExtra {
    static void register(List<Module> list) {
        list.add(new Hitboxes());
        list.add(new AimAssist());
        list.add(new AutoArmor());
    }

    /** Vergroessert die Hitboxen aller Spieler auf deinem Client (zum Treffen und im ESP sichtbar). */
    static final class Hitboxes extends Module {
        private final Setting width = num("Breite +", 0.3, 0.0, 1.5, 0.1);
        private final Setting height = num("Hoehe +", 0.0, 0.0, 1.0, 0.1);

        Hitboxes() {
            super("Hitboxes", Category.COMBAT, "Vergroessert die Hitboxen der Spieler");
        }

        @Override
        public void onTick() {
            for (Entity e : Util.entities(x -> x instanceof Player)) {
                e.setBoundingBox(e.getDimensions(e.getPose()).makeBoundingBox(e.position()).inflate(width.get(), height.get(), width.get()));
            }
        }

        @Override
        public void onDisable() {
            if (mc.level == null) {
                return;
            }
            for (Entity e : Util.entities(x -> x instanceof Player)) {
                e.setBoundingBox(e.getDimensions(e.getPose()).makeBoundingBox(e.position()));
            }
        }
    }

    static final class AimAssist extends Module {
        private final Setting range = num("Reichweite", 5.0, 2.0, 8.0, 0.5);
        private final Setting fov = num("Winkel", 60, 10, 180, 5);
        private final Setting smooth = num("Staerke", 0.25, 0.05, 1.0, 0.05);

        AimAssist() {
            super("AimAssist", Category.COMBAT, "Zieht das Fadenkreuz sanft zum naechsten Spieler");
        }

        @Override
        public void onTick() {
            Entity t = Util.nearest(e -> e instanceof Player && e.isAlive(), range.get());
            if (t == null) {
                return;
            }
            Vec3 eye = mc.player.getEyePosition();
            Vec3 c = t.getBoundingBox().getCenter();
            double dx = c.x - eye.x, dy = c.y - eye.y, dz = c.z - eye.z;
            double flat = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, flat));
            float dyaw = Mth.wrapDegrees(yaw - mc.player.getYRot());
            float dpitch = pitch - mc.player.getXRot();
            if (Math.abs(dyaw) > fov.get()) {
                return;
            }
            mc.player.setYRot(mc.player.getYRot() + dyaw * (float) smooth.get());
            mc.player.setXRot(Mth.clamp(mc.player.getXRot() + dpitch * (float) smooth.get(), -90f, 90f));
        }
    }

    static final class AutoArmor extends Module {
        private int wait;

        AutoArmor() {
            super("AutoArmor", Category.COMBAT, "Zieht fehlende Ruestung aus dem Inventar an");
        }

        @Override
        public void onTick() {
            if (wait > 0) {
                wait--;
                return;
            }
            if (mc.player.containerMenu != mc.player.inventoryMenu) {
                return;
            }
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
            for (EquipmentSlot es : slots) {
                if (!mc.player.getItemBySlot(es).isEmpty()) {
                    continue;
                }
                for (int i = 0; i < 36; i++) {
                    ItemStack st = mc.player.getInventory().getItem(i);
                    var eq = st.get(DataComponents.EQUIPPABLE);
                    if (eq != null && eq.slot() == es && !st.is(Items.ELYTRA)) {
                        int slot = i < 9 ? 36 + i : i;
                        mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId, slot, 0, ClickType.QUICK_MOVE, mc.player);
                        wait = 4;
                        return;
                    }
                }
            }
        }
    }

    private CombatExtra() {
    }
}
