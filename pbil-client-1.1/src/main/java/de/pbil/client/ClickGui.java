package de.pbil.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Hauptmenue: pro Kategorie ein Panel, Klick auf ein Modul schaltet es um, ">" oeffnet die Einstellungen. Mausrad scrollt. */
public class ClickGui extends Screen {
    static final int ACCENT = 0xFFE0A526;
    private static final int ROW = 14;
    private static final int GAP = 4;
    private static final int TOP = 18;

    private static int scroll;

    private int panelW;
    private int x0;
    private int listTop;

    public ClickGui() {
        super(Component.literal("Pbil Client"));
    }

    static Button hit(int x, int y, int w, int h, Runnable action) {
        Button b = Button.builder(Component.empty(), btn -> action.run()).bounds(x, y, w, h).build();
        b.setAlpha(0f);
        return b;
    }

    private int contentHeight() {
        int max = 0;
        for (Category c : Category.values()) {
            max = Math.max(max, Modules.in(c).size());
        }
        return max * ROW;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - (this.height - listTop - 4));
    }

    @Override
    protected void init() {
        Category[] cats = Category.values();
        panelW = Math.max(70, Math.min(112, (this.width - 8) / cats.length - GAP));
        int total = cats.length * (panelW + GAP) - GAP;
        x0 = (this.width - total) / 2;
        listTop = TOP + ROW + 2;
        scroll = Math.max(0, Math.min(scroll, maxScroll()));
        for (int i = 0; i < cats.length; i++) {
            int x = x0 + i * (panelW + GAP);
            int y = listTop - scroll;
            for (Module m : Modules.in(cats[i])) {
                final Module mod = m;
                if (y >= listTop && y + ROW <= this.height) {
                    int w = m.settings.isEmpty() ? panelW : panelW - 12;
                    this.addRenderableWidget(hit(x, y, w, ROW, mod::toggle));
                    if (!m.settings.isEmpty()) {
                        this.addRenderableWidget(hit(x + panelW - 12, y, 12, ROW,
                            () -> this.minecraft.setScreen(new SettingsScreen(this, mod))));
                    }
                }
                y += ROW;
            }
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int old = scroll;
        scroll = (int) Math.max(0, Math.min(maxScroll(), scroll - verticalAmount * ROW));
        if (scroll != old) {
            this.rebuildWidgets();
        }
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        Category[] cats = Category.values();
        String hint = "Klick: an/aus    >: Einstellungen    Mausrad: scrollen    ESC: schliessen";
        g.drawString(this.font, hint, (this.width - this.font.width(hint)) / 2, 5, 0xFF9AA0AA, false);
        String tip = null;
        g.enableScissor(0, listTop, this.width, this.height);
        for (int i = 0; i < cats.length; i++) {
            int x = x0 + i * (panelW + GAP);
            int y = listTop - scroll;
            for (Module m : Modules.in(cats[i])) {
                if (y + ROW > listTop && y < this.height) {
                    boolean hover = mouseX >= x && mouseX < x + panelW && mouseY >= Math.max(y, listTop) && mouseY < y + ROW;
                    int bg = m.enabled ? ACCENT : 0xD0141820;
                    if (hover) {
                        bg = m.enabled ? 0xFFF2BE47 : 0xE0262C38;
                        tip = m.desc;
                    }
                    g.fill(x, y, x + panelW, y + ROW, bg);
                    g.drawString(this.font, m.name, x + 4, y + 3, m.enabled ? 0xFF15110A : 0xFFC4C9D2, false);
                    if (!m.settings.isEmpty()) {
                        g.drawString(this.font, ">", x + panelW - 9, y + 3, m.enabled ? 0xFF15110A : 0xFFE0A526, false);
                    }
                }
                y += ROW;
            }
        }
        g.disableScissor();
        for (int i = 0; i < cats.length; i++) {
            int x = x0 + i * (panelW + GAP);
            g.fill(x, TOP, x + panelW, TOP + ROW, 0xF0171309);
            g.fill(x, TOP + ROW - 1, x + panelW, TOP + ROW, ACCENT);
            g.drawString(this.font, cats[i].label, x + 4, TOP + 3, 0xFFFFFFFF, true);
        }
        if (tip != null) {
            int w = this.font.width(tip);
            int tx = Math.max(2, Math.min(mouseX + 8, this.width - w - 6));
            g.fill(tx - 3, mouseY + 8, tx + w + 3, mouseY + 20, 0xF0000000);
            g.drawString(this.font, tip, tx, mouseY + 10, 0xFFFFFFFF, false);
        }
    }
}
