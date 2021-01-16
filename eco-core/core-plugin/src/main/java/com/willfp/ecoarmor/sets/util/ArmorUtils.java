package com.willfp.ecoarmor.sets.util;

import com.willfp.eco.util.plugin.AbstractEcoPlugin;
import com.willfp.ecoarmor.effects.Effect;
import com.willfp.ecoarmor.sets.ArmorSet;
import com.willfp.ecoarmor.sets.ArmorSets;
import com.willfp.ecoarmor.sets.meta.ArmorSlot;
import lombok.experimental.UtilityClass;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@UtilityClass
public class ArmorUtils {
    /**
     * Instance of EcoArmor.
     */
    private static final AbstractEcoPlugin PLUGIN = AbstractEcoPlugin.getInstance();

    /**
     * Get armor set on an item.
     *
     * @param itemStack The itemStack to check.
     * @return The set, or null if no set is found.
     */
    @Nullable
    public ArmorSet getSetOnItem(@NotNull final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String setName = container.get(PLUGIN.getNamespacedKeyFactory().create("set"), PersistentDataType.STRING);

        if (setName == null) {
            return null;
        }

        return ArmorSets.getByName(setName);
    }

    /**
     * Get armor set that player is wearing.
     *
     * @param player The player to check.
     * @return The set, or null if no full set is worn.
     */
    @Nullable
    public ArmorSet getSetOnPlayer(@NotNull final Player player) {
        List<ArmorSet> found = new ArrayList<>();

        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack == null) {
                continue;
            }

            ArmorSet set = getSetOnItem(itemStack);

            if (set == null) {
                continue;
            }

            found.add(set);
        }

        if (found.size() < 4) {
            return null;
        }

        boolean allEqual = true;
        for (ArmorSet set : found) {
            if (!set.equals(found.get(0))) {
                allEqual = false;
                break;
            }
        }

        if (allEqual) {
            return found.get(0);
        }

        return null;
    }

    /**
     * Get the strength of an effect on a player's set.
     *
     * @param player The player to test.
     * @param effect The effect to test.
     * @return The strength, or 0 if not found.
     */
    public double getEffectStrength(@NotNull final Player player,
                                    @NotNull final Effect effect) {
        ArmorSet set = getSetOnPlayer(player);
        if (set == null) {
            return 0;
        }

        if (set.getEffects().containsKey(effect)) {
            return set.getEffects().get(effect);
        }

        return 0;
    }

    /**
     * If a player has an active effect.
     *
     * @param player The player to test.
     * @param effect The effect to test.
     * @return If a player has an active effect.
     */
    public boolean hasEffect(@NotNull final Player player,
                             @NotNull final Effect effect) {
        return getEffectStrength(player, effect) != 0;
    }

    /**
     * Get tier on upgrade crystal.
     *
     * @param itemStack The item to check.
     * @return The found tier, or null.
     */
    @Nullable
    public static String getCrystalTier(@NotNull final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return null;
        }

        if (meta.getPersistentDataContainer().has(PLUGIN.getNamespacedKeyFactory().create("upgrade_crystal"), PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(PLUGIN.getNamespacedKeyFactory().create("upgrade_crystal"), PersistentDataType.STRING);
        }

        return null;
    }

    /**
     * Get tier on item.
     *
     * @param itemStack The item to check.
     * @return The found tier.
     */
    public static String getTier(@NotNull final ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return "default";
        }

        if (meta.getPersistentDataContainer().has(PLUGIN.getNamespacedKeyFactory().create("tier"), PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(PLUGIN.getNamespacedKeyFactory().create("tier"), PersistentDataType.STRING);
        }

        return "default";
    }

    /**
     * Get tier on item.
     *
     * @param itemStack The item to check.
     * @param tier      The tier to set.
     */
    public static void setTier(@NotNull final ItemStack itemStack,
                               @NotNull final String tier) {
        ItemMeta meta = itemStack.getItemMeta();

        if (meta == null) {
            return;
        }

        if (getSetOnItem(itemStack) == null) {
            return;
        }

        meta.getPersistentDataContainer().set(PLUGIN.getNamespacedKeyFactory().create("tier"), PersistentDataType.STRING, tier);

        ArmorSlot slot = ArmorSlot.getSlot(itemStack);

        if (slot == null) {
            return;
        }

        int armor = 0;
        int toughness = 0;
        int knockback = 0;

        if (tier.equals("iron")) {
            if (slot == ArmorSlot.HELMET) {
                armor = 2;
            } else if (slot == ArmorSlot.CHESTPLATE) {
                armor = 6;
            } else if (slot == ArmorSlot.LEGGINGS) {
                armor = 5;
            } else if (slot == ArmorSlot.BOOTS) {
                armor = 2;
            }
        } else if (tier.equals("diamond")) {
            toughness = 2;
            if (slot == ArmorSlot.HELMET) {
                armor = 3;
            } else if (slot == ArmorSlot.CHESTPLATE) {
                armor = 8;
            } else if (slot == ArmorSlot.LEGGINGS) {
                armor = 6;
            } else if (slot == ArmorSlot.BOOTS) {
                armor = 3;
            }
        } else if (tier.equals("netherite")) {
            toughness = 3;
            knockback = 1;
            if (slot == ArmorSlot.HELMET) {
                armor = 3;
            } else if (slot == ArmorSlot.CHESTPLATE) {
                armor = 8;
            } else if (slot == ArmorSlot.LEGGINGS) {
                armor = 6;
            } else if (slot == ArmorSlot.BOOTS) {
                armor = 3;
            }
        }

        if (armor > 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "ecoarmor-armor", armor, AttributeModifier.Operation.ADD_NUMBER, slot.getSlot()));
        }
        if (toughness > 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS);
            meta.addAttributeModifier(Attribute.GENERIC_ARMOR_TOUGHNESS, new AttributeModifier(UUID.randomUUID(), "ecoarmor-toughness", toughness, AttributeModifier.Operation.ADD_NUMBER, slot.getSlot()));
        }
        if (knockback > 0) {
            meta.removeAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
            meta.addAttributeModifier(Attribute.GENERIC_KNOCKBACK_RESISTANCE, new AttributeModifier(UUID.randomUUID(), "ecoarmor-knockback", (double) knockback / 10, AttributeModifier.Operation.ADD_NUMBER, slot.getSlot()));
        }

        itemStack.setItemMeta(meta);
    }
}