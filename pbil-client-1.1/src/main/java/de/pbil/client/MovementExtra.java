package de.pbil.client;

import java.util.List;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

final class MovementExtra {
    static void register(List<Module> list) {
        list.add(new AirStuck());
        list.add(new BoatFly());
        list.add(new EntitySpeed());
        list.add(new ElytraFly());
        list.add(new Jesus());
        list.add(new Sneak());
        list.add(new Spider());
    }

    static final class AirStuck extends Module {
        AirStuck() {
            super("AirStuck", Category.MOVEMENT, "Haelt dich in der Luft fest");
        }

        @Override
        public void onTick() {
            mc.player.setDeltaMovement(Vec3.ZERO);
        }
    }

    static final class BoatFly extends Module {
        private final Setting speed = num("Tempo", 0.6, 0.1, 2.0, 0.1);

        BoatFly() {
            super("BoatFly", Category.MOVEMENT, "Fliegen im Boot (Leertaste hoch, Shift runter)");
        }

        @Override
        public void onTick() {
            Entity v = mc.player.getVehicle();
            if (v == null || !v.getType().is(EntityTypeTags.BOATS)) {
                return;
            }
            Vec3 look = mc.player.getLookAngle();
            double h = MovementModules.moving() ? speed.get() : 0;
            double y = mc.options.keyJump.isDown() ? 0.4 : mc.options.keyShift.isDown() ? -0.4 : 0.0;
            v.setDeltaMovement(look.x * h, y, look.z * h);
        }
    }

    static final class EntitySpeed extends Module {
        private final Setting factor = num("Faktor", 1.5, 1.0, 4.0, 0.1);

        EntitySpeed() {
            super("EntitySpeed", Category.MOVEMENT, "Schneller auf Reittieren und Booten");
        }

        @Override
        public void onTick() {
            Entity v = mc.player.getVehicle();
            if (v != null && MovementModules.moving()) {
                Vec3 d = v.getDeltaMovement();
                v.setDeltaMovement(d.x * factor.get(), d.y, d.z * factor.get());
            }
        }
    }

    static final class ElytraFly extends Module {
        private final Setting speed = num("Tempo", 1.6, 0.5, 4.0, 0.1);

        ElytraFly() {
            super("ElytraFly", Category.MOVEMENT, "Steuert die Elytra in Blickrichtung (W gedrueckt halten)");
        }

        @Override
        public void onTick() {
            if (mc.player.isFallFlying() && mc.options.keyUp.isDown()) {
                mc.player.setDeltaMovement(mc.player.getLookAngle().scale(speed.get()));
            }
        }
    }

    static final class Jesus extends Module {
        Jesus() {
            super("Jesus", Category.MOVEMENT, "Auf Wasser und Lava treiben (Shift zum Tauchen)");
        }

        @Override
        public void onTick() {
            if ((mc.player.isInWater() || mc.player.isInLava()) && !mc.player.isShiftKeyDown()) {
                Vec3 d = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(d.x, 0.12, d.z);
            }
        }
    }

    static final class Sneak extends Module {
        Sneak() {
            super("Sneak", Category.MOVEMENT, "Schleicht dauerhaft");
        }

        @Override
        public void onTick() {
            mc.options.keyShift.setDown(true);
        }

        @Override
        public void onDisable() {
            mc.options.keyShift.setDown(false);
        }
    }

    static final class Spider extends Module {
        Spider() {
            super("Spider", Category.MOVEMENT, "Klettert an Waenden hoch");
        }

        @Override
        public void onTick() {
            if (mc.player.horizontalCollision && MovementModules.moving()) {
                Vec3 d = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(d.x, 0.2, d.z);
            }
        }
    }

    private MovementExtra() {
    }
}
