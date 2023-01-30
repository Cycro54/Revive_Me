package invoker54.reviveme.common.capability;

import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.UUID;

public class FallenCapability {

    public static final String FALLEN_BOOL = "isFallenREVIVE";
    public static final String FELL_START_INT = "fellStartREVIVE";
    public static final String FELL_END_FLOAT = "fellEndREVIVE";
    public static final String REVIVE_START_INT = "revStartREVIVE";
    public static final String REVIVE_END_INT = "revEndREVIVE";
    public static final String PENALTY_ENUM = "penaltyTypeREVIVE";
    public static final String PENALTY_FLOAT = "penaltyIntREVIVE";
    public static final String OTHERPLAYER_UUID = "otherPlayerREVIVE";
    //endregion

    public FallenCapability(World level){
        this.level = level;
    }
    public FallenCapability() {

    }

    protected World level;
    protected int revStart = 0;
    protected int revEnd = 0;
    protected int fellStart = 0;
    protected float fellEnd = 0;
    protected DamageSource damageSource = DamageSource.OUT_OF_WORLD;
    protected boolean isFallen = false;
    protected UUID otherPlayer = null;
    protected float penaltyAmount = 0;
    protected PENALTYPE penaltyType = PENALTYPE.HEALTH;
    public enum PENALTYPE {
        NONE,
        HEALTH,
        EXPERIENCE,
        FOOD
    }

    public static FallenCapability GetFallCap(LivingEntity player){
        return player.getCapability(FallenProvider.FALLENDATA).orElseGet(FallenCapability::new);
    }

    public void setPenalty(PENALTYPE type, float amount){
        this.penaltyAmount = amount;
        this.penaltyType = type;
    }

    public float getPenaltyAmount(LivingEntity player){
        float actualAmount = penaltyAmount;
        switch (penaltyType){
            case NONE:
                break;
            case HEALTH:
                if (actualAmount > 0 && actualAmount < 1){
                    actualAmount *= player.getMaxHealth();
                }
                break;
            case EXPERIENCE:
                if (actualAmount > 0 && actualAmount < 1){
                    actualAmount *= ((PlayerEntity)player).totalExperience;
                }
                break;
            case FOOD:
                if (actualAmount > 0 && actualAmount < 1){
                    actualAmount *= 40;
                }
        }

        return Math.round(actualAmount);
    }

    public PENALTYPE getPenaltyType(){
        return penaltyType;
    }

    public boolean hasEnough(PlayerEntity player){
        switch (penaltyType) {
            case NONE:
                return true;
            case HEALTH:
                return player.getHealth() > this.penaltyAmount;
            case EXPERIENCE:
                return player.experienceLevel > this.penaltyAmount;
            case FOOD:
                return (player.getFoodData().getFoodLevel() + player.getFoodData().getSaturationLevel()) > this.penaltyAmount;
            default:
                return false;
        }
    }

    public void setDamageSource(DamageSource damageSource){
        this.damageSource = damageSource;
    }

    public DamageSource getDamageSource(){
        return damageSource;
    }

    public float GetTimeLeft(boolean divideByMax) {
        if (ReviveMeConfig.timeLeft == 0) return 1;

        if (divideByMax)
            return 1 - ((level.getGameTime() - fellStart)/(float) fellEnd);

        return ((fellStart + fellEnd) - level.getGameTime())/20f;
    }

    public boolean shouldDie(){
        return level.getGameTime() > (fellEnd + fellStart) && ReviveMeConfig.timeLeft != 0;
    }

    public void SetTimeLeft(int timeStart, float maxSeconds) {
            this.fellStart = timeStart;
            this.fellEnd = maxSeconds * 20;
            //System.out.println("Time left is!: " + (a/20f));
    }

    public void resumeFallTimer(){
        fellStart = (int) (level.getGameTime() - (revStart - fellStart));
    }

    public void ForceDeath() {
        SetTimeLeft(1,0);
    }

