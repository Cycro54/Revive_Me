package invoker54.reviveme.init;

import invoker54.reviveme.common.capability.FallenData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

import static invoker54.reviveme.ReviveMe.MOD_ID;

public class AttachmentTypesInit {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    // Serialization via INBTSerializable
    public static final Supplier<AttachmentType<FallenData>> FALLEN_DATA = ATTACHMENT_TYPES.register(
            "fallen_data", () -> AttachmentType.serializable(FallenData::new).build()
    );

    public static void registerAttachments(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }

}
