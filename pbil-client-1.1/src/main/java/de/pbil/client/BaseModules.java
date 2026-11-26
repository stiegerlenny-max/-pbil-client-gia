package de.pbil.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.level.chunk.LevelChunk;

/** Base-Suche: zaehlt Block-Entities (Kisten, Spawner, Ofen ...) pro geladenem Chunk. */
final class BaseModules {
    static void register(List<Module> list) {
        list.add(new ChunkScanner());
        list.add(new SusChunkFinder());
    }

    abstract static class ChunkList extends Module {
        private final Setting chunks = num("Chunk-Radius", 8, 2, 16, 1);
        final Setting min;
        private List<String> lines = new ArrayList<>();
        private int ticks;

        ChunkList(String name, String desc, double defaultMin) {
            super(name, Category.BASE, desc);
            this.min = num("Mindestens", defaultMin, 1, 200, 1);
        }

        abstract int count(LevelChunk chunk);

        @Override
        public void onTick() {
            if (ticks++ % 20 != 0) {
                return;
            }
            record Hit(int cx, int cz, int n) {
            }
            List<Hit> hits = new ArrayList<>();
            int cx0 = mc.player.getBlockX() >> 4;
            int cz0 = mc.player.getBlockZ() >> 4;
            int r = chunks.asInt();
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    LevelChunk ch = mc.level.getChunkSource().getChunkNow(cx0 + dx, cz0 + dz);
                    if (ch == null) {
                        continue;
                    }
                    int n = count(ch);
                    if (n >= min.get()) {
                        hits.add(new Hit(cx0 + dx, cz0 + dz, n));
                    }
                }
            }
            hits.sort(Comparator.comparingInt((Hit h) -> -h.n()));
            List<String> out = new ArrayList<>();
            for (int i = 0; i < Math.min(8, hits.size()); i++) {
                Hit h = hits.get(i);
                out.add(String.format(java.util.Locale.ROOT, "Chunk %d %d  (X %d Z %d)  %d Bloecke", h.cx(), h.cz(), h.cx() * 16 + 8, h.cz() * 16 + 8, h.n()));
            }
            lines = out;
        }

        @Override
        public void onDisable() {
            lines = new ArrayList<>();
        }

        @Override
        public void onRender(GuiGraphics g, float pt) {
            if (lines.isEmpty()) {
                return;
            }
            int y = mc.getWindow().getGuiScaledHeight() / 2 + 60;
            g.drawString(mc.font, name, 4, y, ClickGui.ACCENT, true);
            for (String s : lines) {
                y += 10;
                g.drawString(mc.font, s, 4, y, 0xFFFFFFFF, true);
            }
        }
    }

    static final class ChunkScanner extends ChunkList {
        ChunkScanner() {
            super("Chunk Scanner", "Listet Chunks mit vielen Block-Entities (Kisten, Spawner, Ofen ...)", 8);
        }

        @Override
        int count(LevelChunk chunk) {
            return chunk.getBlockEntities().size();
        }
    }

    static final class SusChunkFinder extends ChunkList {
        SusChunkFinder() {
            super("Sus Chunk Finder", "Listet Chunks mit auffaellig vielen Lagerbloecken (Basen/Stashes)", 12);
        }

        @Override
        int count(LevelChunk chunk) {
            int n = 0;
            for (var be : chunk.getBlockEntities().values()) {
                if (RenderModules.isStorage(be.getBlockState())) {
                    n++;
                }
            }
            return n;
        }
    }

    private BaseModules() {
    }
}
