package space.cutekitten.mmbstate.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Redirect(method = "doItemPick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPickStack(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/item/ItemStack;"))
    private ItemStack getPickStack(Block block, BlockView world, BlockPos pos, BlockState state) {
        ItemStack stack = block.getPickStack(world, pos, state);

        boolean needsOverride = block != Block.getBlockFromItem(stack.getItem());
        if (!Screen.hasAltDown() || (state.getEntries().isEmpty() && !needsOverride)) return stack;

        if (stack.isEmpty()) stack = new ItemStack(Blocks.BARRIER);

        NbtCompound blockStateTag = new NbtCompound();
        state.getEntries().forEach((stateProperty, value) -> blockStateTag.putString(stateProperty.getName(), value.toString()));

        if (needsOverride) {
            NbtCompound blockOverride = new NbtCompound();
            blockOverride.putString("id", Registry.BLOCK.getId(state.getBlock()).toString());

            stack.setSubNbt("BlockOverride", blockOverride);
        }
        stack.setSubNbt("BlockStateTag", blockStateTag);

        NbtCompound nbtCompound = new NbtCompound();
        NbtList nbtList = new NbtList();
        nbtList.add(NbtString.of("\"(+STATE)\""));
        nbtCompound.put("Lore", nbtList);
        stack.setSubNbt("display", nbtCompound);
        return stack;
    }

    @Redirect(method = "addBlockEntityNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;setSubNbt(Ljava/lang/String;Lnet/minecraft/nbt/NbtElement;)V"))
    private void getExistingLore(ItemStack stack, String key, NbtElement element) {
        if (!key.equals("display")) {
            stack.setSubNbt(key, element);
            return;
        }

        NbtCompound display = stack.getOrCreateSubNbt("display");

        NbtList nbtList;
        if (!display.contains("Lore")) {
            nbtList = new NbtList();
            display.put("Lore", nbtList);
        } else nbtList = display.getList("Lore", 8);

        nbtList.add(NbtString.of("\"(+NBT)\""));
    }
}
