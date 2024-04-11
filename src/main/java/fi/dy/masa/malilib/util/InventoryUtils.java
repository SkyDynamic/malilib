package fi.dy.masa.malilib.util;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import fi.dy.masa.malilib.MaLiLib;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.*;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InventoryUtils
{
    public static final Set<DataComponentType<Integer>> DAMAGE_KEY = Set.of(DataComponentTypes.DAMAGE);
    private static final DefaultedList<ItemStack> EMPTY_LIST = DefaultedList.of();
    public static final Pattern PATTERN_ITEM_BASE = Pattern.compile("^(?<name>(?:[a-z0-9\\._-]+:)[a-z0-9\\._-]+)$");

    /**
     * @return true if the stacks are identical, including their "Components"
     */
    public static boolean areStacksEqual(ItemStack stack1, ItemStack stack2)
    {
        return areStacksAndNbtEqual(stack1, stack2);
    }

    /**
     * @return true if the stacks are identical, including their "Components"
     */
    public static boolean areStacksAndNbtEqual(ItemStack stack1, ItemStack stack2)
    {
        return ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    /**
     * @return true if the stacks are identical, but ignoring the Components
     */
    public static boolean areStacksEqualIgnoreNbt(ItemStack stack1, ItemStack stack2)
    {
        return ItemStack.areItemsEqual(stack1, stack2);
    }

    /**
     * @return true if the stacks are identical, but ignoring the stack size,
     * and if the item is damageable, then ignoring the damage too.
     */
    public static boolean areStacksEqualIgnoreDurability(ItemStack stack1, ItemStack stack2)
    {
        if (!ItemStack.areItemsEqual(stack1, stack2))
        {
            return false;
        }

        ComponentMap tag1 = stack1.getComponents();
        ComponentMap tag2 = stack2.getComponents();

        if (tag1 == null || tag2 == null)
        {
            return tag1 == tag2;
        }

        if (!stack1.isDamageable() && !stack2.isDamageable())
        {
            return Objects.equals(tag1, tag2);
        }

        return areNbtEqualIgnoreKeys(tag1, tag2, DataComponentTypes.DAMAGE, DAMAGE_KEY);
    }

    /**
     * Uses new ComponentMap to compare values
     * @param tag1 (ComponentMap 1)
     * @param tag2 (ComponentMap 2)
     * @param type (DataComponentType) [OPTIONAL]
     * @param ignoredKeys (keys to ignore) [OPTIONAL]
     * @return (return value)
     * @param <T> DataComponentType extendable
     */
    public static <T> boolean areNbtEqualIgnoreKeys(@Nonnull ComponentMap tag1, @Nonnull ComponentMap tag2, @Nullable DataComponentType<T> type, @Nullable Set<DataComponentType<T>> ignoredKeys)
    {
        Set<DataComponentType<?>> keys1;
        Set<DataComponentType<?>> keys2;

        keys1 = tag1.getTypes();
        keys2 = tag2.getTypes();

        if (ignoredKeys != null)
        {
            keys1.removeAll(ignoredKeys);
            keys2.removeAll(ignoredKeys);
        }

        if (!Objects.equals(keys1, keys2))
        {
            return false;
        }

        if (type == null)
        {
            for (DataComponentType<?> key : keys1)
            {
                if (!Objects.equals(tag1.get(key), tag2.get(key)))
                {
                    return false;
                }
            }

            return true;
        }
        else
        {
            return Objects.equals(tag1.get(type), tag2.get(type));
        }
    }

    /**
     * Same as above, but still intended to compare NbtCompounds
     * @param tag1 (NbtCompound tag1)
     * @param tag2 (NbtCompound tag2)
     * @param ignoredKeys (Keys to ignore) [OPTIONAL]
     * @return (result)
     */
    public static boolean areNbtEqualIgnoreKeys(@Nonnull NbtCompound tag1, @Nonnull NbtCompound tag2, @Nullable Set<String> ignoredKeys)
    {
        Set<String> keys1;
        Set<String> keys2;

        keys1 = tag1.getKeys();
        keys2 = tag2.getKeys();

        if (ignoredKeys != null)
        {
            keys1.removeAll(ignoredKeys);
            keys2.removeAll(ignoredKeys);
        }

        if (!Objects.equals(keys1, keys2))
        {
            return false;
        }

        for (String key : keys1)
        {
            if (!Objects.equals(tag1.get(key), tag2.get(key)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Swaps the stack from the slot <b>slotNum</b> to the given hotbar slot <b>hotbarSlot</b>
     * @param container
     * @param slotNum
     * @param hotbarSlot
     */
    public static void swapSlots(ScreenHandler container, int slotNum, int hotbarSlot)
    {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.interactionManager != null;
        //MaLiLib.printDebug("swapSlots(): syncId {}, slotNum {}, hotbarSlot {}, swap", container.syncId, slotNum, hotbarSlot);
        mc.interactionManager.clickSlot(container.syncId, slotNum, hotbarSlot, SlotActionType.SWAP, mc.player);
    }

    /**
     * Assuming that the slot is from the ContainerPlayer container,
     * returns whether the given slot number is one of the regular inventory slots.
     * This means that the crafting slots and armor slots are not valid.
     * @param slotNumber
     * @param allowOffhand
     * @return
     */
    public static boolean isRegularInventorySlot(int slotNumber, boolean allowOffhand)
    {
        return slotNumber > 8 && (allowOffhand || slotNumber < 45);
    }

    /**
     * Finds an empty slot in the player inventory. Armor slots are not valid for the return value of this method.
     * Whether or not the offhand slot is valid, depends on the <b>allowOffhand</b> argument.
     * @param containerPlayer
     * @param allowOffhand
     * @param reverse
     * @return the slot number, or -1 if none were found
     */
    public static int findEmptySlotInPlayerInventory(ScreenHandler containerPlayer, boolean allowOffhand, boolean reverse)
    {
        final int startSlot = reverse ? containerPlayer.slots.size() - 1 : 0;
        final int endSlot = reverse ? -1 : containerPlayer.slots.size();
        final int increment = reverse ? -1 : 1;

        for (int slotNum = startSlot; slotNum != endSlot; slotNum += increment)
        {
            Slot slot = containerPlayer.slots.get(slotNum);
            ItemStack stackSlot = slot.getStack();

            // Inventory crafting, armor and offhand slots are not valid
            if (stackSlot.isEmpty() && isRegularInventorySlot(slot.id, allowOffhand))
            {
                return slot.id;
            }
        }

        return -1;
    }

    /**
     * Finds a slot with an identical item than <b>stackReference</b>, ignoring the durability
     * of damageable items. Does not allow crafting or armor slots or the offhand slot
     * in the ContainerPlayer container.
     * @param container
     * @param stackReference
     * @param reverse
     * @return the slot number, or -1 if none were found
     */
    public static int findSlotWithItem(ScreenHandler container, ItemStack stackReference, boolean reverse)
    {
        final int startSlot = reverse ? container.slots.size() - 1 : 0;
        final int endSlot = reverse ? -1 : container.slots.size();
        final int increment = reverse ? -1 : 1;
        final boolean isPlayerInv = container instanceof PlayerScreenHandler;

        for (int slotNum = startSlot; slotNum != endSlot; slotNum += increment)
        {
            Slot slot = container.slots.get(slotNum);

            if ((!isPlayerInv || isRegularInventorySlot(slot.id, false)) &&
                areStacksEqualIgnoreDurability(slot.getStack(), stackReference))
            {
                return slot.id;
            }
        }

        return -1;
    }

    /**
     * Swap the given item to the player's main hand, if that item is found
     * in the player's inventory.
     * @param stackReference
     * @param mc
     * @return true if an item was swapped to the main hand, false if it was already in the hand, or was not found in the inventory
     */
    public static boolean swapItemToMainHand(ItemStack stackReference, MinecraftClient mc)
    {
        PlayerEntity player = mc.player;
        assert player != null;
        boolean isCreative = player.isCreative();

        // Already holding the requested item
        if (areStacksEqualIgnoreNbt(stackReference, player.getMainHandStack()))
        {
            return false;
        }

        if (isCreative)
        {
            player.getInventory().addPickBlock(stackReference);
            assert mc.interactionManager != null;
            mc.interactionManager.clickCreativeStack(player.getMainHandStack(), 36 + player.getInventory().selectedSlot); // sendSlotPacket
            return true;
        }
        else
        {
            int slot = findSlotWithItem(player.playerScreenHandler, stackReference, true);

            if (slot != -1)
            {
                int currentHotbarSlot = player.getInventory().selectedSlot;
                assert mc.interactionManager != null;
                //MaLiLib.printDebug("swapItemToMainHand(): syncId {}, slot {}, currentHotbarSLot {}, swap", player.playerScreenHandler.syncId, slot, currentHotbarSlot);
                mc.interactionManager.clickSlot(player.playerScreenHandler.syncId, slot, currentHotbarSlot, SlotActionType.SWAP, mc.player);
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the inventory at the given position, if any.
     * Combines chest inventories into double chest inventories when applicable.
     * @param world
     * @param pos
     * @return
     */
    @Nullable
    public static Inventory getInventory(World world, BlockPos pos)
    {
        @SuppressWarnings("deprecation")
        boolean isLoaded = world.isChunkLoaded(pos);

        if (!isLoaded)
        {
            return null;
        }

        // The method in World now checks that the caller is from the same thread...
        BlockEntity te = world.getWorldChunk(pos).getBlockEntity(pos);

        if (te instanceof Inventory inv)
        {
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof ChestBlock && te instanceof ChestBlockEntity)
            {
                ChestType type = state.get(ChestBlock.CHEST_TYPE);

                if (type != ChestType.SINGLE)
                {
                    BlockPos posAdj = pos.offset(ChestBlock.getFacing(state));
                    @SuppressWarnings("deprecation")
                    boolean isLoadedAdj = world.isChunkLoaded(posAdj);

                    if (isLoadedAdj)
                    {
                        BlockState stateAdj = world.getBlockState(posAdj);
                        // The method in World now checks that the caller is from the same thread...
                        BlockEntity te2 = world.getWorldChunk(posAdj).getBlockEntity(posAdj);

                        if (stateAdj.getBlock() == state.getBlock() &&
                            te2 instanceof ChestBlockEntity &&
                            stateAdj.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE &&
                            stateAdj.get(ChestBlock.FACING) == state.get(ChestBlock.FACING))
                        {
                            Inventory invRight = type == ChestType.RIGHT ?             inv : (Inventory) te2;
                            Inventory invLeft  = type == ChestType.RIGHT ? (Inventory) te2 :             inv;
                            inv = new DoubleInventory(invRight, invLeft);
                        }
                    }
                }
            }

            return inv;
        }

        return null;
    }

    /**
     * Checks if the given Shulker Box (or other storage item with the
     * same NBT data structure) currently contains any items.
     * @param stackShulkerBox
     * @return
     */
    public static boolean shulkerBoxHasItems(ItemStack stackShulkerBox)
    {
        ComponentMap data = stackShulkerBox.getComponents();

        if (data != null && data.contains(DataComponentTypes.CONTAINER))
        {
            ContainerComponent itemContainer = data.get(DataComponentTypes.CONTAINER);

            if (itemContainer != null)
                return itemContainer.stream().findAny().isPresent();
            else
                return false;
        }

        return false;
    }

    /**
     * Returns the list of items currently stored in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * Does not keep empty slots.
     * @param stackIn The item holding the inventory contents
     * @return
     */
    public static DefaultedList<ItemStack> getStoredItems(ItemStack stackIn)
    {
        ComponentMap data = stackIn.getComponents();

        if (data != null && data.contains(DataComponentTypes.CONTAINER))
        {
            ContainerComponent itemContainer = data.get(DataComponentTypes.CONTAINER);

            if (itemContainer != null)
            {
                DefaultedList<ItemStack> items = EMPTY_LIST;

                itemContainer.copyTo(items);

                return items;
            }
            else
                return EMPTY_LIST;
        }
        else
            return EMPTY_LIST;
    }

    /**
     * Returns the list of items currently stored in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * Preserves empty slots.
     * @param stackIn The item holding the inventory contents
     * @param slotCount the maximum number of slots, and thus also the size of the list to create
     * @return
     */
    public static DefaultedList<ItemStack> getStoredItems(ItemStack stackIn, int slotCount)
    {
        ComponentMap data = stackIn.getComponents();

        if (data != null && data.contains(DataComponentTypes.CONTAINER))
        {
            ContainerComponent itemContainer = data.get(DataComponentTypes.CONTAINER);

            if (itemContainer != null)
            {
                DefaultedList<ItemStack> items = EMPTY_LIST;

                //final long count = itemContainer.method_57489().count();
                //int count = 0;
                //int maxSlot = -1;

                Iterator<ItemStack> iter = itemContainer.iterator();

                if (slotCount <= 0)
                {
                    Item itemIn = stackIn.getItem();
                    if (itemIn instanceof BlockItem && ((BlockItem) itemIn).getBlock() instanceof ShulkerBoxBlock)
                        slotCount = ShulkerBoxBlockEntity.INVENTORY_SIZE;
                    else
                        slotCount = 27;

                    for (int i = 0; i < slotCount; i++)
                    {
                        if (iter.hasNext())
                        {
                            items.add(iter.next());
                        }
                        else
                        {
                            items.add(ItemStack.EMPTY);
                        }
                    }

                    return items;
                }
                // FIXME Slot handling seems to have been removed,
                //  so we'll use it to define the "size" ...
                else if (slotCount < 54)
                {
                    for (int i = 0; i < slotCount; i++)
                    {
                        if (iter.hasNext())
                        {
                            items.add(iter.next());
                        }
                        else
                        {
                            items.add(ItemStack.EMPTY);
                        }
                    }

                    return items;
                }
                else
                    return EMPTY_LIST;
            }
            else
                return EMPTY_LIST;
        }
        else
            return EMPTY_LIST;
    }

        /*
        NbtCompound nbt = stackIn.getNbt();

        if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            NbtCompound tagBlockEntity = nbt.getCompound("BlockEntityTag");

            if (tagBlockEntity.contains("Items", Constants.NBT.TAG_LIST))
            {
                NbtList tagList = tagBlockEntity.getList("Items", Constants.NBT.TAG_COMPOUND);
                final int count = tagList.size();
                int maxSlot = -1;

                if (slotCount <= 0)
                {
                    for (int i = 0; i < count; ++i)
                    {
                        NbtCompound tag = tagList.getCompound(i);
                        int slot = tag.getByte("Slot");

                        if (slot > maxSlot)
                        {
                            maxSlot = slot;
                        }
                    }

                    slotCount = maxSlot + 1;
                }

                DefaultedList<ItemStack> items = DefaultedList.ofSize(slotCount, ItemStack.EMPTY);

                for (int i = 0; i < count; ++i)
                {
                    NbtCompound tag = tagList.getCompound(i);
                    ItemStack stack = ItemStack.fromNbt(tag);
                    int slot = tag.getByte("Slot");

                    if (slot >= 0 && slot < items.size() && !stack.isEmpty())
                    {
                        items.set(slot, stack);
                    }
                }

                return items;
            }
        }

        return EMPTY_LIST;

         */

    public static boolean bundleHasItems(ItemStack stack)
    {
        ComponentMap data = stack.getComponents();

        if (data != null && data.contains(DataComponentTypes.BUNDLE_CONTENTS))
        {
            BundleContentsComponent bundleContainer = data.get(DataComponentTypes.BUNDLE_CONTENTS);

            if (bundleContainer != null)
                return bundleContainer.stream().findAny().isPresent();
            else
                return false;
        }

        return false;
    }

    public static int bundleCountItems(ItemStack stack)
    {
        ComponentMap data = stack.getComponents();

        if (data != null && data.contains(DataComponentTypes.BUNDLE_CONTENTS))
        {
            BundleContentsComponent bundleContainer = data.get(DataComponentTypes.BUNDLE_CONTENTS);

            if (bundleContainer != null)
                return bundleContainer.getOccupancy();
            else
                return -1;
        }

        return -1;
    }

    public static DefaultedList<ItemStack> getBundleItems(ItemStack stackIn)
    {
        ComponentMap data = stackIn.getComponents();

        if (data != null && data.contains(DataComponentTypes.BUNDLE_CONTENTS))
        {
            BundleContentsComponent bundleContainer = data.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);

            if (bundleContainer != null)
            {
                int maxSlots = bundleContainer.size();
                DefaultedList<ItemStack> items = EMPTY_LIST;

                for (int i = 0; i < maxSlots; i++)
                {

                    ItemStack slot = bundleContainer.get(i);

                    if (!slot.isEmpty())
                        items.add(slot);
                }

                return items;
            }
            return EMPTY_LIST;
        }
        return EMPTY_LIST;
    }

    /**
     * Returns a map of the stored item counts in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * @param stackShulkerBox
     * @return
     */
    public static Object2IntOpenHashMap<ItemType> getStoredItemCounts(ItemStack stackShulkerBox)
    {
        Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
        DefaultedList<ItemStack> items = getStoredItems(stackShulkerBox);

        for (ItemStack stack : items)
        {
            if (!stack.isEmpty())
            {
                map.addTo(new ItemType(stack), stack.getCount());
            }
        }

        return map;
    }

    /**
     * Returns a map of the stored item counts in the given inventory.
     * This also counts the contents of any Shulker Boxes
     * (or other storage item with the same NBT data structure).
     * @param inv
     * @return
     */
    public static Object2IntOpenHashMap<ItemType> getInventoryItemCounts(Inventory inv)
    {
        Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
        final int slots = inv.size();

        for (int slot = 0; slot < slots; ++slot)
        {
            ItemStack stack = inv.getStack(slot);

            if (!stack.isEmpty())
            {
                map.addTo(new ItemType(stack, false, true), stack.getCount());

                if (stack.getItem() instanceof BlockItem &&
                    ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock &&
                    shulkerBoxHasItems(stack))
                {
                    Object2IntOpenHashMap<ItemType> boxCounts = getStoredItemCounts(stack);

                    for (ItemType type : boxCounts.keySet())
                    {
                        map.addTo(type, boxCounts.getInt(type));
                    }
                }
            }
        }

        return map;
    }

    /**
     * Returns the given list of items wrapped as an InventoryBasic
     * @param items
     * @return
     */
    public static Inventory getAsInventory(DefaultedList<ItemStack> items)
    {
        SimpleInventory inv = new SimpleInventory(items.size());

        for (int slot = 0; slot < items.size(); ++slot)
        {
            inv.setStack(slot, items.get(slot));
        }

        return inv;
    }

    /**
     * Creates an ItemStack via a String
     * @param itemNameIn (String containing the item name)
     * @return (The ItemStack object or ItemStack.EMPTY, aka Air)
     */
    public static ItemStack getItemStackFromString(String itemNameIn)
    {
        return getItemStackFromString(itemNameIn, -1, ComponentMap.EMPTY);
    }

    /**
     * Creates an ItemStack via a String
     * @param itemNameIn (String containing the item name)
     * @param data (ComponentMap data to import)
     * @return (The ItemStack object or ItemStack.EMPTY, aka Air)
     */
    public static ItemStack getItemStackFromString(String itemNameIn, ComponentMap data)
    {
        return getItemStackFromString(itemNameIn, -1, data);
    }

    /**
     * Creates an ItemStack via a String
     * @param itemNameIn (String containing the item name)
     * @param count (How many in this stack)
     * @return (The ItemStack object or ItemStack.EMPTY, aka Air)
     */
    public static ItemStack getItemStackFromString(String itemNameIn, int count)
    {
        return getItemStackFromString(itemNameIn, count, ComponentMap.EMPTY);
    }

    /**
     * Creates an ItemStack via a String
     * @param itemNameIn (String containing the item name)
     * @param count (How many in this stack)
     * @param data (ComponentMap data to import)
     * @return (The ItemStack object or ItemStack.EMPTY, aka Air)
     */
    public static ItemStack getItemStackFromString(String itemNameIn, int count, ComponentMap data)
    {
        if (itemNameIn.isEmpty() || itemNameIn.equals("empty") || itemNameIn.equals("minecraft:air"))
        {
            return ItemStack.EMPTY;
        }
        else
        {
            Matcher matcherBase = PATTERN_ITEM_BASE.matcher(itemNameIn);
            String itemName;

            if (matcherBase.matches())
            {
                itemName = matcherBase.group("name");

                if (itemName != null)
                {
                    Identifier itemId = new Identifier(itemName);
                    Item item = Registries.ITEM.get(itemId);
                    RegistryEntry<Item> itemEntry = RegistryEntry.of(item);

                    MaLiLib.printDebug("InventoryUtils#getItemStackFromString(): id {}, item {}, entry {}", itemId.toString(), item.toString(), itemEntry.toString());

                    if (item != Items.AIR && itemEntry.hasKeyAndValue())
                    {
                        if (count < 0 && data.isEmpty())
                        {
                            return new ItemStack(itemEntry);
                        }
                        else if (data.isEmpty())
                        {
                            return new ItemStack(itemEntry, count);
                        }
                        else if (count < 0)
                        {
                            ItemStack result = new ItemStack(itemEntry);
                            result.applyComponentsFrom(data);

                            return result;
                        }
                        else
                        {
                            ItemStack result = new ItemStack(itemEntry, count);
                            result.applyComponentsFrom(data);

                            return result;
                        }
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
