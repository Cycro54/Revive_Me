package invoker54.reviveme.common.data;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.RefreshOptionsMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.GameRules;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ReviveItemData extends ReviveConfigData {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveItemData.class, ReviveMeConfig.debugMode);
    public static final HashMap<String, ReviveItemData> reviveItemMap = new HashMap<>();
    protected static final File itemFilepath = new File(getItemPath().toUri());
    protected static final File exampleFilepath = new File(getExamplePath().toUri());

    public static final String MAIN_REVIVE_ITEM_NBT = "mainReviveItemNBT";
    public static final String DISPLAY_NAME_STRING = "displayName";
    public static final String DESCRIPTION_STRING = "description";
    public static final String ITEM_RESOURCE_LOCATION_STRING = "itemResourceLocation";
    public static final String ITEM_TAG_STRING = "itemTag";
    public static final String NBT_DATA_STRING = "nbtData";
    public static final String ITEM_USER_ENUM = "itemUser";
    public static final String PARENT_ID_NAME_STRING = "parentIDName";
    public static final String REVIVE_SECONDS_DOUBLE = "reviveSeconds";
    public static final String REVIVE_HEALTH_DOUBLE = "reviveHealth";
    public static final String REVIVE_FOOD_DOUBLE = "reviveFood";
    public static final String PENALTY_TIMER_DOUBLE = "penaltyTimer";
    public static final String COUNT_REQUIRED_INT = "countRequired";
//    public static final String MAX_USES_INT = "maxUses";
    public static final String REVIVE_CHANCE_DOUBLE = "reviveChance";
    public static final String FALLEN_TIMER_CHANGE_INT = "fallenTimerChange";
    public static final String REFRESH_OPTIONS_BOOL = "refreshOptions";
    public static final String USE_REVIVE_ON_FAIL_BOOL = "useReviveOnFail";
    public static final String REVIVE_EFFECTS_LIST_STRING = "reviveEffects";
    public static final String REVIVE_COMMANDS_LIST_STRING = "reviveCommands";
    public static final String EXAMPLE_NAME_STRING = "EXAMPLE";
    public static final String EXAMPLE_VERSION_INT = "version";

    public CompoundNBT dataTag;

    private String idName = "";
    private ReviveItemData parentItem = null;
    private String itemResourceLocation = "";
    private String itemTag = "";
    private CompoundNBT nbtData = new CompoundNBT();
    private String displayName = "";
    private String description = "";
    private USER itemUser = USER.BOTH;
    public enum USER{
        REVIVER,
        FALLEN,
        BOTH,
        NONE
    }
    private double reviveSeconds = ReviveMeConfig.reviveTime;
    private int countRequired = 1;
