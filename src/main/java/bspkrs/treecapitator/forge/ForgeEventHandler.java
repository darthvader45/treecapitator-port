package bspkrs.treecapitator.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import net.minecraftforge.fml.common.registry.GameData;
import bspkrs.treecapitator.Treecapitator;
import bspkrs.treecapitator.TreecapitatorMod;
import bspkrs.treecapitator.config.TCConfigHandler;
import bspkrs.treecapitator.config.TCSettings;
import bspkrs.treecapitator.registry.ModConfigRegistry;
import bspkrs.treecapitator.registry.TreeDefinition;
import bspkrs.treecapitator.registry.TreeRegistry;
import bspkrs.treecapitator.util.TCLog;
import bspkrs.util.ModulusBlockID;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class ForgeEventHandler
{
    private final Map<String, Boolean>         playerSneakingMap = new ConcurrentHashMap<String, Boolean>(64);
    private final Map<CachedBreakSpeed, Float> breakSpeedCache   = new ConcurrentHashMap<CachedBreakSpeed, Float>(64);

    @SubscribeEvent
    public void onBlockClicked(LeftClickBlock event)
    {
    	// I think what I've done is short-circuit the "left click block" PlayerInteractEvent.Action from 1.8.9 --AstroTibs
    	if (TreecapitatorMod.proxy.isEnabled() && !TCSettings.sneakAction.equalsIgnoreCase("none") )// && event.action.equals(PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
        {
            if (!event.getEntityPlayer().world.isAirBlock(event.getPos()))
                playerSneakingMap.put(event.getEntityPlayer().getGameProfile().getName(), event.getEntityPlayer().isSneaking());
        }
    }

    @SubscribeEvent
    public void getPlayerBreakSpeed(BreakSpeed event)
    {
        ModulusBlockID blockID = new ModulusBlockID(event.getState(), 4);
        BlockPos pos = event.getPos();

        if (TreecapitatorMod.proxy.isEnabled() && (TreeRegistry.instance().isRegistered(blockID)
                || (TCSettings.allowAutoTreeDetection && TreeRegistry.canAutoDetect(event.getEntityPlayer().world, event.getState().getBlock(), pos)))
                && Treecapitator.isBreakingPossible(event.getEntityPlayer(), event.getPos(), false))
        {
            TreeDefinition treeDef;
            if (TCSettings.allowAutoTreeDetection)
                treeDef = TreeRegistry.autoDetectTree(event.getEntityPlayer().world, blockID, pos, TCSettings.allowDebugLogging);
            else
                treeDef = TreeRegistry.instance().get(blockID);

            if (treeDef != null)
            {
                Boolean isSneaking = playerSneakingMap.get(event.getEntityPlayer().getGameProfile().getName());
                boolean swappedSneak = !(((isSneaking != null) && (isSneaking.booleanValue() == event.getEntityPlayer().isSneaking())) || (isSneaking == null));

                CachedBreakSpeed cachedBreakSpeed = new CachedBreakSpeed(event, swappedSneak);
                Float newBreakSpeed = breakSpeedCache.get(cachedBreakSpeed);

                if (newBreakSpeed == null)
                {
                    if (!swappedSneak)
                    {
                        if (TCSettings.treeHeightDecidesBreakSpeed)
                        {
                            if (Treecapitator.isBreakingEnabled(event.getEntityPlayer()))
                            {
                                int height = Treecapitator.getTreeHeight(treeDef, event.getEntityPlayer().world, pos, event.getEntityPlayer());
                                if (height > 1)
                                    event.setNewSpeed(event.getOriginalSpeed() / (height * TCSettings.treeHeightModifier));
                            }
                        }
                        else if (Treecapitator.isBreakingEnabled(event.getEntityPlayer()))
                            event.setNewSpeed(event.getOriginalSpeed() * treeDef.breakSpeedModifier());
                    }
                    else
                        event.setNewSpeed(0.0f);

                    breakSpeedCache.put(cachedBreakSpeed, event.getNewSpeed());
                }
                else
                    event.setNewSpeed(newBreakSpeed);
            }
        }
    }

    @SubscribeEvent
    public void onBlockHarvested(BreakEvent event)
    {
        if ((event.getState() != null) && (event.getWorld() != null) && (event.getPlayer() != null))
        {
            if (TreecapitatorMod.proxy.isEnabled() && !event.getWorld().isRemote)
            {
                ModulusBlockID blockID = new ModulusBlockID(event.getState(), 4);

                if ((TreeRegistry.instance().isRegistered(blockID) || (TCSettings.allowAutoTreeDetection
                        && TreeRegistry.canAutoDetect(event.getWorld(), event.getState().getBlock(), event.getPos())))
                        && Treecapitator.isBreakingPossible(event.getPlayer(), event.getPos(), TCSettings.allowDebugLogging))
                {
                    BlockPos pos = event.getPos();
                    if (TreeRegistry.instance().trackTreeChopEventAt(pos))
                    {
                        TCLog.debug("BlockID " + blockID + " is a log.");

                        TreeDefinition treeDef;
                        if (TCSettings.allowAutoTreeDetection)
                            treeDef = TreeRegistry.autoDetectTree(event.getWorld(), blockID, pos, TCSettings.allowDebugLogging);
                        else
                            treeDef = TreeRegistry.instance().get(blockID);

                        if (treeDef != null)
                            (new Treecapitator(event.getPlayer(), treeDef)).onBlockHarvested(event.getWorld(), pos);

                        TreeRegistry.instance().endTreeChopEventAt(pos);
                    }
                    else
                        TCLog.debug("Previous chopping event detected for block @%s", pos.toString());
                }
            }

            cleanUpCaches(event.getPlayer());

            if (ModConfigRegistry.instance().isChanged())
                ModConfigRegistry.instance().writeChangesToConfig(TCConfigHandler.instance().getConfig());
        }
    }

    public void cleanUpCaches(EntityPlayer player)
    {
        List<CachedBreakSpeed> toRemove = new ArrayList<CachedBreakSpeed>();
        for (CachedBreakSpeed bs : breakSpeedCache.keySet())
            if (bs.getEntityPlayer().getGameProfile().getName().equals(player.getGameProfile().getName()))
                toRemove.add(bs);

        for (CachedBreakSpeed bs : toRemove)
            breakSpeedCache.remove(bs);

        if (playerSneakingMap.containsKey(player.getGameProfile().getName()))
            playerSneakingMap.remove(player.getGameProfile().getName());
    }

    private class CachedBreakSpeed extends BreakSpeed
    {
        private final boolean isSneaking;
        private final boolean swappedSneak;

        public CachedBreakSpeed(BreakSpeed event, boolean swappedSneak)
        {
            super(event.getEntityPlayer(), event.getState(), event.getOriginalSpeed(), event.getPos());
            isSneaking = event.getEntityPlayer().isSneaking();
            this.swappedSneak = swappedSneak;
        }
        
        // I do not know what these do but they don't seem to botch Treecapitator :X --AstroTibs
        /*
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;

            if (!(o instanceof CachedBreakSpeed))
                return false;

            CachedBreakSpeed bs = (CachedBreakSpeed) o;

            ItemStack oItem = bs.getEntityPlayer().getActiveItemStack();//.getCurrentEquippedItem();
            ItemStack thisItem = getEntityPlayer().getActiveItemStack();//.getCurrentEquippedItem();

            return bs.getEntityPlayer().getGameProfile().getName().equals(getEntityPlayer().getGameProfile().getName())
                    && ((oItem != null) && (oItem.getItem() != null) ? ((thisItem != null) && (thisItem.getItem() != null)
                            ? GameData.getItemRegistry().getNameForObject(thisItem.getItem()).equals(GameData.getItemRegistry().getNameForObject(oItem.getItem())) : false)
                            : (thisItem == null) || (thisItem.getItem() == null))
                    && GameData.getBlockRegistry().getNameForObject(bs.getState().getBlock()).equals(GameData.getBlockRegistry().getNameForObject(getState().getBlock()))
                    && (bs.isSneaking == isSneaking) && (bs.swappedSneak == swappedSneak)
                    && (bs.getState().getBlock().getMetaFromState(bs.getState()) == getState().getBlock().getMetaFromState(getState()))
                    && (bs.getOriginalSpeed() == getOriginalSpeed()) && (bs.getPos().equals(getPos()));
        }

        @Override
        public int hashCode()
        {
            ItemStack thisItem = getEntityPlayer().getActiveItemStack();//.getCurrentEquippedItem();
            HashFunction hf = Hashing.md5();
            Hasher h = hf.newHasher()
                    .putString(getEntityPlayer().getGameProfile().getName(), Charsets.UTF_8)
                    .putString(GameData.getBlockRegistry().getNameForObject(getState().getBlock()).toString(), Charsets.UTF_8)
                    .putBoolean(isSneaking)
                    .putBoolean(swappedSneak)
                    .putInt(getState().getBlock().getMetaFromState(getState()))
                    .putFloat(getOriginalSpeed())
                    .putInt(getPos().hashCode());

            if ((thisItem != null) && (thisItem.getItem() != null))
                h.putString(GameData.getItemRegistry().getNameForObject(thisItem.getItem()).toString(), Charsets.UTF_8)
                        .putInt(thisItem.getMetadata());

            return h.hash().hashCode();
        }
        */
    }
}
