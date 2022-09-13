package invoker54.reviveme.common.api;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FallenProvider implements ICapabilitySerializable<INBT> {
    public static final byte COMPOUND_NBT_ID = new CompoundNBT().getId();

    public FallenProvider(World level){
        fallenCapability = new FallenCapability(level);
    }

    //region Capability setup
    //This is where all of the fallen capability data is
    @CapabilityInject(FallenCapability.class)
    public static Capability<FallenCapability> FALLENDATA = null;

    private final static String FALLEN_NBT = "fallenData";

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {


        if (FALLENDATA == capability) {
            return LazyOptional.of(() -> fallenCapability).cast();
            // why are we using a lambda?  Because LazyOptional.of() expects a NonNullSupplier interface.  The lambda automatically
            //   conforms itself to that interface.  This save me having to define an inner class implementing NonNullSupplier.
            // The explicit cast to LazyOptional<T> is required because our CAPABILITY_ELEMENTAL_FIRE can't be typed.  Our code has
            //   checked that the requested capability matches, so the explict cast is safe (unless you have mixed them up)
        }

        return LazyOptional.empty();


        //return LazyOptional.empty();
        // Note that if you are implementing getCapability in a derived class which implements ICapabilityProvider
        // eg you have added a new MyEntity which has the method MyEntity::getCapability instead of using AttachCapabilitiesEvent to attach a
        // separate class, then you should call
        // return super.getCapability(capability, facing);
        //   instead of
        // return LazyOptional.empty();
    }

    @Override
    public INBT serializeNBT() {
        CompoundNBT nbtData = new CompoundNBT();
        INBT fallenNBT = FALLENDATA.writeNBT(fallenCapability, null);
        nbtData.put(FALLEN_NBT, fallenNBT);
        return  nbtData;
    }

    @Override
    public void deserializeNBT(INBT nbt) {
        if (nbt.getId() != COMPOUND_NBT_ID) {
            //System.out.println("Unexpected NBT type:"+nbt);
            return;  // leave as default in case of error
        }
        //System.out.println("I ran for deserializing");
        CompoundNBT nbtData = (CompoundNBT) nbt;
        FALLENDATA.readNBT(fallenCapability, null, nbtData.getCompound(FALLEN_NBT));
    }

    //This is where the current capability is stored to read and write
    private FallenCapability fallenCapability;

}
