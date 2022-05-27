package xerca.xercapaint.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import xerca.xercapaint.common.XercaPaint;
import xerca.xercapaint.common.item.ItemCanvas;
import xerca.xercapaint.common.item.ItemPalette;
import xerca.xercapaint.common.item.Items;
import xerca.xercapaint.common.packets.CloseGuiPacket;
import xerca.xercapaint.common.packets.OpenGuiPacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityEasel extends Entity {

    private static final DataParameter<ItemStack> DATA_CANVAS;
    private EntityPlayer painter = null;
    private Runnable dropDeferred = null;
    private int dropWaitTicks = 0;

    static {
        DATA_CANVAS = EntityDataManager.createKey(EntityEasel.class, DataSerializers.ITEM_STACK);
    }

    public EntityEasel(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(DATA_CANVAS, ItemStack.EMPTY);
    }

    public void setPainter(EntityPlayer painter) {
        this.painter = painter;
    }

    public EntityPlayer getPainter() {
        return this.painter;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean attackEntityFrom(@Nonnull DamageSource damageSource, float p_31580_) {
        if (!this.world.isRemote && !this.isDead) {
            if (!getItem().isEmpty() && !damageSource.isExplosion()) {
                this.dropItem(damageSource.getTrueSource(), false);
            } else {
                this.dropItem(damageSource.getTrueSource());
                kill();
            }
        }
        return false;
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound tag) {
        NBTTagCompound itemTag = tag.getCompoundTag("Item");
        if (itemTag != null && !itemTag.hasNoTags()) {
            ItemStack var3 = new ItemStack(itemTag);
            if (var3.isEmpty()) {
                XercaPaint.LOGGER.warn("Unable to load item from: {}", itemTag);
            }
            this.setItem(var3, false);
        }
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound tag) {
        if (!this.getItem().isEmpty()) {
            tag.setTag("Item", getItem().writeToNBT(new NBTTagCompound()));
        }
    }

    private void showBreakingParticles() {
        if (this.world instanceof WorldServer) {
            ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY + 0.6666666666666666D, this.posZ, 10, (double) (this.getCollisionBorderSize() / 4.0F), (double) (this.getCollisionBorderSize() / 4.0F), (double) (this.getCollisionBorderSize() / 4.0F), 0.05D);
        }
    }

    public void kill() {
        showBreakingParticles();
        this.setDead();
    }

    public void dropItem(@Nullable Entity entity) {
        this.dropItem(entity, true);
    }

    private void dropItem(@Nullable Entity entity, boolean dropSelf) {
        if (painter != null) {
            if (!world.isRemote) {
                if (dropDeferred == null) {
                    CloseGuiPacket pack = new CloseGuiPacket();
                    XercaPaint.network.sendTo(pack, (EntityPlayerMP) painter);
                    dropDeferred = () -> doDrop(entity, dropSelf);
                }
            }
        } else {
            doDrop(entity, dropSelf);
        }
    }

    public void doDrop(@Nullable Entity entity, boolean dropSelf) {
        ItemStack canvasStack = this.getItem();
        this.setItem(ItemStack.EMPTY);

        if (!canvasStack.isEmpty()) {
            canvasStack = canvasStack.copy();
            this.spawnAtLocation(canvasStack);
        }

        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.isCreative()) {
                return;
            }
        }

        if (dropSelf && this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.spawnAtLocation(this.getEaselItemStack());
        }
    }

    public ItemStack getItem() {
        return this.getDataManager().get(DATA_CANVAS);
    }

    public void setItem(ItemStack itemStack) {
        this.setItem(itemStack, true);
    }

    public void setItem(ItemStack itemStack, boolean makeSound) {
        if (!itemStack.isEmpty()) {
            itemStack = itemStack.copy();
            itemStack.setCount(1);
            //itemStack.setEntityRepresentation(this); // TODO ????
        }

        this.getDataManager().set(DATA_CANVAS, itemStack);
        if (makeSound) {
            if (!itemStack.isEmpty()) {
                this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
            } else {
                this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
            }
        }
    }

//    public SlotAccess getSlot(int i) {
//        return i == 0 ? new SlotAccess() {
//            public ItemStack get() {
//                return EntityEasel.this.getItem();
//            }
//
//            public boolean set(ItemStack itemStack) {
//                EntityEasel.this.setItem(itemStack);
//                return true;
//            }
//        } : super.getSlot(i);
//    }

// TODO ????
//    @Override
//    public void notifyDataManagerChange(final DataParameter<?> key) {
//        super.notifyDataManagerChange(key);
//        if (key.equals(DATA_CANVAS)) {
//            ItemStack itemStack = this.getItem();
//            if (!itemStack.isEmpty() && itemStack.getEntityRepresentation() != this) {
//                itemStack.setEntityRepresentation(this);
//            }
//        }
//    }
    // TODO ????
//    @Override
//    public IPacket<?> getAddEntityPacket() {
//        return NetworkHooks.getEntitySpawningPacket(this);
//    }

    @Override
    public boolean processInitialInteract(final EntityPlayer player, final EnumHand hand) {
        ItemStack itemInHand = player.getHeldItem(hand);
        boolean isEaselFilled = !this.getItem().isEmpty();
        boolean handHoldsCanvas = itemInHand.getItem() instanceof ItemCanvas;
        boolean handHoldsPalette = itemInHand.getItem() instanceof ItemPalette;
        if (this.world.isRemote) {
            return isEaselFilled || handHoldsCanvas;
        } else {
            if (!isEaselFilled) {
                if (handHoldsCanvas && !this.isDead) {
                    this.setItem(itemInHand);
                    itemInHand.shrink(1);
                }
            } else {
                boolean unused = this.painter == null;
                boolean toEdit = handHoldsPalette && !(getItem().hasTagCompound() && getItem().getTagCompound().getInteger("generation") > 0);
                boolean allowed = unused || !toEdit;
                OpenGuiPacket pack = new OpenGuiPacket(this.getEntityId(), allowed, toEdit, hand);
                XercaPaint.network.sendTo(pack, (EntityPlayerMP) player);
                if (toEdit && allowed) {
                    this.painter = player;
                }
            }

            return true;
        }
    }

    protected ItemStack getEaselItemStack() {
        return new ItemStack(Items.ITEM_EASEL);
    }

    public ItemStack getPickResult() {
        ItemStack canvas = this.getItem();
        return canvas.isEmpty() ? this.getEaselItemStack() : canvas.copy();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        //move(MoverType.SELF, 0, -0.25, 0);
        // TODO Check importance
        //  reapplyPosition();
        if (!world.isRemote) {
            if (dropDeferred != null && painter == null) {
                dropDeferred.run();
                dropDeferred = null;
            }
        }
        if (painter != null) {
            if (painter.isDead) {
                painter = null;
            }
        }
    }

    @Override
    public void setItemStackToSlot(final EntityEquipmentSlot slotIn, final ItemStack stack) {

    }

    private void spawnAtLocation(ItemStack stack) {
        if (!world.isRemote) {
            world.spawnEntity(new EntityItem(world, posX, posY, posZ, stack));
        }
    }
}
