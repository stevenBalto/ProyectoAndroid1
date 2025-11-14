package com.example.proyecto;

// Modelo para cada ítem del menú
public class MenuItem {
    private int icon; // Recurso drawable del ícono
    private String title; // Título del ítem

    public MenuItem(int icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }
}
