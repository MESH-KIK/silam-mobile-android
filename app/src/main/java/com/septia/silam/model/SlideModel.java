package com.septia.silam.model;

public class SlideModel {
    private String tagline;
    private String title;
    private String description;
    private int iconResId;
    private boolean hasNotaAction;

    public SlideModel(String tagline, String title, String description, int iconResId, boolean hasNotaAction) {
        this.tagline = tagline;
        this.title = title;
        this.description = description;
        this.iconResId = iconResId;
        this.hasNotaAction = hasNotaAction;
    }

    public String getTagline() { return tagline; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconResId() { return iconResId; }
    public boolean isHasNotaAction() { return hasNotaAction; }
}