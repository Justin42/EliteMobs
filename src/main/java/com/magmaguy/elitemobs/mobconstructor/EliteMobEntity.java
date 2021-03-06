package com.magmaguy.elitemobs.mobconstructor;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.items.MobTierFinder;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.mobpowers.ElitePower;
import com.magmaguy.elitemobs.mobpowers.majorpowers.MajorPower;
import com.magmaguy.elitemobs.mobpowers.minorpowers.MinorPower;
import com.magmaguy.elitemobs.mobspawning.NaturalMobSpawnEventHandler;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import com.magmaguy.elitemobs.powerstances.MinorPowerPowerStance;
import com.magmaguy.elitemobs.utils.VersionChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

public class EliteMobEntity {

    /*
    Note that a lot of values here are defined by EliteMobProperties.java
     */
    private LivingEntity eliteMob;
    private int eliteMobLevel;
    private double eliteMobTier;
    private double maxHealth;
    private String name;
    /*
    Store all powers in one set, makes no sense to access it in individual sets.
    The reason they are split up in the first place is to add them in a certain ratio
    Once added, they can just be stored in a pool
     */
    private HashSet<ElitePower> powers = new HashSet<>();
    private int minorPowerCount = 0;
    private int majorPowerCount = 0;
    private boolean hasMinorVisualEffect = false;
    private boolean hasMajorVisualEffect = false;
    private boolean hasVisualEffectObfuscated = true;
    private boolean isNaturalEntity;
    /*
    This just defines default behavior
     */
    private boolean hasStacking = true;
    private boolean hasCustomArmor = false;
    private boolean hasCustomName = false;
    private boolean hasCustomPowers = false;
    private boolean hasFarAwayUnload = true;
    private boolean hasCustomHealth = false;
    private boolean hasNormalLoot = true;

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     *
     * @param livingEntity  Minecraft entity associated to this elite mob
     * @param eliteMobLevel Level of the mob, can be modified during runtime. Dynamically assigned.
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(livingEntity);
        /*
        Set the power list
         */
        randomizePowers(eliteMobProperties);
        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(this);

