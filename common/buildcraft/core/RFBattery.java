package buildcraft.core;

import buildcraft.api.core.NetworkData;
import cofh.api.energy.IEnergyStorage;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Reference implementation of {@link IEnergyStorage}. Use/extend this or
 * implement your own.
 * 
 * Changed to synchronizable.
 * 
 * @author King Lemming
 * 
 */
public class RFBattery implements IEnergyStorage {
	@NetworkData
	protected int energy;
	@NetworkData
	protected int capacity;
	@NetworkData
	protected int maxReceive;
	@NetworkData
	protected int maxExtract;

	public RFBattery() {

		this(0, 0, 0);
	}

	public RFBattery(int capacity, int maxReceive) {

		this(capacity, maxReceive, 0);
	}
	
	

	public RFBattery(int capacity, int maxReceive, int maxExtract) {

		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
	}

	public RFBattery readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("Energy")) {
			this.energy = nbt.getInteger("Energy");

			if (energy > capacity) {
				energy = capacity;
			}
		}
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		if (energy < 0) {
			energy = 0;
		}
		nbt.setInteger("Energy", energy);
		nbt.setInteger("MaxReceive", maxReceive);
		nbt.setInteger("MaxExtract", maxExtract);
		nbt.setInteger("Capacity", capacity);
		return nbt;
	}

	public void setCapacity(int capacity) {

		this.capacity = capacity;

		if (energy > capacity) {
			energy = capacity;
		}
	}

	public void setMaxTransfer(int maxTransfer) {

		setMaxReceive(maxTransfer);
		setMaxExtract(maxTransfer);
	}

	public void setMaxReceive(int maxReceive) {

		this.maxReceive = maxReceive;
	}

	public void setMaxExtract(int maxExtract) {

		this.maxExtract = maxExtract;
	}

	public int getMaxReceive() {

		return maxReceive;
	}

	public int getMaxExtract() {

		return maxExtract;
	}

	/**
	 * This function is included to allow for server -> client sync. Do not call
	 * this externally to the containing Tile Entity, as not all IEnergyHandlers
	 * are guaranteed to have it.
	 * 
	 * @param energy
	 */
	public void setEnergyStored(int energy) {

		this.energy = energy;

		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}

	/**
	 * This function is included to allow the containing tile to directly and
	 * efficiently modify the energy contained in the EnergyStorage. Do not rely
	 * on this externally, as not all IEnergyHandlers are guaranteed to have it.
	 * 
	 * @param energy
	 */
	public void modifyEnergyStored(int energy) {

		this.energy += energy;

		if (this.energy > capacity) {
			this.energy = capacity;
		} else if (this.energy < 0) {
			this.energy = 0;
		}
	}

	/* IEnergyStorage */
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {

		int energyReceived = Math.min(capacity - energy,
				Math.min(this.maxReceive, maxReceive));

		if (!simulate) {
			energy += energyReceived;
		}
		return energyReceived;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {

		int energyExtracted = Math.min(energy,
				Math.min(this.maxExtract, maxExtract));

		if (!simulate) {
			energy -= energyExtracted;
		}
		return energyExtracted;
	}

	@Override
	public int getEnergyStored() {

		return energy;
	}

	@Override
	public int getMaxEnergyStored() {

		return capacity;
	}
	
	public static RFBattery fromNBT(NBTTagCompound nbt) {
		RFBattery bat = null;
		if (nbt.hasKey("Energy") && nbt.hasKey("MaxReceive") && nbt.hasKey("MaxExtract") && nbt.hasKey("Capacity")) {
			bat = new RFBattery();
			bat.energy = nbt.getInteger("Energy");
			bat.maxExtract = nbt.getInteger("MaxExtract");
			bat.maxReceive = nbt.getInteger("MaxReceive");
			bat.capacity = nbt.getInteger("Capacity");
			
			if (bat.energy > bat.capacity) {
				bat.energy = bat.capacity;
			}
		}
		return bat;
	}
	

}
