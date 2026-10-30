package com.dinedemand;

/**
 * Main application entry point for DineOn-Demand POS.
 */
public class DineOnDemandApp {
    public static void main(String[] args) {
        // Enable anti-aliasing for fonts across Linux/Wayland and X11
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        com.dinedemand.ui.DineOnDemandApp.main(args);
    }
}