    public boolean isFallen() {
        return isFallen;
    }

    public void setFallen(boolean fallen) {
        this.isFallen = fallen;

        if (!fallen){
            setProgress(0, 1);
            SetTimeLeft(0, 1);
            setOtherPlayer(null);
        }
    }

    public UUID getOtherPlayer() {
        return otherPlayer;
    }

    public boolean compareUUID(UUID targUUID){
        if (targUUID == null) return false;

        if (getOtherPlayer() == null) return false;

        return getOtherPlayer().equals(targUUID);
    }

    public void setOtherPlayer(UUID playerID){
        otherPlayer = playerID;
    }

    public void setProgress(int timeStart, int seconds){
        this.revStart = timeStart;
        this.revEnd = seconds * 20;
    }

    public float getProgress() {
        return (level.getGameTime() - revStart) /(float)revEnd;
    }


    public INBT writeNBT(){
        CompoundNBT cNBT = new CompoundNBT();
        cNBT.putInt(FELL_START_INT, this.fellStart);
        cNBT.putFloat(FELL_END_FLOAT, this.fellEnd/20);
        cNBT.putBoolean(FALLEN_BOOL, this.isFallen);
        cNBT.putInt(REVIVE_START_INT, this.revStart);
        cNBT.putInt(REVIVE_END_INT, this.revEnd/20);
        cNBT.putString(PENALTY_ENUM, this.penaltyType.name());
        cNBT.putFloat(PENALTY_FLOAT, this.penaltyAmount);

        if(this.otherPlayer != null)
        cNBT.putUUID(OTHERPLAYER_UUID, this.otherPlayer);
        return cNBT;
    }
    public void readNBT(INBT nbt){
        CompoundNBT cNBT = (CompoundNBT) nbt;
        this.SetTimeLeft(cNBT.getInt(FELL_START_INT), cNBT.getInt(FELL_END_FLOAT));
        this.setFallen(cNBT.getBoolean(FALLEN_BOOL));
        this.setProgress(cNBT.getInt(REVIVE_START_INT), cNBT.getInt(REVIVE_END_INT));
        this.setPenalty(PENALTYPE.valueOf(cNBT.getString(PENALTY_ENUM)), cNBT.getInt(PENALTY_FLOAT));

        if(cNBT.hasUUID(OTHERPLAYER_UUID)) {
            this.setOtherPlayer(cNBT.getUUID(OTHERPLAYER_UUID));
        }
        else {
            this.setOtherPlayer(null);
        }
    }

    public static class FallenNBTStorage implements Capability.IStorage<FallenCapability> {

        @Nullable
        @Override
        public INBT writeNBT(Capability<FallenCapability> capability, FallenCapability instance, Direction side) {
            CompoundNBT cNBT = new CompoundNBT();
            cNBT.putInt(FELL_START_INT, instance.fellStart);
            cNBT.putFloat(FELL_END_FLOAT, instance.fellEnd/20);
            cNBT.putBoolean(FALLEN_BOOL, instance.isFallen());
            cNBT.putString(PENALTY_ENUM, instance.penaltyType.name());
            cNBT.putFloat(PENALTY_FLOAT, instance.penaltyAmount);
            return cNBT;
        }

        @Override
        public void readNBT(Capability<FallenCapability> capability, FallenCapability instance, Direction side, INBT nbt) {
            CompoundNBT cNBT = (CompoundNBT) nbt;
            instance.SetTimeLeft(cNBT.getInt(FELL_START_INT), cNBT.getInt(FELL_END_FLOAT));
            instance.setFallen(cNBT.getBoolean(FALLEN_BOOL));
            try {
                instance.setPenalty(PENALTYPE.valueOf(cNBT.getString(PENALTY_ENUM)), cNBT.getInt(PENALTY_FLOAT));
            }

            catch (Exception e){
                //System.out.println("Couldn't find PENALTY type, doing default...");
                instance.setPenalty(PENALTYPE.NONE,0);
            }
        }
    }
}
