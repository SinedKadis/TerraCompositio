package net.sinedkadis.terracompositio.mixin;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;


public class MixinPlugin implements IMixinConfigPlugin {
    private static final Map<String, Predicate<Void>> MIXIN_CONDITIONS = new HashMap<>();

    static {
        MIXIN_CONDITIONS.put("BookGuiMixin", v -> isModLoaded("patchouli"));
    }

    private static boolean isModLoaded(String modId) {
        try {
            return switch (modId) {
                case "patchouli" -> {
                    Class.forName("vazkii.patchouli.forge.common.ForgeModInitializer");
                    yield true;
                }
                case "create" -> {
                    Class.forName("com.simibubi.create.Create");
                    yield true;
                }
                case "jei" -> {
                    Class.forName("mezz.jei.JustEnoughItems");
                    yield true;
                }
                default -> false;
            };
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Извлекаем простое имя миксина
        String simpleName = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);

        // Проверяем наличие специальных условий
        if (MIXIN_CONDITIONS.containsKey(simpleName)) {
            return MIXIN_CONDITIONS.get(simpleName).test(null);
        }

        // По умолчанию применяем миксин
        return true;
    }


    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
