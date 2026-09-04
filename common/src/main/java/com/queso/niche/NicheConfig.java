package com.queso.niche;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class NicheConfig {

    public static final class Values {
        public float recoilScale = 1.0F;
        public float zoomFactor = 0.4F;
        public boolean cameraSway = true;
        public float vignetteIntensity = 1.0F;
        public boolean abilitiesEnabled = true;
        public boolean tutorialShown = false;
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = Path.of("config", "niche.json");

    private static Values values = new Values();

    private NicheConfig() {}

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                try (Reader reader = Files.newBufferedReader(PATH)) {
                    Values loaded = GSON.fromJson(reader, Values.class);
                    if (loaded != null) {
                        values = loaded;
                    }
                }
            }
            save();
        } catch (Exception e) {
            Constants.LOG.error("Failed to load {} config; using defaults", Constants.MOD_ID, e);
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(PATH)) {
                GSON.toJson(values, writer);
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed to save {} config", Constants.MOD_ID, e);
        }
    }

    public static float recoilScale() {
        return Math.max(0.0F, values.recoilScale);
    }

    public static float zoomFactor() {
        return Math.max(0.1F, values.zoomFactor);
    }

    public static boolean cameraSway() {
        return values.cameraSway;
    }

    public static float vignetteIntensity() {
        return Math.max(0.0F, Math.min(1.0F, values.vignetteIntensity));
    }

    public static boolean abilitiesEnabled() {
        return values.abilitiesEnabled;
    }

    public static boolean tutorialShown() {
        return values.tutorialShown;
    }

    public static void markTutorialShown() {
        if (!values.tutorialShown) {
            values.tutorialShown = true;
            save();
        }
    }
}
