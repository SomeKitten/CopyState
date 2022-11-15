package space.cutekitten.mmbstate.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(net.minecraft.item.BlockItem.class)
public abstract class BlockItemMixin {
    @Shadow public abstract ActionResult place(ItemPlacementContext context);

    @Shadow protected abstract boolean canPlace(ItemPlacementContext context, BlockState state);

    @Shadow public abstract Block getBlock();

    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;"))
    private BlockState getPlacementState(Block block, ItemPlacementContext context) {
        NbtCompound blockOverrideNbt = context.getStack().getSubNbt("BlockOverride");
        if (blockOverrideNbt == null) return block.getPlacementState(context);
        Block overrideBlock = Registry.BLOCK.get(new Identifier(blockOverrideNbt.getString("id")));
        return overrideBlock.getPlacementState(context);
    }

    @Redirect(method = "getPlacementState", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BlockItem;canPlace(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z"))
    private boolean canPlace(BlockItem blockItem, ItemPlacementContext context, BlockState state) {
        NbtCompound blockOverrideNbt = context.getStack().getSubNbt("BlockOverride");
        if (blockOverrideNbt != null) return true;
        return this.canPlace(context, getBlock().getPlacementState(context));
    }
}
