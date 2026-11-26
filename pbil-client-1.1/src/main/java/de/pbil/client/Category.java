package de.pbil.client;

public enum Category {
    COMBAT("Combat"),
    MISC("Misc"),
    DONUT("Donut"),
    BASE("Base Finding"),
    CLIENT("Pbil Client"),
    MOVEMENT("Movement"),
    MACRO("Macro"),
    RENDER("Render"),
    VISUALS("Visuals");

    public final String label;

    Category(String label) {
        this.label = label;
    }
}
