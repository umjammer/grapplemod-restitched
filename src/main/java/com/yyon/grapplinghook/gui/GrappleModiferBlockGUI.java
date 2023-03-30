package com.yyon.grapplinghook.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yyon.grapplinghook.blockentity.GrappleModifierBlockEntity;
import com.yyon.grapplinghook.util.GrappleCustomization;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class GrappleModiferBlockGUI extends Screen {
	private static final Identifier texture = new Identifier("grapplemod", "textures/gui/guimodifier_bg.png");

	int xSize = 221;
	int ySize = 221;

	protected int guiLeft;
	protected int guiTop;

	int posy;
	int id;
	HashMap<ClickableWidget, String> options;

	GrappleModifierBlockEntity tile;
	GrappleCustomization customization;

	GrappleCustomization.UpgradeCategories category = null;

	public GrappleModiferBlockGUI(GrappleModifierBlockEntity tile) {
		super(Text.translatable("grapplemodifier.title.desc"));

		this.tile = tile;
		customization = tile.customization;
	}

	@Override
	public void init() {
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;

		this.mainScreen();
	}
	
	class PressCategory implements PressAction {
		GrappleCustomization.UpgradeCategories category;
		public PressCategory(GrappleCustomization.UpgradeCategories category) {
			this.category = category;
		}
		
		public void onPress(ButtonWidget p_onPress_1_) {
			boolean unlocked = tile.isUnlocked(category) || MinecraftClient.getInstance().player.isCreative();

			if (unlocked) {
				showCategoryScreen(category);
			} else {
				notAllowedScreen(category);
			}
		}
	}
	
	class PressBack implements PressAction {
		public void onPress(ButtonWidget p_onPress_1_) {
			mainScreen();
		}
	}

	public void mainScreen() {
		clearScreen();

		this.addDrawableChild(ButtonWidget.builder(
								Text.translatable("grapplemodifier.close.desc"),
								onPress -> close())
						.dimensions(this.guiLeft + 10, this.guiTop + this.ySize - 20 - 10, 50, 20)
						.build()
		);

		this.addDrawableChild(ButtonWidget.builder(
						Text.translatable("grapplemodifier.reset.desc"),
						onPress -> {
							customization = new GrappleCustomization();
							mainScreen();
						})
				.dimensions(this.guiLeft + this.xSize - 50 - 10, this.guiTop + this.ySize - 20 - 10, 50, 20)
				.build()
		);

		this.addDrawableChild(ButtonWidget.builder(
						Text.translatable("grapplemodifier.helpbutton.desc"),
						onPress -> helpScreen())
				.dimensions(this.guiLeft + 10 + 75, this.guiTop + this.ySize - 20 - 10, 50, 20)
				.build()
		);

		int y = 0;
		int x = 0;
		for (int i = 0; i < GrappleCustomization.UpgradeCategories.size(); i++) {
			GrappleCustomization.UpgradeCategories category = GrappleCustomization.UpgradeCategories.fromInt(i);
			if (category != GrappleCustomization.UpgradeCategories.LIMITS) {
				if (i == GrappleCustomization.UpgradeCategories.size()/2) {
					y = 0;
					x += 1;
				}

				this.addDrawableChild(ButtonWidget.builder(
									Text.literal(category.getName()),
									new PressCategory(category))
								.dimensions(this.guiLeft + 10 + 105*x, this.guiTop + 15 + 30 * y, 95, 20)
								.build()
				);

				y += 1;
			}
		}

		this.addDrawableChild(new TextWidget(
				Text.translatable("grapplemodifier.apply.desc"),
				this.guiLeft + 10, this.guiTop + this.ySize - 20 - 10 - 10
		));
	}

	static class BackgroundWidget extends ClickableWidget {

		public BackgroundWidget(int posX, int posY, int sizeVertical, int sizeHorizontal, Text text) {
			super(posX, posY, sizeVertical, sizeHorizontal, text);
			this.active = false;
		}
		
		public BackgroundWidget(int x, int y, int w, int h) {
			this(x, y, w, h, Text.literal(""));
		}
		
	    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
			RenderSystem.setShaderTexture(0,texture);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexture(stack, this.getX(), this.getY(), 0, 0, this.width, this.height);
	    }

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder narrationElementOutput) {

		}
	}

	public void clearScreen() {
		this.category = null;
		posy = 10;
		id = 10;
		options = new HashMap<>();
		this.clearChildren();
		
		this.addDrawableChild(new BackgroundWidget(this.guiLeft, this.guiTop, this.xSize, this.ySize));
	}
	
	static class TextWidget extends ClickableWidget {
		public TextWidget(int posX, int posY, int sizeVertical, int sizeHorizontal, Text text) {
			super(posX, posY, sizeVertical, sizeHorizontal, text);
		}
		
		public TextWidget(Text text, int x, int y) {
			this(x, y, 50, 15 * text.getString().split("\n").length + 5, text);
		}
		
		public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			TextRenderer fontRenderer = minecraft.textRenderer;
			RenderSystem.setShaderTexture(0,WIDGETS_TEXTURE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.enableDepthTest();
			int colour = this.active ? 16777215 : 10526880;
			int lineno = 0;
			for (String s : this.getMessage().getString().split("\n")) {
				drawTextWithShadow(stack, fontRenderer, Text.literal(s), this.getX(), this.getY() + lineno*15, colour | MathHelper.ceil(this.alpha * 255.0F) << 24);
				lineno++;
			}
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder narrationElementOutput) {

		}
	}

	public void notAllowedScreen(GrappleCustomization.UpgradeCategories category) {
		clearScreen();

		this.addDrawableChild(
				ButtonWidget.builder(
							Text.translatable("grapplemodifier.back.desc"),
							new PressBack())
						.dimensions(this.guiLeft + 10, this.guiTop + this.ySize - 20 - 10, 50, 20)
						.build()
		);

		this.category = category;
		this.addDrawableChild(new TextWidget(Text.translatable("grapplemodifier.unlock1.desc"), this.guiLeft + 10, this.guiTop + 10));
		this.addDrawableChild(new TextWidget(Text.literal(this.category.getName()), this.guiLeft + 10, this.guiTop + 25));
		this.addDrawableChild(new TextWidget(Text.translatable("grapplemodifier.unlock2.desc"), this.guiLeft + 10, this.guiTop + 40));
		this.addDrawableChild(new TextWidget(Text.translatable("grapplemodifier.unlock3.desc"), this.guiLeft + 10, this.guiTop + 55));
		this.addDrawableChild(new TextWidget(new ItemStack(this.category.getItem()).toHoverableText(), this.guiLeft + 10, this.guiTop + 70));
		this.addDrawableChild(new TextWidget(Text.translatable("grapplemodifier.unlock4.desc"), this.guiLeft + 10, this.guiTop + 85));
	}

	public void helpScreen() {
		clearScreen();

		this.addDrawableChild(
				ButtonWidget.builder(
								Text.translatable("grapplemodifier.back.desc"),
								new PressBack())
						.dimensions(this.guiLeft + 10, this.guiTop + this.ySize - 20 - 10, 50, 20)
						.build()
		);

		this.addDrawableChild(new TextWidget(
						Text.translatable("grapplemodifier.help.desc"),
						this.guiLeft + 10, this.guiTop + 10
		));
		
	}
	
	class GuiCheckbox extends CheckboxWidget {
		String option;
		public Text tooltip;

		public GuiCheckbox(int x, int y, int w, int h, Text text, boolean val, String option, Text tooltip) {
			super(x, y, w, h, text, val);
			this.option = option;
			this.tooltip = tooltip;
		}
		
		@Override
		public void onPress() {
			super.onPress();
			
			customization.setBoolean(option, this.isChecked());
			
			updateEnabled();
		}
		
		@Override
		public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
			super.renderButton(stack, mouseX, mouseY, partialTick);
			
			if (this.hovered) {
				String tooltipText = tooltip.getString();
				ArrayList<Text> lines = new ArrayList<>();

				for (String line : tooltipText.split("\n"))
					lines.add(Text.literal(line));


				renderTooltip(stack, lines, Optional.empty(), mouseX, mouseY);
			}
		}
	}

	public void addCheckbox(String option) {
		String text = Text.translatable(this.customization.getName(option)).getString();
		String desc = Text.translatable(this.customization.getDescription(option)).getString();
		GuiCheckbox checkbox = new GuiCheckbox(10 + this.guiLeft, posy + this.guiTop, this.xSize - 20, 20, Text.literal(text), customization.getBoolean(option), option, Text.literal(desc));
		posy += 22;
		this.addDrawableChild(checkbox);
		options.put(checkbox, option);
	}
		
	class GuiSlider extends SliderWidget {
		double min, max, val;
		String text, option;
		public Text tooltip;
		public GuiSlider(int x, int y, int w, int h, Text text, double min, double max, double val, String option, Text tooltip) {
			super(x, y, w, h, text, (val - min) / (max - min));
			this.min = min;
			this.max = max;
			this.val = val;
			this.text = text.getString();
			this.option = option;
			this.tooltip = tooltip;
			
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage(Text.literal(text + ": " + String.format("%.1f", this.val)));
		}

		@Override
		protected void applyValue() {
			this.val = (this.value * (this.max - this.min)) + this.min;
//			d = Math.floor(d * 10 + 0.5) / 10;
			customization.setDouble(option, this.val);
		}
		
		@Override
		public void renderButton(MatrixStack stack, int mouseX, int mouseY, float partialTick) {
			super.renderButton(stack, mouseX, mouseY, partialTick);
			
			if (this.hovered) {
				String tooltiptext = tooltip.getString();
				ArrayList<Text> lines = new ArrayList<>();
				for (String line : tooltiptext.split("\n")) {
					lines.add(Text.literal(line));
				}
				renderTooltip(stack, lines, Optional.empty(), mouseX, mouseY);
			}
		}
	}
	
	public void addSlider(String option) {
		double d = customization.getDouble(option);
		d = Math.floor(d * 10 + 0.5) / 10;
		
		double max = GrappleCustomization.getMaxFromConfig(option, this.getLimits());
		double min = GrappleCustomization.getMinFromConfig(option, this.getLimits());
		
		String text = Text.translatable(this.customization.getName(option)).getString();
		String desc = Text.translatable(this.customization.getDescription(option)).getString();
		GuiSlider slider = new GuiSlider(10 + this.guiLeft, posy + this.guiTop, this.xSize - 20, 20, Text.literal(text), min, max, d, option, Text.literal(desc));
		
		posy += 22;
		this.addDrawableChild(slider);
		options.put(slider, option);
	}

	public void showCategoryScreen(GrappleCustomization.UpgradeCategories category) {
		clearScreen();

		this.addDrawableChild(
				ButtonWidget.builder(
								Text.translatable("grapplemodifier.back.desc"),
								new PressBack())
						.dimensions(this.guiLeft + 10, this.guiTop + this.ySize - 20 - 10, 50, 20)
						.build()
		);

		this.category = category;

		if (category == GrappleCustomization.UpgradeCategories.ROPE) {
			addSlider("maxlen");
			addCheckbox("phaserope");
			addCheckbox("sticky");
		} else if (category == GrappleCustomization.UpgradeCategories.THROW) {
			addSlider("hookgravity");
			addSlider("throwspeed");
			addCheckbox("reelin");
			addSlider("verticalthrowangle");
			addSlider("sneakingverticalthrowangle");
			addCheckbox("detachonkeyrelease");
		} else if (category == GrappleCustomization.UpgradeCategories.MOTOR) {
			addCheckbox("motor");
			addSlider("motormaxspeed");
			addSlider("motoracceleration");
			addCheckbox("motorwhencrouching");
			addCheckbox("motorwhennotcrouching");
			addCheckbox("smartmotor");
			addCheckbox("motordampener");
			addCheckbox("pullbackwards");
		} else if (category == GrappleCustomization.UpgradeCategories.SWING) {
			addSlider("playermovementmult");
		} else if (category == GrappleCustomization.UpgradeCategories.STAFF) {
			addCheckbox("enderstaff");
		} else if (category == GrappleCustomization.UpgradeCategories.FORCEFIELD) {
			addCheckbox("repel");
			addSlider("repelforce");
		} else if (category == GrappleCustomization.UpgradeCategories.MAGNET) {
			addCheckbox("attract");
			addSlider("attractradius");
		} else if (category == GrappleCustomization.UpgradeCategories.DOUBLE) {
			addCheckbox("doublehook");
			addCheckbox("smartdoublemotor");
			addSlider("angle");
			addSlider("sneakingangle");
			addCheckbox("oneropepull");
		} else if (category == GrappleCustomization.UpgradeCategories.ROCKET) {
			addCheckbox("rocket");
			addSlider("rocket_force");
			addSlider("rocket_active_time");
			addSlider("rocket_refuel_ratio");
			addSlider("rocket_vertical_angle");
		}
		
		this.updateEnabled();
	}
	
	@Override
	public void close() {
		this.tile.setCustomizationClient(customization);
		super.close();
	}
	
	public void updateEnabled() {
		for (ClickableWidget b : this.options.keySet()) {
			String option = this.options.get(b);
			boolean enabled = true;
			
			String desc = Text.translatable(this.customization.getDescription(option)).getString();
			
			if (!this.customization.isOptionValid(option)) {
				desc = Text.translatable("grapplemodifier.incompatability.desc").getString() + "\n" + desc;
				enabled = false;
			}
			
			int level = GrappleCustomization.optionEnabledInConfig(option);
			if (this.getLimits() < level) {
				if (level == 1) {
					desc = Text.translatable("grapplemodifier.limits.desc").getString() + "\n" + desc;
				} else {
					desc = Text.translatable("grapplemodifier.locked.desc").getString() + "\n" + desc;
				}
				enabled = false;
			}
			
			b.active = enabled;

			if (b instanceof GuiSlider) {
				((GuiSlider) b).tooltip = Text.literal(desc);
				b.setAlpha(enabled ? 1.0F : 0.5F);
			}
			if (b instanceof GuiCheckbox) {
				((GuiCheckbox) b).tooltip = Text.literal(desc);
				b.setAlpha(enabled ? 1.0F : 0.5F);
			}
		}
	}
	
	public int getLimits() {
		if (this.tile.isUnlocked(GrappleCustomization.UpgradeCategories.LIMITS) || MinecraftClient.getInstance().player.isCreative()) {
			return 1;
		}
		return 0;
	}
}
