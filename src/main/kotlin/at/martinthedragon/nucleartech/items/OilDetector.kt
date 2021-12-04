package at.martinthedragon.nucleartech.items

import at.martinthedragon.nucleartech.NuclearTags
import at.martinthedragon.nucleartech.SoundEvents
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TranslatableComponent
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class OilDetector(properties: Properties) : Item(properties) {
    override fun appendHoverText(stack: ItemStack, world: Level?, tooltip: MutableList<Component>, flag: TooltipFlag) {
        autoTooltip(stack, tooltip)
    }

    override fun use(world: Level, player: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        fun checkColumns(world: Level, playerPos: BlockPos, offsetX: Int, offsetZ: Int): Boolean {
            val oilOreTag = NuclearTags.Blocks.ORES_OIL
            for (i in 5..(playerPos.y + 15)) {
                val found = when {
                    offsetX == 0 && offsetZ == 0 -> world.getBlockState(BlockPos(playerPos.x, i, playerPos.z)).`is`(oilOreTag)
                    offsetX == 0 -> world.getBlockState(BlockPos(playerPos.x, i, playerPos.z + offsetZ)).`is`(oilOreTag) ||
                            world.getBlockState(BlockPos(playerPos.x, i, playerPos.z - offsetZ)).`is`(oilOreTag)
                    offsetZ == 0 -> world.getBlockState(BlockPos(playerPos.x + offsetX, i, playerPos.z)).`is`(oilOreTag) ||
                            world.getBlockState(BlockPos(playerPos.x - offsetX, i, playerPos.z)).`is`(oilOreTag)
                    else -> world.getBlockState(BlockPos(playerPos.x + offsetX, i, playerPos.z + offsetZ)).`is`(oilOreTag) ||
                            world.getBlockState(BlockPos(playerPos.x - offsetX, i, playerPos.z + offsetZ)).`is`(oilOreTag) ||
                            world.getBlockState(BlockPos(playerPos.x + offsetX, i, playerPos.z - offsetZ)).`is`(oilOreTag) ||
                            world.getBlockState(BlockPos(playerPos.x - offsetX, i, playerPos.z - offsetZ)).`is`(oilOreTag)
                }

                if (found) return true
            }
            return false
        }

        if (world.isClientSide) {
            val playerPosition = player.blockPosition()
            if (checkColumns(world, playerPosition, 0, 0)) {
                player.displayClientMessage(TranslatableComponent("$descriptionId.below").withStyle(ChatFormatting.DARK_GREEN), true)
            } else if (
                checkColumns(world, playerPosition, 5, 0) ||
                checkColumns(world, playerPosition, 0, 5) ||
                checkColumns(world, playerPosition, 10, 0) ||
                checkColumns(world, playerPosition, 0, 10) ||
                checkColumns(world, playerPosition, 5, 5)
            ) {
                player.displayClientMessage(TranslatableComponent("$descriptionId.near").withStyle(ChatFormatting.GOLD), true)
            } else {
                player.displayClientMessage(TranslatableComponent("$descriptionId.no_oil").withStyle(ChatFormatting.RED), true)
            }
        }

        player.playSound(SoundEvents.randomBleep.get(), 1F, 1F)

        val itemStack = player.getItemInHand(hand)
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide)
    }
}
