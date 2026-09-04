package com.queso.niche.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.queso.niche.Constants;
import com.queso.niche.weapon.Augment;
import com.queso.niche.weapon.WeaponBuild;
import com.queso.niche.weapon.WeaponComponentType;
import com.queso.niche.weapon.WeaponPartItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class WeaponTextureCompositor {

    public static final Identifier BASE_LAYER =
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/weapon/musket_base.png");

    public enum LayerSet implements StringRepresentable {
        WEAPON("weapon", "textures/weapon/", "textures/weapon/musket.png"),
        ICON("icon", "textures/item/musket/", "textures/item/musket.png");

        public static final Codec<LayerSet> CODEC = StringRepresentable.fromEnum(LayerSet::values);

        private final String name;
        private final String root;
        private final List<Identifier> fallbackLayers;

        LayerSet(String name, String root, String fallback) {
            this.name = name;
            this.root = root;
            this.fallbackLayers = List.of(Identifier.fromNamespaceAndPath(Constants.MOD_ID, fallback));
        }

        public Identifier part(WeaponComponentType slot, String partPath) {
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID, root + slot.id() + "/" + partPath + ".png");
        }

        public Identifier augment(Augment augment) {
            return Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                    root + "augments/" + augment.layerName() + ".png");
        }

        public List<Identifier> fallbackLayers() {
            return fallbackLayers;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private static final int KEY_MIN_R = 200, KEY_MAX_G = 60, KEY_MAX_B = 60;

    private static final int A_SHIFT = 24, R_SHIFT = 16, G_SHIFT = 8, B_SHIFT = 0;

    private static final int CACHE_CAP = 64;

    private static final float MODEL_Z_FRONT = 8.5F / 16.0F, MODEL_Z_BACK = 7.5F / 16.0F;

    private static final Map<List<Identifier>, Composited> CACHE =
            new LinkedHashMap<>(16, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<List<Identifier>, Composited> eldest) {
                    if (size() > CACHE_CAP) {
                        Minecraft.getInstance().getTextureManager().release(eldest.getValue().texture());
                        return true;
                    }
                    return false;
                }
            };

    private static final Map<LayerKey, List<Identifier>> LAYERS_CACHE =
            new LinkedHashMap<>(16, 0.75F, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<LayerKey, List<Identifier>> eldest) {
                    return size() > CACHE_CAP;
                }
            };

    private record LayerKey(WeaponBuild build, LayerSet set) {}

    private static long nextTextureId = 0L;

    private WeaponTextureCompositor() {}

    public record MeshQuad(float[] pos, float[] uv, float nx, float ny, float nz) {}

    public record Composited(Identifier texture, List<MeshQuad> quads) {}

    public static Identifier layerTexture(WeaponPartItem part) {
        return layerTexture(LayerSet.WEAPON, part);
    }

    public static Identifier layerTexture(LayerSet set, WeaponPartItem part) {
        Identifier partId = BuiltInRegistries.ITEM.getKey(part);
        return set.part(part.slot(), partId.getPath());
    }

    public static boolean allPresent(List<Identifier> layers) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        for (Identifier layer : layers) {
            if (rm.getResource(layer).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public static Identifier composite(List<Identifier> layers) {
        Composited c = composited(layers);
        return c == null ? null : c.texture();
    }

    public static Composited composited(List<Identifier> layers) {
        Composited cached = CACHE.get(layers);
        if (cached != null) {
            return cached;
        }

        ResourceManager rm = Minecraft.getInstance().getResourceManager();
        NativeImage result = null;
        try {
            for (Identifier layer : layers) {
                Optional<Resource> res = rm.getResource(layer);
                if (res.isEmpty()) {
                    continue;
                }
                try (InputStream in = res.get().open()) {
                    try (NativeImage src = NativeImage.read(NativeImage.Format.RGBA, in)) {
                        if (result == null) {
                            result = new NativeImage(src.getWidth(), src.getHeight(), true);
                        }
                        stamp(src, result);
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOG.error("Failed compositing weapon texture for {}", layers, e);
            if (result != null) {
                result.close();
            }
            return null;
        }

        if (result == null) {
            return null;
        }

        List<MeshQuad> quads = buildMesh(result);

        Identifier id = Identifier.fromNamespaceAndPath(Constants.MOD_ID,
                "weapon_composite/" + Long.toHexString(nextTextureId++));
        Minecraft.getInstance().getTextureManager()
                .register(id, new DynamicTexture(() -> "niche weapon composite", result));
        Composited c = new Composited(id, quads);
        CACHE.put(List.copyOf(layers), c);
        return c;
    }

    public static List<Identifier> resolvedLayers(WeaponBuild build) {
        return resolvedLayers(build, LayerSet.WEAPON);
    }

    public static List<Identifier> resolvedLayers(WeaponBuild build, LayerSet set) {
        LayerKey key = new LayerKey(build, set);
        List<Identifier> cached = LAYERS_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        List<Identifier> layers = layersFor(build.resolveParts(), set);
        Augment augment = build.augment();
        if (augment != null) {
            layers.add(set.augment(augment));
        }
        List<Identifier> resolved = !layers.isEmpty() && allPresent(layers) ? List.copyOf(layers) : List.of();
        LAYERS_CACHE.put(key, resolved);
        return resolved;
    }

    private static List<MeshQuad> buildMesh(NativeImage img) {
        int w = img.getWidth(), h = img.getHeight();
        boolean[] opaque = new boolean[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                opaque[y * w + x] = ((img.getPixel(x, y) >>> A_SHIFT) & 0xFF) != 0;
            }
        }

        List<MeshQuad> quads = new ArrayList<>();
        float zf = MODEL_Z_FRONT, zb = MODEL_Z_BACK;
        quads.add(new MeshQuad(
                new float[]{0, 0, zf, 1, 0, zf, 1, 1, zf, 0, 1, zf},
                new float[]{0, 1, 1, 1, 1, 0, 0, 0}, 0, 0, 1));
        quads.add(new MeshQuad(
                new float[]{0, 0, zb, 0, 1, zb, 1, 1, zb, 1, 0, zb},
                new float[]{0, 1, 0, 0, 1, 0, 1, 1}, 0, 0, -1));

        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                if (!opaque[py * w + px]) {
                    continue;
                }
                float xL = (float) px / w, xR = (float) (px + 1) / w;
                float yT = 1.0F - (float) py / h, yB = 1.0F - (float) (py + 1) / h;
                float u = (px + 0.5F) / w, v = (py + 0.5F) / h;
                if (isTransparent(opaque, w, h, px, py - 1)) {
                    wall(quads, xL, yT, zf, xR, yT, zb, u, v, 0, 1, 0);
                }
                if (isTransparent(opaque, w, h, px, py + 1)) {
                    wall(quads, xL, yB, zf, xR, yB, zb, u, v, 0, -1, 0);
                }
                if (isTransparent(opaque, w, h, px - 1, py)) {
                    wall(quads, xL, yB, zf, xL, yT, zb, u, v, -1, 0, 0);
                }
                if (isTransparent(opaque, w, h, px + 1, py)) {
                    wall(quads, xR, yB, zf, xR, yT, zb, u, v, 1, 0, 0);
                }
            }
        }
        return quads;
    }

    private static boolean isTransparent(boolean[] opaque, int w, int h, int x, int y) {
        return x < 0 || y < 0 || x >= w || y >= h || !opaque[y * w + x];
    }

    private static void wall(List<MeshQuad> quads, float x0, float y0, float z0, float x1, float y1, float z1,
                             float u, float v, float nx, float ny, float nz) {
        float[] pos = {x0, y0, z0, x1, y1, z0, x1, y1, z1, x0, y0, z1};
        float[] uv = {u, v, u, v, u, v, u, v};
        quads.add(new MeshQuad(pos, uv, nx, ny, nz));
    }

    private static void stamp(NativeImage src, NativeImage dst) {
        int w = Math.min(src.getWidth(), dst.getWidth());
        int h = Math.min(src.getHeight(), dst.getHeight());
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getPixel(x, y);
                int a = (argb >>> A_SHIFT) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int r = (argb >>> R_SHIFT) & 0xFF;
                int g = (argb >>> G_SHIFT) & 0xFF;
                int b = (argb >>> B_SHIFT) & 0xFF;
                if (isRedKey(r, g, b)) {
                    continue;
                }
                dst.setPixel(x, y, argb);
            }
        }
    }

    private static boolean isRedKey(int r, int g, int b) {
        return r >= KEY_MIN_R && g <= KEY_MAX_G && b <= KEY_MAX_B;
    }

    public static List<Identifier> layersFor(List<WeaponPartItem> parts) {
        return layersFor(parts, LayerSet.WEAPON);
    }

    public static List<Identifier> layersFor(List<WeaponPartItem> parts, LayerSet set) {
        List<Identifier> layers = new ArrayList<>();
        for (WeaponComponentType slot : WeaponComponentType.values()) {
            for (WeaponPartItem part : parts) {
                if (part.slot() == slot) {
                    layers.add(layerTexture(set, part));
                    break;
                }
            }
        }
        return layers;
    }

    public static void clear() {
        var tm = Minecraft.getInstance().getTextureManager();
        for (Composited c : CACHE.values()) {
            tm.release(c.texture());
        }
        CACHE.clear();
        LAYERS_CACHE.clear();
    }
}
