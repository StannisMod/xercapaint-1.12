package xerca.xercapaint.common.item;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemEasel extends Item {

    public ItemEasel(String name) {
        this.setUnlocalizedName(name);
        this.setRegistryName(name);
        this.setMaxStackSize(1);
        this.setCreativeTab(Items.paintTab);
    }

    private static Vec3d atBottomCenterOf(BlockPos pos) {
        return new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ());
    }

    @Override
    public EnumActionResult onItemUse(final EntityPlayer player, final World worldIn, final BlockPos pos, final EnumHand hand, final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        // TODO Fix bugs with easel
        return EnumActionResult.FAIL;
//        if (facing == EnumFacing.DOWN) {
//            return EnumActionResult.FAIL;
//        } else {
//            World level = player.world;
//            ItemStack itemstack = player.getHeldItemMainhand();
//            Vec3d vec3 = atBottomCenterOf(pos);
//            AxisAlignedBB aabb = new AxisAlignedBB(pos);//Entities.EASEL.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
//            if (/*level.checkNoEntityCollision(aabb, (p_40505_) -> true) && */level.getEntitiesWithinAABB(Entity.class, aabb).isEmpty()) {
//                if (level instanceof WorldServer) {
//                    WorldServer serverlevel = (WorldServer) level;
//                    EntityEasel easel = new EntityEasel(serverlevel);//, itemstack.getTagCompound(), null, player, pos, SpawnReason.SPAWN_EGG, true, true);
//
//                    float f = (float) MathHelper.floor((MathHelper.wrapDegrees(player.rotationYaw - 180.0F) + 22.5F) / 45.0F) * 45.0F;
//                    easel.setPositionAndRotation(player.posX, player.posY, player.posZ, f, 0.0F);
//                    //serverlevel.addFreshEntityWithPassengers(easel);
//                    serverlevel.spawnEntity(easel);
//                    level.playSound(null, easel.posX, easel.posY, easel.posZ, SoundEvents.ENTITY_ARMORSTAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
//                }
//
//                itemstack.shrink(1);
//                return level.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
//            } else {
//                return EnumActionResult.FAIL;
//            }
//        }
        //return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }
}
