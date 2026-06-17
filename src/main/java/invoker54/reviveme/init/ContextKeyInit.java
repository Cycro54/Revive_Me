package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;

public class ContextKeyInit {
    public static ContextKey<Float> bodyRotContext = new ContextKey<>(Identifier.fromNamespaceAndPath(ReviveMe.MOD_ID, "saved_body_rotation"));

}
