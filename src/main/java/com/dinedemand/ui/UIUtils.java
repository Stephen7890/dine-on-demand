package com.dinedemand.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared styling, font registration, color palette, and image caching utilities.
 */
public class UIUtils {
    // Colors
    public static final Color PRIMARY_BG = Color.WHITE;
    public static final Color SECONDARY_BG = new Color(248, 250, 252);
    public static final Color SIDEBAR_BG = new Color(15, 23, 42); // Deep slate navy
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 59);
    public static final Color SIDEBAR_ACTIVE = new Color(51, 65, 85);

    public static final Color ACCENT_COLOR = new Color(15, 23, 42);
    public static final Color ACCENT_PRIMARY = new Color(37, 99, 235); // Modern Royal Blue
    public static final Color ACCENT_LIGHT = new Color(239, 246, 255);

    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(71, 85, 105);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);

    public static final Color BORDER_LIGHT = new Color(226, 232, 240);
    public static final Color CARD_BG = Color.WHITE;

    public static final Color SUCCESS_GREEN = new Color(22, 163, 74);
    public static final Color SUCCESS_BG = new Color(240, 253, 244);

    public static final Color WARNING_AMBER = new Color(217, 119, 6);
    public static final Color WARNING_BG = new Color(254, 252, 232);

    public static final Color DANGER_RED = new Color(220, 38, 38);
    public static final Color DANGER_BG = new Color(254, 242, 242);

    // Fonts
    public static Font GEIST_BOLD;
    public static Font GEIST_SEMIBOLD;
    public static Font GEIST_MEDIUM;
    public static Font GEIST_REGULAR;

    public static Font FONT_HERO;
    public static Font FONT_TITLE;
    public static Font FONT_SUBTITLE;
    public static Font FONT_BODY_BOLD;
    public static Font FONT_BODY;
    public static Font FONT_SMALL_BOLD;
    public static Font FONT_SMALL;

    private static final Map<String, ImageIcon> IMAGE_CACHE = new ConcurrentHashMap<>();

    static {
        loadFonts();
    }

    private static void loadFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            GEIST_BOLD = loadFontFromClasspath("/fonts/Geist-Bold.ttf", 24f);
            GEIST_SEMIBOLD = loadFontFromClasspath("/fonts/Geist-SemiBold.ttf", 18f);
            GEIST_MEDIUM = loadFontFromClasspath("/fonts/Geist-Medium.ttf", 14f);
            GEIST_REGULAR = loadFontFromClasspath("/fonts/Geist-Regular.ttf", 14f);

            if (GEIST_BOLD != null) ge.registerFont(GEIST_BOLD);
            if (GEIST_SEMIBOLD != null) ge.registerFont(GEIST_SEMIBOLD);
            if (GEIST_MEDIUM != null) ge.registerFont(GEIST_MEDIUM);
            if (GEIST_REGULAR != null) ge.registerFont(GEIST_REGULAR);

            Font baseBold = (GEIST_BOLD != null) ? GEIST_BOLD : new Font("Segoe UI", Font.BOLD, 24);
            Font baseSemi = (GEIST_SEMIBOLD != null) ? GEIST_SEMIBOLD : new Font("Segoe UI", Font.BOLD, 18);
            Font baseMed = (GEIST_MEDIUM != null) ? GEIST_MEDIUM : new Font("Segoe UI", Font.BOLD, 14);
            Font baseReg = (GEIST_REGULAR != null) ? GEIST_REGULAR : new Font("Segoe UI", Font.PLAIN, 14);

            FONT_HERO = baseBold.deriveFont(28f);
            FONT_TITLE = baseBold.deriveFont(20f);
            FONT_SUBTITLE = baseSemi.deriveFont(16f);
            FONT_BODY_BOLD = baseMed.deriveFont(14f);
            FONT_BODY = baseReg.deriveFont(14f);
            FONT_SMALL_BOLD = baseMed.deriveFont(12f);
            FONT_SMALL = baseReg.deriveFont(12f);

        } catch (Exception e) {
            System.err.println("Fallback to system fonts: " + e.getMessage());
            e.printStackTrace();
            FONT_HERO = new Font("Segoe UI", Font.BOLD, 28);
            FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
            FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
            FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 14);
            FONT_BODY = new Font("Segoe UI", Font.PLAIN, 14);
            FONT_SMALL_BOLD = new Font("Segoe UI", Font.BOLD, 12);
            FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
        }
    }

    private static Font loadFontFromClasspath(String resourcePath, float size) {
        try (InputStream is = UIUtils.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            }
        } catch (Exception e) {
            System.err.println("Could not load font " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static ImageIcon getScaledImage(String path, int width, int height) {
        String cacheKey = path + "@" + width + "x" + height;
        if (IMAGE_CACHE.containsKey(cacheKey)) {
            return IMAGE_CACHE.get(cacheKey);
        }

        ImageIcon icon = null;
        try {
            if (path != null && !path.trim().isEmpty()) {
                String cleanPath = path.startsWith("/") ? path : "/" + path;
                java.net.URL imgUrl = UIUtils.class.getResource(cleanPath);
                if (imgUrl != null) {
                    ImageIcon rawIcon = new ImageIcon(imgUrl);
                    Image scaled = rawIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    icon = new ImageIcon(scaled);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image " + path + ": " + e.getMessage());
            e.printStackTrace();
        }

        if (icon == null) {
            icon = createPlaceholderIcon(width, height, "Meal");
        }

        IMAGE_CACHE.put(cacheKey, icon);
        return icon;
    }

    public static ImageIcon createPlaceholderIcon(int width, int height, String label) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(241, 245, 249));
        g2.fillRoundRect(0, 0, width, height, 16, 16);

        g2.setColor(new Color(148, 163, 184));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, width / 6)));
        FontMetrics fm = g2.getFontMetrics();
        int x = (width - fm.stringWidth(label)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(label, x, y);
        g2.dispose();
        return new ImageIcon(img);
    }
}
