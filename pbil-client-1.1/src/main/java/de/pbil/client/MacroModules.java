package de.pbil.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Chat-Makros: Jede Zeile in config/pbil_macros.txt (Name=Text) wird ein Modul in der Kategorie Macro.
 * Anschalten sendet den Text (mit / am Anfang als Befehl) und schaltet das Modul wieder aus.
 */
final class MacroModules {
    private static final String DEFAULT = String.join("\n",
        "# Name=Text oder Befehl (Befehle beginnen mit /). Nach dem Bearbeiten Minecraft neu starten.",
        "GG=gg",
        "Hallo=Hallo zusammen!",
        "Spawn=/spawn",
        "Home=/home",
        "Auktion=/ah",
        "Shop=/shop",
        "") + "\n";

    static void register(List<Module> list) {
        Path f = FabricLoader.getInstance().getConfigDir().resolve("pbil_macros.txt");
        try {
            if (!Files.isRegularFile(f)) {
                Files.createDirectories(f.getParent());
                Files.writeString(f, DEFAULT, StandardCharsets.UTF_8);
            }
            int n = 0;
            for (String line : Files.readAllLines(f, StandardCharsets.UTF_8)) {
                line = line.trim();
                int eq = line.indexOf('=');
                if (line.isEmpty() || line.startsWith("#") || eq <= 0 || eq == line.length() - 1 || n >= 30) {
                    continue;
                }
                list.add(new Macro(line.substring(0, eq).trim(), line.substring(eq + 1).trim()));
                n++;
            }
        } catch (IOException e) {
            PbilClient.LOGGER.warn("Makros konnten nicht geladen werden", e);
        }
    }

    static final class Macro extends Module {
        private final String text;

        Macro(String name, String text) {
            super("Macro " + name, Category.MACRO, text);
            this.text = text;
        }

        @Override
        public void onEnable() {
            if (mc.player == null) {
                return;
            }
            if (text.startsWith("/")) {
                mc.player.connection.sendCommand(text.substring(1));
            } else {
                mc.player.connection.sendChat(text);
            }
            setEnabled(false);
        }
    }

    private MacroModules() {
    }
}
