/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.blueprints.ITileBuilder;
import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.NetworkData;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.MjBattery;
import buildcraft.core.IBoxProvider;
import buildcraft.core.LaserData;
import buildcraft.core.RFBattery;
import buildcraft.core.TileRFBuildCraft;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public abstract class TileAbstractBuilder extends TileRFBuildCraft implements ITileBuilder, IInventory, IBoxProvider {

	/**
	 * Computes the maximum amount of energy required to build a full chest,
	 * plus a safeguard. That's a nice way to evaluate maximum amount of energy
	 * that need to be in a builder.
	 */
	private static final double FULL_CHEST_ENERGY = 9 * 3 * 64 * SchematicRegistry.BUILD_ENERGY + 1000;

	@NetworkData
	public LinkedList<LaserData> pathLasers = new LinkedList<LaserData> ();

	public ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();

	protected SafeTimeTracker buildTracker = new SafeTimeTracker(5);

	private double mjPrev = 0;
	private int mjUnchangedCycles = 0;
	
	@Override
	public RFBattery getDefaultBattery() {
		return new RFBattery(1000, 250);
	}
	
	@Override
	public void initialize () {
		super.initialize();

		if (worldObj.isRemote) {
			RPCHandler.rpcServer(this, "uploadBuildersInAction");
		}
	}

	@RPC (RPCSide.SERVER)
	private void uploadBuildersInAction (RPCMessageInfo info) {
		for (BuildingItem i : buildersInAction) {
			RPCHandler.rpcPlayer(this, "launchItem", info.sender, i);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (mjPrev != battery.getEnergyStored()/10) {
			mjPrev = battery.getEnergyStored()/10;
			mjUnchangedCycles = 0;
		}

		BuildingItem toRemove = null;

		for (BuildingItem i : buildersInAction) {
			i.update();

			if (i.isDone) {
				toRemove = i;
			}
		}

		if (toRemove != null) {
			buildersInAction.remove(toRemove);
		}

		if (mjPrev != battery.getEnergyStored()/10) {
			mjPrev = battery.getEnergyStored()/10;
			mjUnchangedCycles = 0;
		}

		mjUnchangedCycles++;

		/**
		 * After 100 cycles with no consumption or additional power, start to
		 * slowly to decrease the amount of power available in the builder.
		 */
		if (mjUnchangedCycles > 100) {
			battery.modifyEnergyStored(-100 *10);

			if (battery.getEnergyStored()/10 < 0) {
				battery.modifyEnergyStored(0);
			}

			mjPrev = battery.getEnergyStored()*10;
		}
	}

	public ArrayList<BuildingItem> getBuilders() {
		return buildersInAction;
	}

	public LinkedList<LaserData> getPathLaser() {
		return pathLasers;
	}

	@RPC (RPCSide.CLIENT)
	public void launchItem (BuildingItem item) {
		buildersInAction.add(item);
	}

	public void addBuildingItem(BuildingItem item) {
		buildersInAction.add(item);
		RPCHandler.rpcBroadcastPlayers(this, "launchItem", item);
	}

	public final double energyAvailable() {
		return battery.getEnergyStored()/10;
	}

	public final void consumeEnergy(double quantity) {
		battery.modifyEnergyStored((int)quantity *10);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		nbttagcompound.setDouble("mjStored", battery.getEnergyStored()/10);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		battery.setEnergyStored((int) (nbttagcompound.getDouble("mjStored")*10));

		mjPrev = battery.getEnergyStored()/10;
		mjUnchangedCycles = 0;
	}
	
	@Override
        public double getMaxRenderDistanceSquared() {
        	return Double.MAX_VALUE;
    	}
}
