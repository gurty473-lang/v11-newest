package dev.motions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MotionsConfig {
    public static final int[] ACCENTS = {0xFFA3E635, 0xFF22D3EE, 0xFFA78BFA, 0xFFFB923C, 0xFFF472B6};
    /** Glass tints: smoke, ocean, violet, rose, emerald, amber. */
    public static final int[] GLASS = {0xFF0B0D11, 0xFF0C2D55, 0xFF2B1657, 0xFF54152F, 0xFF0C3F2D, 0xFF4F320A};

    public boolean notifications = true;
    public boolean showAuctionHud = true;
    public boolean animations = true;
    public float animSpeed = 1.0f;
    public int accent = ACCENTS[0];
    public boolean sounds = true;
    public boolean blur = true; // real menu background blur (can be turned off for more FPS)
    public int blurLevel = 8;   // 1..10 (Minecraft's own default is 5)
    public boolean copyWinner = true; // copy the winner's name to the clipboard when an auction ends
    public int glass = 0;       // index into GLASS
    public float hudX = 0.5f, hudY = 0f, hudScale = 1f; // card position (0..1 across free space) and size

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("motions-client.json");
    private static MotionsConfig inst = new MotionsConfig();

    public static MotionsConfig get() { return inst; }

    public int glassTint() { return GLASS[Math.max(0, Math.min(GLASS.length - 1, glass))]; }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                MotionsConfig c = GSON.fromJson(Files.readString(FILE), MotionsConfig.class);
                if (c != null) inst = c;
            }
        } catch (Exception e) {
            inst = new MotionsConfig();
        }
        inst.animSpeed = Math.max(0.5f, Math.min(2f, inst.animSpeed));
        inst.hudScale = Math.max(0.5f, Math.min(1.5f, inst.hudScale <= 0 ? 1f : inst.hudScale));
        inst.hudX = Math.max(0f, Math.min(1f, inst.hudX));
        inst.blurLevel = Math.max(1, Math.min(10, inst.blurLevel <= 0 ? 8 : inst.blurLevel));
        inst.hudY = Math.max(0f, Math.min(1f, inst.hudY));
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(inst));
        } catch (IOException ignored) {
        }
    }
}
