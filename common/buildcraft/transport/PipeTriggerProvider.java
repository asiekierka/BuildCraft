/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.LinkedList;

import cofh.api.energy.IEnergyHandler;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IOverrideDefaultTriggers;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerProvider;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.triggers.TriggerPipeContents;

public class PipeTriggerProvider implements ITriggerProvider {

	@Override
	public LinkedList<ITrigger> getPipeTriggers(IPipeTile tile) {
		LinkedList<ITrigger> result = new LinkedList<ITrigger>();
		Pipe<?> pipe = null;
		if (tile instanceof TileGenericPipe) {
			pipe = ((TileGenericPipe) tile).pipe;
		}

		if (pipe == null) {
			return result;
		} else if (pipe instanceof IOverrideDefaultTriggers) {
			return ((IOverrideDefaultTriggers) pipe).getTriggers();
		}

		for (Gate gate : pipe.gates) {
			if (gate != null) {
				gate.addTrigger(result);
			}
		}

		switch (tile.getPipeType()) {
			case ITEM:
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsItems.trigger);
				break;
			case FLUID:
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsFluids.trigger);
				break;
			case POWER:
				result.add(TriggerPipeContents.PipeContents.empty.trigger);
				result.add(TriggerPipeContents.PipeContents.containsEnergy.trigger);
				result.add(TriggerPipeContents.PipeContents.tooMuchEnergy.trigger);
			result.add(TriggerPipeContents.PipeContents.requestsEnergy.trigger);

				break;
		case STRUCTURE:
			break;
		}

		if (tile instanceof IEnergyHandler && ((IEnergyHandler)tile).getMaxEnergyStored(ForgeDirection.UNKNOWN) > 0) {
			result.add(BuildCraftCore.triggerEnergyHigh);
			result.add(BuildCraftCore.triggerEnergyLow);
		}

		return result;
	}

	@Override
	public LinkedList<ITrigger> getNeighborTriggers(Block block, TileEntity tile) {
		return null;
	}
}
