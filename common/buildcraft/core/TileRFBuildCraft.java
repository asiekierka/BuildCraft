package buildcraft.core;

import cofh.api.energy.IEnergyHandler;
import buildcraft.api.core.NetworkData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class TileRFBuildCraft extends TileBuildCraft implements IEnergyHandler{

	private static final RFBattery invalid = new RFBattery(0,0,0); 
	
	
	@NetworkData
	protected RFBattery battery = invalid;

	@Override
	public void initialize(){
		if(battery == invalid)
			battery = getDefaultBattery();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);

		RFBattery tmp = RFBattery.fromNBT(nbttagcompound);
		if(tmp == null && battery == invalid){
			battery = getDefaultBattery();
		}else{
			battery = tmp;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		battery.writeToNBT(nbttagcompound);
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
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
	
	protected abstract RFBattery getDefaultBattery();
	
	
	
	
}
