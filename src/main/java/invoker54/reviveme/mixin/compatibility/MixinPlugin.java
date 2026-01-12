package invoker54.reviveme.mixin.compatibility;

import net.minecraftforge.fml.loading.FMLLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String s) {

    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        //MrCrayFish mod
        if (mixinClassName.contains("invoker54.reviveme.mixin.compatibility.ControllableButtonBindingMixin")){
            return targetClassName.contains("com.mrcrayfish.controllable.client.binding.ButtonBinding");
        }
        return true;
    }

    public static boolean isLoaded(String modid) {
        boolean isLoaded = FMLLoader.getLoadingModList().getModFileById(modid) != null;
        //System.out.println(modid+" : "+isLoaded);
        return isLoaded;
    }

    @Override
    public void acceptTargets(Set<String> set, Set<String> set1) {

    }

    @Override
    public List<String> getMixins() {
        return Collections.emptyList();
    }

    @Override
    public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }

    @Override
    public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {

    }
}
