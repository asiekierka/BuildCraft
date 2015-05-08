/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.render;

import java.util.Date;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.IRobotOverlayItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.core.render.RenderLaser;
import buildcraft.robotics.EntityRobot;
import buildcraft.robotics.ItemRobot;

public class RenderRobot extends Render implements IItemRenderer {
	private static final ResourceLocation overlay_red = new ResourceLocation(
			DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_side.png");
	private static final ResourceLocation overlay_cyan = new ResourceLocation(
			DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_bottom.png");
	
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private ModelBase model = new ModelBase() {
	};
	private ModelBase modelHelmet = new ModelBase() {
	};
	private ModelRenderer box, helmetBox;

	public RenderRobot() {
		customRenderItem = new RenderItem() {
			@Override
			public boolean shouldBob() {
				return false;
			}

			@Override
			public boolean shouldSpreadItems() {
				return false;
			}
		};
		customRenderItem.setRenderManager(RenderManager.instance);

		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.setRotationPoint(0.0F, 0.0F, 0.0F);
		helmetBox = new ModelRenderer(modelHelmet, 0, 0);
		helmetBox.addBox(-4F, -4F, -4F, 8, 8, 8);
		helmetBox.setRotationPoint(0.0F, 0.0F, 0.0F);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z);
	}

	private void doRender(EntityRobot robot, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		if (robot.getStackInSlot(0) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(0));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(1) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(1));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(2) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(2));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(3) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(3));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.itemInUse != null) {
			GL11.glPushMatrix();

			GL11.glRotatef((float) (-robot.itemAngle1 / (2 * Math.PI) * 360) + 180, 0, 1, 0);
			GL11.glRotatef((float) (robot.itemAngle2 / (2 * Math.PI) * 360), 0, 0, 1);

			if (robot.itemActive) {
				long newDate = new Date().getTime();
				robot.itemActiveStage = (robot.itemActiveStage + (newDate - robot.lastUpdateTime) / 10) % 45;
				GL11.glRotatef(robot.itemActiveStage, 0, 0, 1);
				robot.lastUpdateTime = newDate;
			}

			GL11.glTranslatef(-0.4F, 0, 0);
			GL11.glRotatef(-45F + 180F, 0, 1, 0);
			GL11.glScalef(0.8F, 0.8F, 0.8F);

			ItemStack itemstack1 = robot.itemInUse;

			if (itemstack1.getItem().requiresMultipleRenderPasses()) {
				for (int k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
					RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, k));
					this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
				}
			} else {
				RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, 0));
				this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
			}

			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(renderManager.renderEngine, robot.laser, EntityLaser.LASER_TEXTURES[1]);
		}

		if (robot.getTexture() != null) {
			renderManager.renderEngine.bindTexture(robot.getTexture());
			float storagePercent = (float) robot.getBattery().getEnergyStored() / (float) robot.getBattery().getMaxEnergyStored();
			box.rotateAngleY = -robot.itemAngle1;
			doRenderRobot(1F / 16F, renderManager.renderEngine, storagePercent, robot.isActive());
		}

		for (ItemStack s : robot.getWearables()) {
			doRenderWearable(robot, renderManager.renderEngine, s);
		}
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityRobot) entity).getTexture();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (RenderManager.instance == null || RenderManager.instance.renderEngine == null) {
			return;
		}

		GL11.glPushMatrix();

		if (item.getItem() == BuildCraftRobotics.robotItem) {
			ItemRobot robot = (ItemRobot) item.getItem();
			RenderManager.instance.renderEngine.bindTexture(robot.getTextureRobot(item));
		}

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glTranslated(0.0, 1.0, 0.7);
		} else if (type == ItemRenderType.ENTITY) {
			GL11.glScaled(0.6, 0.6, 0.6);
		} else if (type == ItemRenderType.INVENTORY) {
			GL11.glScaled(1.5, 1.5, 1.5);
		}
		
		doRenderRobot(1F / 16F, RenderManager.instance.renderEngine, 0.9F, false);
		
		GL11.glPopMatrix();
	}

	private void doRenderItem(ItemStack stack) {
		float renderScale = 0.5f;
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0.28F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(stack);
		customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

		GL11.glPopMatrix();
	}

	private void doRenderWearable(EntityRobot entity, TextureManager textureManager, ItemStack wearable) {
		float robotYaw = 90f + entity.itemAngle1 * 180f / (float)Math.PI;
		if (wearable.getItem() instanceof IRobotOverlayItem) {
			((IRobotOverlayItem) wearable.getItem()).renderRobotOverlay(wearable, textureManager);
		} else if (wearable.getItem() instanceof ItemArmor) {
			GL11.glPushMatrix();
			GL11.glScalef(1.0125F, 1.0125F, 1.0125F);
			GL11.glTranslatef(0.0f, -0.25f, 0.0f);
			GL11.glRotatef(180F, 0, 0, 1);
			textureManager.bindTexture(RenderBiped.getArmorResource(entity, wearable, 0, null));
			ModelBiped model = ForgeHooksClient.getArmorModel(entity, wearable, 0, null);
			if (model != null)
				model.render(entity, 0, 0, 0, robotYaw, 0, 1 / 16F);
			else
				helmetBox.render(1 / 16F);
			GL11.glPopMatrix();
		}
	}
	
	private void doRenderRobot(float factor, TextureManager texManager, float storagePercent, boolean isAsleep) {
		box.render(factor);

		if (!isAsleep) {
			float lastBrightnessX = OpenGlHelper.lastBrightnessX;
			float lastBrightnessY = OpenGlHelper.lastBrightnessY;

			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, storagePercent);
			texManager.bindTexture(overlay_red);
			box.render(factor);

			GL11.glDisable(GL11.GL_BLEND);
			
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			texManager.bindTexture(overlay_cyan);
			box.render(factor);

			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glPopMatrix();

			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
		}
	}
}
