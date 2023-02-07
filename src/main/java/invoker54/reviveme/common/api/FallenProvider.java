package invoker54.reviveme.common.api;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FallenProvider implements ICapabilitySerializable<Tag> {
    public static final byte COMPOUND_NBT_ID = new CompoundTag().getId();

    public FallenProvider(Level level){
        fallenCapability = new FallenCapability(level);
    }

    //region Capability setup
    //This is where all of the fallen capability data is
    public static Capability<FallenCapability> FALLENDATA = CapabilityManager.get(new CapabilityToken<>() {});

    private final static String FALLEN_NBT = "fallenData";
    //This is where the current capability is stored to read and write
    private FallenCapability fallenCapability;
    private final LazyOptional<FallenCapability> optionalData = LazyOptional.of(() -> fallenCapability);
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        return FALLENDATA.orEmpty(capability, this.optionalData);
//
//
//        if (FALLENDATA == capability) {
//            return LazyOptional.of(() -> fallenCapability).cast();
//            // why are we using a lambda?  Because LazyOptional.of() expects a NonNullSupplier interface.  The lambda automatically
//            //   conforms itself to that interface.  This save me having to define an inner class implementing NonNullSupplier.
//            // The explicit cast to LazyOptional<T> is required because our CAPABILITY_ELEMENTAL_FIRE can't be typed.  Our code has
//            //   checked that the requested capability matches, so the explict cast is safe (unless you have mixed them up)
//        }
//
//        return LazyOptional.empty();
//
//
//        //return LazyOptional.empty();
//        // Note that if you are implementing getCapability in a derived class which implements ICapabilityProvider
//        // eg you have added a new MyEntity which has the method MyEntity::getCapability instead of using AttachCapabilitiesEvent to attach a
//        // separate class, then you should call
//        // return super.getCapability(capability, facing);
//        //   instead of
//        // return LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
//        CompoundTag nbtData = new CompoundTag();
//        Tag fallenNBT = FALLENDATA.writeNBT(fallenCapability, null);
//        nbtData.put(FALLEN_NBT, fallenNBT);
//        return  nbtData;
        return this.fallenCapability.writeNBT();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
//        if (nbt.getId() != COMPOUND_NBT_ID) {
//            //System.out.println("Unexpected NBT type:"+nbt);
//            return;  // leave as default in case of error
//        }
//        //System.out.println("I ran for deserializing");
//        CompoundTag nbtData = (CompoundTag) nbt;
//
//        FALLENDATA.readNBT(fallenCapability, null, nbtData.getCompound(FALLEN_NBT));
        this.fallenCapability.readNBT(nbt);
    }
}
