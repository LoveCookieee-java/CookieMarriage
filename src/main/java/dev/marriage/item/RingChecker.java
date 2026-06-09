package dev.marriage.item;

import dev.marriage.MarriagePlugin;
import dev.marriage.item.RingItem.RingProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Kiểm tra và tiêu thụ nhẫn cưới trong inventory của người chơi.
 * Hỗ trợ: Vanilla, MMOItems, ItemEdit, ItemsAdder (soft-depend qua reflection).
 * Không cần compile-time dependency với các plugin bên ngoài.
 */
public final class RingChecker {

    private final MarriagePlugin plugin;

    // Soft-depend flags
    private final boolean mmoItemsEnabled;
    private final boolean itemEditEnabled;
    private final boolean itemsAdderEnabled;

    public RingChecker(MarriagePlugin plugin) {
        this.plugin = plugin;
        var pm = plugin.getServer().getPluginManager();
        mmoItemsEnabled   = pm.isPluginEnabled("MMOItems");
        itemEditEnabled   = pm.isPluginEnabled("ItemEdit");
        itemsAdderEnabled = pm.isPluginEnabled("ItemsAdder");

        if (mmoItemsEnabled)   plugin.getLogger().info("[RingChecker] Hooked vào MMOItems.");
        if (itemEditEnabled)   plugin.getLogger().info("[RingChecker] Hooked vào ItemEdit.");
        if (itemsAdderEnabled) plugin.getLogger().info("[RingChecker] Hooked vào ItemsAdder.");
    }

    /**
     * Kiểm tra player có nhẫn cưới hợp lệ trong inventory không.
     *
     * @param player Người chơi cần kiểm tra
     * @return true nếu tìm thấy ít nhất 1 nhẫn hợp lệ
     */
    public boolean hasRing(Player player) {
        List<RingItem> validRings = plugin.getRingConfig().getRingItems();
        for (ItemStack stack : player.getInventory().getContents()) {
            if (isEmpty(stack)) continue;
            for (RingItem ring : validRings) {
                if (matches(stack, ring)) return true;
            }
        }
        return false;
    }

    /**
     * Tiêu thụ (xóa) 1 nhẫn cưới hợp lệ khỏi inventory của player.
     * Phải gọi {@link #hasRing(Player)} trước để đảm bảo nhẫn tồn tại.
     *
     * @param player Người chơi cần lấy nhẫn
     * @return true nếu tìm thấy và xóa thành công
     */
    public boolean consumeRing(Player player) {
        List<RingItem> validRings = plugin.getRingConfig().getRingItems();
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (isEmpty(stack)) continue;
            for (RingItem ring : validRings) {
                if (matches(stack, ring)) {
                    if (stack.getAmount() > 1) {
                        stack.setAmount(stack.getAmount() - 1);
                    } else {
                        inv.setItem(i, null);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isEmpty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    private boolean matches(ItemStack stack, RingItem ring) {
        return switch (ring.provider()) {
            case VANILLA    -> matchVanilla(stack, ring);
            case MMOITEMS   -> mmoItemsEnabled   && matchMMOItems(stack, ring);
            case ITEMEDIT   -> itemEditEnabled    && matchItemEdit(stack, ring);
            case ITEMSADDER -> itemsAdderEnabled  && matchItemsAdder(stack, ring);
        };
    }

    // ── Vanilla ───────────────────────────────────────────────────────────────

    private boolean matchVanilla(ItemStack stack, RingItem ring) {
        try {
            Material mat = Material.valueOf(ring.id().toUpperCase());
            return stack.getType() == mat;
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[RingChecker] Material không hợp lệ: " + ring.id());
            return false;
        }
    }

    // ── MMOItems (reflection) ─────────────────────────────────────────────────
    // Đọc NBT tag MMOITEMS_ITEM_TYPE và MMOITEMS_ITEM_ID từ ItemMeta display name
    // thông qua MMOItems NBTItem API qua reflection.

    private boolean matchMMOItems(ItemStack stack, RingItem ring) {
        try {
            // NBTItem.get(stack)
            Class<?> nbtItemClass = Class.forName("net.Indyuce.mmoitems.api.item.NBTItem");
            Method getMethod = nbtItemClass.getMethod("get", ItemStack.class);
            Object nbtItem = getMethod.invoke(null, stack);
            if (nbtItem == null) return false;

            // nbtItem.hasType()
            Method hasType = nbtItemClass.getMethod("hasType");
            if (!(Boolean) hasType.invoke(nbtItem)) return false;

            // nbtItem.getType().getId()
            Method getType = nbtItemClass.getMethod("getType");
            Object typeObj = getType.invoke(nbtItem);
            Method getId = typeObj.getClass().getMethod("getId");
            String itemType = (String) getId.invoke(typeObj);

            // nbtItem.getString("MMOITEMS_ITEM_ID")
            Method getString = nbtItemClass.getMethod("getString", String.class);
            String itemId = (String) getString.invoke(nbtItem, "MMOITEMS_ITEM_ID");

            return ring.type().equalsIgnoreCase(itemType)
                    && ring.id().equalsIgnoreCase(itemId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "[RingChecker] MMOItems reflection error", e);
            return false;
        }
    }

    // ── ItemEdit (reflection) ─────────────────────────────────────────────────

    private boolean matchItemEdit(ItemStack stack, RingItem ring) {
        try {
            // Lấy plugin instance
            Object itemEditPlugin = plugin.getServer().getPluginManager().getPlugin("ItemEdit");
            if (itemEditPlugin == null) return false;

            // itemEditPlugin.getItemManager()
            Method getManager = itemEditPlugin.getClass().getMethod("getItemManager");
            Object manager = getManager.invoke(itemEditPlugin);

            // manager.getItemId(stack) → Optional<String>
            Method getItemId = manager.getClass().getMethod("getItemId", ItemStack.class);
            Object optResult = getItemId.invoke(manager, stack);

            if (!(optResult instanceof Optional<?> opt)) return false;
            if (opt.isEmpty()) return false;

            String customId = opt.get().toString();
            return ring.id().equalsIgnoreCase(customId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "[RingChecker] ItemEdit reflection error", e);
            return false;
        }
    }

    // ── ItemsAdder (reflection) ───────────────────────────────────────────────

    private boolean matchItemsAdder(ItemStack stack, RingItem ring) {
        try {
            // CustomStack.byItemStack(stack)
            Class<?> csClass = Class.forName("dev.lone.itemsadder.api.CustomStack");
            Method byStack = csClass.getMethod("byItemStack", ItemStack.class);
            Object cs = byStack.invoke(null, stack);
            if (cs == null) return false;

            // cs.getNamespacedID()
            Method getNamespacedId = csClass.getMethod("getNamespacedID");
            String namespacedId = (String) getNamespacedId.invoke(cs);

            return ring.id().equalsIgnoreCase(namespacedId);
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "[RingChecker] ItemsAdder reflection error", e);
            return false;
        }
    }
}
