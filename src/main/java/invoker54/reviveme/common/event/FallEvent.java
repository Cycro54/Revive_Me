package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    //First, stop the death event.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void InterruptDeath(LivingHurtEvent event){

        if (event.getEntityLiving() instanceof ServerPlayerEntity){

            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            FallenCapability instance = FallenCapability.GetFallCap(event.getEntityLiving());

            //If they are in creative mode, don't bother with any of this.
            if (player.isCreative()) return;

            if (player.getHealth() - event.getAmount() <= 0){
                if (instance.isFallen() == false) {

                    //Set to fallen state
                    instance.setFallen(true);

                    //Set health to 1
                    player.setHealth(1);

                    //Set food to 0
                    player.getFoodData().setFoodLevel(0);

                    //Set last damage source for later
                    instance.setDamageSource(event.getSource());

                    //Set time left to whatever is in config file
                    instance.SetTimeLeft((int) player.level.getGameTime(), ReviveMeConfig.timeLeft);

                    //Set penalty type and amount
                    instance.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount);
                    //System.out.println(ReviveMeConfig.penaltyType);

                    //Make them invulnerable to all damage (besides void and creative of course.)
                    player.setInvulnerable(true);

                    //Cancel the event so the player doesn't end up being killed
                    if (event.isCancelable())
                        event.setCanceled(true);

                    //Make it so they can't move very fast.
                    player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

                    //Dismount the player if riding something
                    player.stopRiding();

                    //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());

                    //Finally send capability code to all players
                    CompoundNBT nbt = new CompoundNBT();
                    nbt.put(player.getStringUUID(),instance.writeNBT());
                    NetworkHandler.INSTANCE.send((PacketDistributor.ALL.noArg()), new SyncClientCapMsg(nbt, ""));
                }
            }
        }
    }
}
