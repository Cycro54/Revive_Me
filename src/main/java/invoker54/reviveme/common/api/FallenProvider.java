package invoker54.reviveme.common.api;//package invoker54.reviveme.common.api;
//
//import invoker54.reviveme.common.capability.FallenData;
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.level.Level;
//import net.neoforged.neoforge.capabilities.ICapabilityProvider;
//import org.jetbrains.annotations.NotNull;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//public class FallenProvider implements ICapabilityProvider<Player, Void, FallenData> {
//    public static final byte COMPOUND_NBT_ID = new CompoundTag().getId();
//
//    public FallenProvider(Level level){
//        FallenData = new FallenData(level);
//    }
//
//    //region Capability setup
//    //This is where all of the fallen capability data is
//    public static Capability<FallenData> FALLENDATA = CapabilityManager.get(new CapabilityToken<>() {});
//
//    private final static String FALLEN_NBT = "fallenData";
//    //This is where the current capability is stored to read and write
//    private FallenData FallenData;
//    private final LazyOptional<FallenData> optionalData = LazyOptional.of(() -> FallenData);
//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
//        return FALLENDATA.orEmpty(capability, this.optionalData);
////
////
////        if (FALLENDATA == capability) {
////            return LazyOptional.of(() -> FallenData).cast();
////            // why are we using a lambda?  Because LazyOptional.of() expects a NonNullSupplier interface.  The lambda automatically
////            //   conforms itself to that interface.  This save me having to define an inner class implementing NonNullSupplier.
////            // The explicit cast to LazyOptional<T> is required because our CAPABILITY_ELEMENTAL_FIRE can't be typed.  Our code has
////            //   checked that the requested capability matches, so the explict cast is safe (unless you have mixed them up)
////        }
////
////        return LazyOptional.empty();
////
////
////        //return LazyOptional.empty();
////        // Note that if you are implementing getCapability in a derived class which implements ICapabilityProvider
////        // eg you have added a new MyEntity which has the method MyEntity::getCapability instead of using AttachCapabilitiesEvent to attach a
////        // separate class, then you should call
////        // return super.getCapability(capability, facing);
////        //   instead of
////        // return LazyOptional.empty();
//    }
//
//    @Override
//    public Tag serializeNBT() {
////        CompoundTag nbtData = new CompoundTag();
////        Tag fallenNBT = FALLENDATA.writeNBT(FallenData, null);
////        nbtData.put(FALLEN_NBT, fallenNBT);
////        return  nbtData;
//        return this.FallenData.writeNBT();
//    }
//
//    @Override
//    public void deserializeNBT(Tag nbt) {
////        if (nbt.getId() != COMPOUND_NBT_ID) {
////            //System.out.println("Unexpected NBT type:"+nbt);
////            return;  // leave as default in case of error
////        }
////        //System.out.println("I ran for deserializing");
////        CompoundTag nbtData = (CompoundTag) nbt;
////
////        FALLENDATA.readNBT(FallenData, null, nbtData.getCompound(FALLEN_NBT));
//        this.FallenData.readNBT(nbt);
//    }
//
//    @Override
//    public @org.jetbrains.annotations.Nullable FallenData getCapability(@NotNull Player o, Void unused) {
//        return o.getCapability();
//    }
//}
