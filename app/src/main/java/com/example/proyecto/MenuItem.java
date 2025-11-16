package com.example.proyecto;

public class MenuItem {
    private int icon;
    private String title;
    private int color;

    public MenuItem(int icon, String title, int color) {
        this.icon = icon;
        this.title = title;
        this.color = color;
    }

    public int getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public int getColor() {
        return color;
    }
}
