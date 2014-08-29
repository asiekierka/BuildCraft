package buildcraft.core;

import cofh.api.energy.IEnergyHandler;
import buildcraft.api.core.NetworkData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileRFBuildCraft extends TileBuildCraft implements IEnergyHandler{

	@NetworkData
	protected RFBattery battery = new RFBattery(0,0,0);

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		battery = RFBattery.fromNBT(nbttagcompound);
		if(battery == null){
			
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		battery.writeToNBT(nbttagcompound);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection arg0) {
		return true;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return battery.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection arg0) {
		return battery.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection arg0) {
		return battery.getMaxEnergyStored();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return battery.receiveEnergy(maxReceive, simulate);
	}
	
	public abstract RFBattery getDefaultBattery();
	
	
	
	
}