//    private int maxUses = 0;
    private double reviveChance = 1;
    private int fallenTimerChange = 0;
    private boolean refreshOptions = false;
    private boolean useReviveOnFail = true;
    private List<String> reviveCommands = new ArrayList<>();

    public ReviveItemData(double revivedHealth, double revivedFood, double penaltyTimer, List<EffectInstance> reviveEffects, String idName, CompoundNBT dataTag) {
        super(revivedHealth, revivedFood, penaltyTimer, reviveEffects);
        this.dataTag = dataTag;
        AtomicBoolean hasRequiredEntry = new AtomicBoolean(false);

        this.runIfTagPresent(PARENT_ID_NAME_STRING, inbt -> {
            this.parentItem = reviveItemMap.get(inbt.getAsString());
            this.inherit(this.parentItem);
        });
        this.runIfTagPresent(REVIVE_HEALTH_DOUBLE, inbt -> this.setRevivedHealth(((NumberNBT)inbt).getAsDouble()));
        this.runIfTagPresent(REVIVE_FOOD_DOUBLE, inbt -> this.setRevivedFood(((NumberNBT)inbt).getAsDouble()));
        this.runIfTagPresent(PENALTY_TIMER_DOUBLE, inbt -> this.setFallenPenaltyTimer(((NumberNBT)inbt).getAsDouble()));

        this.idName = idName;
        this.runIfTagPresent(ITEM_RESOURCE_LOCATION_STRING, (inbt)-> {
            this.itemResourceLocation = inbt.getAsString();
            hasRequiredEntry.set(true);
        });
        this.runIfTagPresent(ITEM_TAG_STRING, (inbt)-> {
            this.itemTag = inbt.getAsString();
            hasRequiredEntry.set(true);
        });
        this.runIfTagPresent(NBT_DATA_STRING, (inbt ->{
            try {
                hasRequiredEntry.set(true);
                this.nbtData = JsonToNBT.parseTag(inbt.getAsString());
            }
            catch (CommandSyntaxException e) {
                if (!dataTag.getString(NBT_DATA_STRING).isEmpty()) {
                    LOGGER.error("[Revive Me!] There's a typo somewhere in 'nbtData' ("+this.idName+")");
                    LOGGER.error(e.getMessage());
                }
                this.nbtData = new CompoundNBT();
            }
        }));
        this.runIfTagPresent(DISPLAY_NAME_STRING, (inbt -> this.displayName = inbt.getAsString()));
        this.runIfTagPresent(DESCRIPTION_STRING, (inbt -> this.description = inbt.getAsString()));
        this.runIfTagPresent(ITEM_USER_ENUM, (inbt -> {
            try {
                this.itemUser = USER.valueOf(dataTag.getString(ITEM_USER_ENUM));
            }
            catch (Exception e){
                LOGGER.error("[Revive Me!] Incorrect User value ("+this.idName+"): " + dataTag.getString(ITEM_USER_ENUM));
                this.itemUser = USER.BOTH;
            }
        }));
        this.runIfTagPresent(REVIVE_SECONDS_DOUBLE, (inbt -> this.reviveSeconds = ((NumberNBT)inbt).getAsDouble()));
        this.runIfTagPresent(COUNT_REQUIRED_INT, (inbt -> this.countRequired = ((NumberNBT)inbt).getAsInt()));
//        this.runIfTagPresent(MAX_USES_INT, (inbt -> this.maxUses = ((NumberNBT)inbt).getAsInt()));
        this.runIfTagPresent(REVIVE_CHANCE_DOUBLE, (inbt -> this.reviveChance = ((NumberNBT)inbt).getAsDouble()));
        this.runIfTagPresent(FALLEN_TIMER_CHANGE_INT, (inbt -> this.fallenTimerChange = ((NumberNBT)inbt).getAsInt()));
        this.runIfTagPresent(REFRESH_OPTIONS_BOOL, (inbt -> this.refreshOptions = ((ByteNBT)inbt).getAsByte() != 0));
        this.runIfTagPresent(USE_REVIVE_ON_FAIL_BOOL, (inbt -> this.useReviveOnFail = ((ByteNBT)inbt).getAsByte() != 0));

        this.runIfTagPresent(REVIVE_EFFECTS_LIST_STRING, this::grabEffectsFromList);
        this.runIfTagPresent(REVIVE_COMMANDS_LIST_STRING, this::grabCommandsFromList);

        if (!hasRequiredEntry.get()){
            throw new NullPointerException("[Revive Me!] "+this.idName+" is missing one of the main entries!: 'itemResourceLocation' or 'itemTag', or 'nbtData'");
        }
    }

    public static ReviveItemData fromEntry(String idName, CompoundNBT itemTag, List<String> currentList, Map<String, CompoundNBT> itemMap){
        if (currentList.contains(idName)){
            StringBuilder builder = new StringBuilder();
            currentList.forEach(s -> {
                if (!Objects.equals(idName, s)) builder.append(s);
                else builder.append("[").append(s).append("]");

                builder.append("->");
            });
            builder.append("[").append(idName).append("]");
            throw new StackOverflowError(builder.toString());
        }
        currentList.add(idName);

        if (reviveItemMap.containsKey(idName)) return reviveItemMap.get(idName);


        String parentID = itemTag.getString(PARENT_ID_NAME_STRING);
        ReviveItemData parentData = reviveItemMap.get(parentID);

        if (!parentID.isEmpty() && parentData == null){
            parentData = fromEntry(parentID, itemMap.get(parentID), currentList, itemMap);
            reviveItemMap.put(parentData.getIdName(), parentData);
        }

        ReviveConfigData starterData = ReviveMeConfig.configReviveData;
        ReviveItemData itemData;
        try {
            itemData = new ReviveItemData(starterData.getRevivedHealth(), starterData.getRevivedFood(),
                    starterData.getFallenPenaltyTimer(), new ArrayList<>(), idName, itemTag);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }

        currentList.remove(idName);
        reviveItemMap.put(idName, itemData);

        return itemData;
    }

    public void grabEffectsFromList(INBT inbt) {
        ListNBT listNBT = ((ListNBT) inbt);
        List<String> stringList = new ArrayList<>();
        listNBT.forEach(s -> stringList.add(s.getAsString()));
        List<EffectInstance> instances = new ArrayList<>();
        for (String s : stringList) {
            if (s.equals("PARENT")) {
                if (this.parentItem == null) instances.addAll(ReviveMeConfig.configReviveData.getReviveEffects());
                else instances.addAll(this.parentItem.getReviveEffects());
                continue;
            }
            instances.add(ReviveConfigData.EffectFromString(s));
        }
        this.setReviveEffects(instances);
    }

    public void grabCommandsFromList(INBT inbt){
        ListNBT listNBT = ((ListNBT) inbt);
        List<String> stringList = new ArrayList<>();
        listNBT.forEach(s -> {
            if (s.getAsString().equals("PARENT")){
                if (this.parentItem == null) LOGGER.error("[Revive Me!] There is no parent! (" + this.getIdName() +")");
                else stringList.addAll(this.parentItem.getReviveCommands());

                return;
            }
            stringList.add(s.getAsString());
        });
        this.reviveCommands = stringList;
    }

    public void inherit(ReviveItemData parentData){
        this.itemUser = parentData.itemUser;
        this.reviveSeconds = parentData.reviveSeconds;
        this.setRevivedHealth(parentData.getRevivedHealth());
        this.setRevivedFood(parentData.getRevivedFood());
        this.setFallenPenaltyTimer(parentData.getFallenPenaltyTimer());
        this.countRequired = parentData.countRequired;
//        this.maxUses = parentData.maxUses;
    }

    public static List<ReviveItemData> filterList(List<ReviveItemData> dataList, USER itemUser){
        if (itemUser == USER.NONE) return new ArrayList<>();
        if (itemUser == USER.BOTH) return dataList;
        return dataList.stream().filter(data -> data.itemUser == USER.BOTH ||
                data.itemUser == itemUser).collect(Collectors.toList());
    }

    public static ReviveItemData getData(ItemStack stack, USER targetUser){
        if (stack == null) return null;

        List<ReviveItemData> itemDataList = new ArrayList<>(reviveItemMap.values());
        itemDataList = filterList(itemDataList, ReviveMeConfig.itemUser);
        itemDataList = filterList(itemDataList, targetUser);

        for (ReviveItemData data : itemDataList){
            //resource location
            if (!data.itemResourceLocation.isEmpty() &&
                    !stack.getItem().getRegistryName().toString().contains(data.itemResourceLocation)) continue;
            //item tag
            if (!data.itemTag.isEmpty()){
                boolean hasTag = stack.getItem().getTags().stream().anyMatch(resource -> resource.toString().contains(data.itemTag));
                if (!hasTag) continue;
            }
            //nbtData
            if (!data.nbtData.isEmpty()){
                ItemStack stack1 = new ItemStack(stack.getItem());
                stack1.getOrCreateTag().merge(data.nbtData);
                if (!ItemStack.tagMatches(stack1, stack)) continue;
            }
            return data;
        }
        return null;
    }

    public void runIfTagPresent(String key, Consumer<INBT> consumer){
        if (!this.dataTag.contains(key)) return;
        consumer.accept(this.dataTag.get(key));
    }

    public String getIdName() {
        return idName;
    }

    public InvoText getDisplayName() {
        InvoText displayText = InvoText.literal(this.displayName);
        if (this.displayName.contains("key:")){
            displayText = InvoText.translate(this.displayName.replace("key:", ""));
        }
        return displayText;
    }

    public InvoText getDescription() {
        InvoText descriptionText = InvoText.literal(this.description);
        if (this.description.contains("key:")){
            descriptionText = InvoText.translate(this.description.replace("key:", ""));
        }
        return descriptionText;
    }

    public String getItemResourceLocation() {
        return itemResourceLocation;
    }

    public String getItemTag() {
        return itemTag;
    }

    public CompoundNBT getNbtData() {
        return nbtData;
    }

    public USER getItemUser() {
        return itemUser;
    }

    public ReviveItemData getParent() {
        return this.parentItem;
    }

    public double getReviveSeconds() {
        return reviveSeconds;
    }

    public int getCountRequired() {
        return countRequired;
    }

