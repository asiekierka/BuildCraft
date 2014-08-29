package buildcraft.core;

import cofh.api.energy.IEnergyHandler;
import buildcraft.api.core.NetworkData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileRFBuildCraft extends TileBuildCraft implements IEnergyHandler{
	
	@NetworkData
	//protected RFBattery battery = invalid;

	private RFBattery battery = null;
	
	public RFBattery getBattery(){
		if(battery == null){
			battery = getDefaultBattery();
		}
		return battery;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		RFBattery tmp = RFBattery.fromNBT(nbttagcompound);
		if(tmp == null){
			battery = getDefaultBattery();
		}else{
			battery = tmp;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		getBattery().writeToNBT(nbttagcompound);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
		return getBattery().extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ForgeDirection arg0) {
		return getBattery().getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ForgeDirection arg0) {
		return getBattery().getMaxEnergyStored();
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
		return getBattery().receiveEnergy(maxReceive, simulate);
	}
	
	protected abstract RFBattery getDefaultBattery();
	
	
	
	
}
