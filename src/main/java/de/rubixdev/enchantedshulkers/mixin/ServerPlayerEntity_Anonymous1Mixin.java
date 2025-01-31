package de.rubixdev.enchantedshulkers.mixin;

import de.rubixdev.enchantedshulkers.Mod;
import de.rubixdev.enchantedshulkers.interfaces.HasClientMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net.minecraft.server.network.ServerPlayerEntity$1")
public class ServerPlayerEntity_Anonymous1Mixin implements HasClientMod {
    @Unique private boolean hasClientMod = false;
    @SuppressWarnings("FieldMayBeFinal")
    @Unique private boolean inInitPhase =
    //#if MC >= 12002
        false;
    //#else
    //$$     true;
    //#endif

    @Override
    public void enchantedShulkers$setTrue() {
        this.hasClientMod = true;
    }

    //#if MC < 12002
    //$$ @Override
    //$$ public void enchantedShulkers$submit() {
    //$$     this.inInitPhase = false;
    //$$ }
    //#endif

    //////////////////////////////////////////////

    @ModifyVariable(method = "updateState", at = @At("HEAD"), index = 2, argsOnly = true)
    private DefaultedList<ItemStack> updateStateStacks(DefaultedList<ItemStack> stacks) {
        if (this.hasClientMod || this.inInitPhase) return stacks;

        DefaultedList<ItemStack> newStacks = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); ++i) {
            newStacks.set(i, setLore(stacks.get(i).copy()));
        }
        return newStacks;
    }

    @ModifyVariable(method = "updateState", at = @At("HEAD"), index = 3, argsOnly = true)
    private ItemStack updateStateCursorStack(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    @ModifyVariable(method = "updateSlot", at = @At("HEAD"), index = 3, argsOnly = true)
    private ItemStack updateSlot(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    @ModifyVariable(method = "updateCursorStack", at = @At("HEAD"), index = 2, argsOnly = true)
    private ItemStack updateCursorStack(ItemStack stack) {
        if (this.hasClientMod || this.inInitPhase) return stack;
        return setLore(stack.copy());
    }

    //////////////////////////////////////////////

    @Unique
    private static ItemStack setLore(ItemStack stack) {
        NbtList lore = new NbtList();
        if (EnchantmentHelper.getLevel(Mod.SIPHON_ENCHANTMENT, stack) > 0
                || storesEnchantment(Mod.SIPHON_ENCHANTMENT, stack)) {
            lore.add(NbtString.of(enchantmentText("Siphon")));
        }
        if (EnchantmentHelper.getLevel(Mod.REFILL_ENCHANTMENT, stack) > 0
                || storesEnchantment(Mod.REFILL_ENCHANTMENT, stack)) {
            lore.add(NbtString.of(enchantmentText("Refill")));
        }

        if (!lore.isEmpty()) {
            NbtCompound nbt = stack.getOrCreateSubNbt("display");
            nbt.put("Lore", lore);
        }

        return stack;
    }

    @Unique
    private static String enchantmentText(String name) {
        return "{\"text\": \"" + name + "\", \"italic\": false, \"color\": \"gray\"}";
    }

    @Unique
    private static boolean storesEnchantment(Enchantment enchantment, ItemStack stack) {
        if (stack.isEmpty()) return false;
        Identifier identifier = EnchantmentHelper.getEnchantmentId(enchantment);
        NbtList nbtList = EnchantedBookItem.getEnchantmentNbt(stack);
        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            Identifier identifier2 = EnchantmentHelper.getIdFromNbt(nbtCompound);
            if (identifier2 != null && identifier2.equals(identifier)) return true;
        }
        return false;
    }
}