//    public int getMaxUses() {
//        return maxUses;
//    }

    public double getReviveChance(){
        return this.reviveChance;
    }

    public int getFallenTimerChange(){
        return this.fallenTimerChange;
    }

    public boolean isRefreshOptions(){
        return this.refreshOptions;
    }

    public boolean useReviveOnFail(){
        return this.useReviveOnFail;
    }

    public List<String> getReviveCommands() {
        return reviveCommands;
    }

    public int getItemCount(PlayerEntity reviver, ItemStack chosenStack) {
        int count = 0;
        for (int a = 0; a < reviver.inventory.getContainerSize(); a++) {
            ItemStack containerStack = reviver.inventory.getItem(a);
            if (!chosenStack.sameItem(containerStack)) continue;
            if (!ItemStack.tagMatches(containerStack, chosenStack)) continue;
            count += containerStack.getCount();
        }
        return count;
    }

    public void takeItemCount(PlayerEntity reviver, ItemStack chosenStack) {
        int count = this.getCountRequired();
        for (int a = 0; a < reviver.inventory.getContainerSize(); a++) {
            ItemStack containerStack = reviver.inventory.getItem(a);
            if (!chosenStack.sameItem(containerStack)) continue;
            if (!ItemStack.tagMatches(containerStack, chosenStack)) continue;

            int removeAmount = Math.min(count, containerStack.getCount());
            containerStack.setCount(containerStack.getCount() - removeAmount);
            count -= removeAmount;
            if (count == 0) break;
        }
    }

    @Override
    public void revivePlayer(PlayerEntity fallen, boolean isCommand, PlayerEntity reviver, InvoText reviveText) {
        FallenCapability cap = FallenCapability.get(fallen);
        if (fallen.level.random.nextFloat() <= this.reviveChance || isCommand) {
            if (!this.getDisplayName().getString().isEmpty()){
                reviveText.append(InvoText.literal(" (").append(this.getDisplayName()).append(InvoText.literal(")")));
            }
            else{
                reviveText.append(InvoText.literal(" ("+this.idName+")"));
            }
            cap.incrementSelfReviveCount();
            super.revivePlayer(fallen, isCommand, reviver, reviveText);
            this.runCommands(fallen, reviver);
            return;
        }

        //Play the break sound
        fallen.level.playSound(null, fallen.blockPosition(), SoundEvents.ITEM_BREAK, SoundCategory.PLAYERS, MathUtil.randomFloat(0.7F,1.0F), MathUtil.randomFloat(0.8F,1.0F));

        if (this.refreshOptions){
            cap.cycleReviveOptions(null);
            cap.cycleReviveOptions(null);
            cap.refreshSelfReviveTypes();
            cap.refreshReviveItemList();
        }
        if (this.useReviveOnFail) cap.incrementSelfReviveCount();

        if (reviver != null && reviver != fallen) cap.resumeFallTimer();

        if (ReviveMeConfig.timeLeft != 0) {
            double timeLeft = cap.getTimeLeft(false);
            timeLeft = Math.max(timeLeft, 0);
            timeLeft += this.fallenTimerChange;
            cap.SetTimeLeft(fallen.level.getGameTime(), timeLeft, false);
        }

        //Fallen Player
        cap.setOtherPlayerAndItem(null, null);
        cap.syncClient(true);
        if (this.refreshOptions) NetworkHandler.sendToPlayer(fallen, new RefreshOptionsMsg());

        if (fallen != reviver && reviver != null) {
            FallenCapability reviveCap = FallenCapability.get(reviver);
            reviveCap.setOtherPlayerAndItem(null, null);
            reviveCap.syncClient(true);
        }
    }

    public void runCommands(PlayerEntity fallen, PlayerEntity reviver){
        GameRules.BooleanValue commandFeedback = fallen.getServer().getGameRules().getRule(GameRules.RULE_SENDCOMMANDFEEDBACK);
        boolean isAlreadySilenced = commandFeedback.get();

        if (ReviveMeConfig.silenceCommandMessages) commandFeedback.set(false, fallen.getServer());
        for (String s : this.getReviveCommands()){
            String properString = s.replace("@s", reviver.getName().getString())
                    .replace("@p", fallen.getName().getString());
            fallen.getServer().getCommands().performCommand(
                    fallen.createCommandSourceStack().withPermission(5), properString);
        }
        commandFeedback.set(isAlreadySilenced, fallen.getServer());
    }

    @Override
    public void takeFromReviver(PlayerEntity reviver,  PlayerEntity fallen) {
        super.takeFromReviver(reviver, fallen);
    }

    protected static Path getItemPath(){
        return FMLPaths.CONFIGDIR.get().resolve("reviveme/reviveme-items.json");
    }
    protected static Path getExamplePath(){
        return FMLPaths.CONFIGDIR.get().resolve("reviveme/reviveme-items-example.json");
    }

    public static void createFile(String resourceName, Path filePath, boolean justReplace){
        if (!justReplace && filePath.toFile().isFile()) return;

        try(InputStream stream = ReviveItemData.class.getClassLoader()
                .getResource("assets/"+ ReviveMe.MOD_ID + resourceName).openStream()){
            long bytes = Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.info("[Revive Me!] HOW MANY BYTES WERE WRITTEN?? " + bytes);
        }
        catch (Exception e){
            LOGGER.error("[Revive Me!] File Error: "+ e.getMessage());
        }
    }

    public static CompoundNBT getNBTFromFile(){
        try{
            if (!Files.isDirectory(getItemPath().getParent())){
                Files.createDirectories(getItemPath().getParent());
            }

            createFile("/reviveme-items.json", getItemPath(), false);
            createFile("/reviveme-items-example.json", getExamplePath(), true);

            InputStreamReader itemStream = new InputStreamReader(Files.newInputStream(getItemPath()), StandardCharsets.UTF_8);

            CompoundNBT reviveItemNBT = ((CompoundNBT) JsonOps.INSTANCE.convertTo(NBTDynamicOps.INSTANCE,
                    JSONUtils.parse(itemStream)));

            itemStream.close();

            return reviveItemNBT;
        }
        catch (Exception e){
            LOGGER.error("[Revive Me!] There was an issue grabbing revive items");
            e.printStackTrace();
        }
        return new CompoundNBT();
    }
    public static void deserializeNBT(CompoundNBT reviveItemNBT) {
        reviveItemMap.clear();
        try {

            Map<String, CompoundNBT> itemMap = new HashMap<>();
            reviveItemNBT.getAllKeys().forEach(key ->{
                if (key.equals("EXAMPLE")){
                    LOGGER.warn("[Revive Me!] This is only an example, don't make into a revive item");
                    return;
                }

                INBT inbt = reviveItemNBT.get(key);
                if (inbt == null){
                    LOGGER.error("[Revive Me!] This inbt is empty or malformed! " + key);
                    return;
                }
                if (inbt.getType() != CompoundNBT.TYPE){
                    LOGGER.error("[Revive Me!] This inbt is not the correct type! " + key);
                }
                itemMap.put(key, (CompoundNBT) inbt);
            });

            itemMap.forEach((key, value) -> fromEntry(key, value, new ArrayList<>(), itemMap));
        }
        catch (Exception e){
            LOGGER.error("[Revive Me!] There was an issue grabbing revive items");
            e.printStackTrace();
        }

    }
}