        eliteMob.setCanPickupItems(false);

    }

    /**
     * This is the generic constructor used in most instances of natural elite mob generation
     *
     * @param livingEntity  Minecraft entity associated to this elite mob
     * @param eliteMobLevel Level of the mob, can be modified during runtime. Dynamically assigned.
     */
    public EliteMobEntity(LivingEntity livingEntity, int eliteMobLevel, double currentHealthPercent) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = livingEntity;
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(livingEntity);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        eliteMob.setHealth(maxHealth * currentHealthPercent);
        /*
        Set the armor
         */
        setArmor();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(livingEntity);
        /*
        Set the power list
         */
        randomizePowers(eliteMobProperties);
        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(this);

        eliteMob.setCanPickupItems(false);

    }


    /**
     * Spawning method for boss mobs.
     * Assumes custom powers and custom names.
     *
     * @param entityType    type of mob that this entity is slated to become
     * @param location      location at which the elite mob will spawn
     * @param eliteMobLevel boss mob level, should be automatically generated based on the highest player tier online
     * @param name          the name for this boss mob, overrides the usual elite mob name format
     * @see BossMobEntity
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteMobLevel, String name) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entityType);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(name);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Register whether or not the elite mob is natural
         */
        this.isNaturalEntity = EntityTracker.isNaturalEntity(this.eliteMob);
        /*
        These have custom powers
         */
        this.hasCustomPowers = true;
        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(this);

        eliteMob.setCanPickupItems(false);

        this.setHasStacking(false);
        this.setHasCustomArmor(true);

    }

    /*
    This is the generic constructor for elite mobs spawned via commands
     */
    public EliteMobEntity(EntityType entityType, Location location, int eliteMobLevel, HashSet<ElitePower> mobPowers) {

        /*
        Register living entity to keep track of which entity this object is tied to
         */
        this.eliteMob = spawnBossMobLivingEntity(entityType, location);
        /*
        Register level, this is variable as per stacking rules
         */
        setEliteMobLevel(eliteMobLevel);
        eliteMobTier = MobTierFinder.findMobTier(eliteMobLevel);
        /*
        Get correct instance of plugin data, necessary for settings names and health among other things
         */
        EliteMobProperties eliteMobProperties = EliteMobProperties.getPluginData(entityType);
        /*
        Handle name, variable as per stacking rules
         */
        setCustomName(eliteMobProperties);
        /*
        Handle health, max is variable as per stacking rules
        Currently #setHealth() resets the health back to maximum
         */
        setMaxHealth(eliteMobProperties);
        setHealth();
        /*
        Set the armor
         */
        setArmor();
        /*
        Register whether or not the elite mob is natural
        All mobs spawned via commands are considered natural
         */
        this.isNaturalEntity = true;
        /*
        Set the power list
         */
        if (!mobPowers.isEmpty()) {
            this.powers = mobPowers;
            for (ElitePower elitePower : powers) {
                elitePower.applyPowers(eliteMob);
                if (elitePower instanceof MajorPower)
                    this.majorPowerCount++;
                if (elitePower instanceof MinorPower)
                    this.minorPowerCount++;
            }
            MinorPowerPowerStance minorPowerPowerStance = new MinorPowerPowerStance(this);
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);
        } else {
            randomizePowers(eliteMobProperties);
        }

        /*
        Start tracking the entity
         */
        EntityTracker.registerEliteMob(this);

        eliteMob.setCanPickupItems(false);

    }

    /*
    This avoids accidentally assigning an elite mob to an entity spawned specifically to be a boss mob or reinforcement
    */
    private static LivingEntity spawnBossMobLivingEntity(EntityType entityType, Location location) {
        NaturalMobSpawnEventHandler.setIgnoreMob(true);
        return (LivingEntity) location.getWorld().spawnEntity(location, entityType);
    }

    private void setCustomName(EliteMobProperties eliteMobProperties) {
        this.name = ChatColorConverter.convert(
                eliteMobProperties.getName().replace(
                        "$level", eliteMobLevel + ""));
        eliteMob.setCustomName(this.name);
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            eliteMob.setCustomNameVisible(true);
    }

    private void setCustomName(String name) {
        this.name = ChatColorConverter.convert(name);
        this.getLivingEntity().setCustomName(this.name);
        this.hasCustomName = true;
        if (ConfigValues.defaultConfig.getBoolean(DefaultConfig.ALWAYS_SHOW_NAMETAGS))
            eliteMob.setCustomNameVisible(true);
    }

    private void setEliteMobLevel(int newLevel) {
        if (newLevel < 1)
            newLevel = 1;
        this.eliteMobLevel = newLevel;
    }

    private void setMaxHealth(EliteMobProperties eliteMobProperties) {
        double defaultMaxHealth = eliteMobProperties.getDefaultMaxHealth();
        this.maxHealth = (eliteMobLevel * CombatSystem.PER_LEVEL_POWER_INCREASE * defaultMaxHealth + defaultMaxHealth);
        eliteMob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
    }

    private void setHealth() {
        eliteMob.setHealth(maxHealth = (maxHealth > 2000) ? 2000 : maxHealth);
    }

    private void setHealth(double healthPercentage) {
        eliteMob.setHealth(this.maxHealth * healthPercentage);
    }


    private void setArmor() {

        if (VersionChecker.currentVersionIsUnder(12, 2)) return;
        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_ARMOR)) return;

        eliteMob.getEquipment().setItemInMainHandDropChance(0);
        eliteMob.getEquipment().setHelmetDropChance(0);
        eliteMob.getEquipment().setChestplateDropChance(0);
        eliteMob.getEquipment().setLeggingsDropChance(0);
        eliteMob.getEquipment().setBootsDropChance(0);

        if (hasCustomArmor) return;

        if (!(eliteMob instanceof Zombie || eliteMob instanceof PigZombie ||
                eliteMob instanceof Skeleton || eliteMob instanceof WitherSkeleton)) return;

        eliteMob.getEquipment().setBoots(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setLeggings(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setChestplate(new ItemStack(Material.AIR));
        eliteMob.getEquipment().setHelmet(new ItemStack(Material.AIR));

        if (eliteMobLevel >= 12)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.LEATHER_HELMET));

        if (eliteMobLevel >= 14)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.LEATHER_BOOTS));

        if (eliteMobLevel >= 16)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));

        if (eliteMobLevel >= 18)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));

        if (eliteMobLevel >= 20)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.CHAINMAIL_HELMET));

        if (eliteMobLevel >= 22)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));

        if (eliteMobLevel >= 24)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));

        if (eliteMobLevel >= 26)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.CHAINMAIL_CHESTPLATE));

        if (eliteMobLevel >= 28)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));

        if (eliteMobLevel >= 30)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));

        if (eliteMobLevel >= 32)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));

        if (eliteMobLevel >= 34)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));

        if (eliteMobLevel >= 36)
            eliteMob.getEquipment().setBoots(new ItemStack(Material.DIAMOND_BOOTS));

        if (eliteMobLevel >= 38)
            if (ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ELITE_HELMETS))
                eliteMob.getEquipment().setHelmet(new ItemStack(Material.DIAMOND_HELMET));

        if (eliteMobLevel >= 40)
            eliteMob.getEquipment().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));

        if (eliteMobLevel >= 42)
            eliteMob.getEquipment().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));

    }

    private void randomizePowers(EliteMobProperties eliteMobProperties) {

        if (hasCustomPowers) return;
        if (eliteMobTier < 1) return;

        int availableDefensivePowers = 0;
        int availableOffensivePowers = 0;
        int availableMiscellaneousPowers = 0;
        int availableMajorPowers = 0;

        if (eliteMobTier >= 1) availableDefensivePowers = 1;
        if (eliteMobTier >= 2) availableOffensivePowers = 1;
        if (eliteMobTier >= 3) availableMiscellaneousPowers = 1;
        if (eliteMobTier >= 4) availableMajorPowers = 1;
        if (eliteMobTier >= 5) availableDefensivePowers = 2;
        if (eliteMobTier >= 6) availableOffensivePowers = 2;
        if (eliteMobTier >= 7) availableMiscellaneousPowers = 2;
        if (eliteMobTier >= 8) availableMajorPowers = 2;

        //apply defensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidDefensivePowers().clone(), availableDefensivePowers);

        //apply offensive powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidOffensivePowers().clone(), availableOffensivePowers);

        //apply miscellaneous powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMiscellaneousPowers().clone(), availableMiscellaneousPowers);

        //apply major powers
        applyPowers((HashSet<ElitePower>) eliteMobProperties.getValidMajorPowers().clone(), availableMajorPowers);

        MinorPowerPowerStance minorPowerStanceMath = new MinorPowerPowerStance(this);
        MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);

    }

    private void applyPowers(HashSet<ElitePower> elitePowers, int availablePowerAmount) {

        if (availablePowerAmount < 1) return;

        ArrayList<ElitePower> localPowers = new ArrayList<>(elitePowers);

        for (ElitePower mobPower : this.powers)
            localPowers.remove(mobPower);

        for (int i = 0; i < availablePowerAmount; i++)
            if (localPowers.size() < 1)
                break;
            else {
                ElitePower selectedPower = localPowers.get(ThreadLocalRandom.current().nextInt(localPowers.size()));
                this.powers.add(selectedPower);
                selectedPower.applyPowers(this.eliteMob);
                localPowers.remove(selectedPower);
                if (selectedPower instanceof MajorPower)
                    this.majorPowerCount++;
                if (selectedPower instanceof MinorPower)
                    this.minorPowerCount++;
            }

    }

    public void setCustomPowers(HashSet<ElitePower> elitePowers) {

        this.powers = elitePowers;
        for (ElitePower elitePower : elitePowers) {
            elitePower.applyPowers(this.eliteMob);
            if (elitePower instanceof MinorPower)
                this.minorPowerCount++;
            if (elitePower instanceof MajorPower)
                this.majorPowerCount++;
        }

        if (this.minorPowerCount > 0) {
            MinorPowerPowerStance minorPowerStanceMath = new MinorPowerPowerStance(this);
        }

        if (this.majorPowerCount > 0) {
            MajorPowerPowerStance majorPowerPowerStance = new MajorPowerPowerStance(this);
        }

    }

    public LivingEntity getLivingEntity() {
        return eliteMob;
    }

    public int getLevel() {
        return eliteMobLevel;
    }

    public boolean hasPower(ElitePower mobPower) {
        for (ElitePower elitePower : powers)
            if (elitePower.getClass().getTypeName().equals(mobPower.getClass().getTypeName()))
                return true;
        return false;
    }

    public int getMinorPowerCount() {
        return this.minorPowerCount;
    }

    public int getMajorPowerCount() {
        return this.majorPowerCount;
    }

    public HashSet<ElitePower> getPowers() {
        return powers;
    }

    public double getMaxHealth() {
        return maxHealth;
    }

    public void setHasStacking(boolean bool) {
        this.hasStacking = bool;
    }

    public boolean getHasCustomArmor() {
        return this.hasCustomArmor;
    }

    public void setHasCustomArmor(boolean bool) {
        this.hasCustomArmor = bool;
    }

    public boolean getHasCustomPowers() {
        return this.hasCustomPowers;
    }

    public void setHasCustomPowers(boolean bool) {
        this.hasCustomPowers = bool;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasMinorVisualEffect() {
        return this.hasMinorVisualEffect;
    }

    public void setHasMinorVisualEffect(boolean bool) {
        this.hasMinorVisualEffect = bool;
    }

    public boolean hasMajorVisualEffect() {
        return this.hasMajorVisualEffect;
    }

    public void setHasMajorVisualEffect(boolean bool) {
        this.hasMajorVisualEffect = bool;
    }

    public boolean isNaturalEntity() {
        return this.isNaturalEntity;
    }

    public void setIsNaturalEntity(Boolean bool) {
        this.isNaturalEntity = bool;
        this.hasNormalLoot = bool;
    }

    public boolean canStack() {
        return this.hasStacking;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getHasFarAwayUnload() {
        return this.hasFarAwayUnload;
    }

    public void setHasFarAwayUnload(boolean bool) {
        this.hasFarAwayUnload = bool;
    }

    public boolean getHasSpecialLoot() {
        return this.hasNormalLoot;
    }

    public void setHasSpecialLoot(boolean bool) {
        this.isNaturalEntity = bool;
        this.hasNormalLoot = bool;
    }

    public boolean getHasCustomHealth() {
        return this.hasCustomHealth;
    }

    public void setHasCustomHealth(boolean bool) {
        this.hasCustomHealth = bool;
    }

    public boolean getHasCustomName() {
        return this.hasCustomName;
    }

    public void setHasCustomName(boolean bool) {
        this.hasCustomName = bool;
    }

    public void setHasVisualEffectObfuscated(boolean bool) {
        this.hasVisualEffectObfuscated = bool;
    }

    public boolean getHasVisualEffectObfuscated() {
        return this.hasVisualEffectObfuscated;
    }

}
