/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PerditionCalculator;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.IPipeTransportPowerHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;

public class PipePowerWood extends Pipe<PipeTransportPower> implements IPowerReceptor, IEnergyHandler, IPipeTransportPowerHook {

	public final boolean[] powerSources = new boolean[6];

	protected int standardIconIndex = PipeIconProvider.TYPE.PipePowerWood_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllWood_Solid.ordinal();

	private PowerHandler powerHandler;
	private final SafeTimeTracker sourcesTracker = new SafeTimeTracker(1);
	private boolean full;

	public PipePowerWood(Item item) {
		super(new PipeTransportPower(), item);
		transport.initFromPipe(getClass());
		this.powerHandler = new PowerHandler(this, Type.PIPE);
		powerHandler.configure(0, 500, 1, 1500);
		powerHandler.setPerdition(new PerditionCalculator(PerditionCalculator.MIN_POWERLOSS));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		return standardIconIndex;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (container.getWorldObj().isRemote) {
			return;
		}

		double mjStored = this.powerHandler.getEnergyStored();

		//System.out.println("mjStored = " + mjStored);
		
		if (mjStored > 0) {
			int sources = 0;

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (!container.isPipeConnected(o)) {
					powerSources[o.ordinal()] = false;
					continue;
				}

				if (isPowerSource(o)) {
					powerSources[o.ordinal()] = true;
				}

				if (powerSources[o.ordinal()]) {
					sources++;
				}
			}

			if (sources <= 0) {
				mjStored = mjStored > 5 ? mjStored - 5 : 0;
				return;
			}

			double energyToRemove;

			/*if (mjStored > 40) {
				energyToRemove = mjStored / 40 + 4;
			} else if (mjStored > 10) {
				energyToRemove = mjStored / 10;
			} else {
				energyToRemove = 1;
			}*/
			// TODO
			energyToRemove = Math.min(32.0, mjStored);
			energyToRemove /= sources;

			for (ForgeDirection o : ForgeDirection.VALID_DIRECTIONS) {
				if (!powerSources[o.ordinal()]) {
					continue;
				}
				
				TileEntity tile = container.getTile(o);
				
				double energyUsable = mjStored > energyToRemove ? energyToRemove : mjStored;
				double energySent = transport.receiveEnergy(o, energyUsable);

				if (energySent > 0) {
					mjStored -= energySent;
				}
			}
		}
		
		this.powerHandler.setEnergy(mjStored);
	}

	public boolean requestsPower() {
		if (full) {
			boolean request = this.powerHandler.getEnergyStored() < 1500 / 2;

			if (request) {
				full = false;
			}

			return request;
		}

		full = this.powerHandler.getEnergyStored() >= 1500 - 10;

		return !full;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		this.powerHandler.writeToNBT(data);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			data.setBoolean("powerSources[" + i + "]", powerSources[i]);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.powerHandler.readFromNBT(data);

		for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
			powerSources[i] = data.getBoolean("powerSources[" + i + "]");
		}
	}

	@Override
	public double receiveEnergy(ForgeDirection from, double val) {
		return -1;
	}

	@Override
	public double requestEnergy(ForgeDirection from, double amount) {
		if (container.getTile(from) instanceof IPipeTile) {
			return amount;
		} else {
			return 0;
		}
	}

	public boolean isPowerSource(ForgeDirection from) {
		TileEntity tile = container.getTile(from);
		if(tile instanceof IPowerEmitter) {
			return true;
		} else if(tile instanceof IEnergyHandler) {
			return ((IEnergyHandler)tile).canConnectEnergy(from.getOpposite());
		} else {
			return false;
		}
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {

	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive,
			boolean simulate) {
		double mjStored = this.powerHandler.getEnergyStored();
		int maxEnergyToAdd = Math.min(maxReceive, Math.min(320, (int)Math.round((1500.0 - mjStored) * 10)));
		if(!simulate) {
			System.out.println("givink energy 1 = " + maxEnergyToAdd);
			this.powerHandler.setEnergy(mjStored + ((double)maxEnergyToAdd / 10.0));
		}
		return maxEnergyToAdd;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract,
			boolean simulate) {
		return 0;
	}

	@Override
	public int getEnergyStored(ForgeDirection from) {
		return (int)Math.round(this.powerHandler.getEnergyStored() * 10);
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection from) {
		return (int)Math.round(this.powerHandler.getMaxEnergyStored() * 10);
	}
}
