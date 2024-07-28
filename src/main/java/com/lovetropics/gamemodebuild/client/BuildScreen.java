package com.lovetropics.gamemodebuild.client;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import com.lovetropics.gamemodebuild.message.UpdateFilterMessage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.BitSet;

public class BuildScreen extends AbstractContainerScreen<BuildContainer> {

	private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(GamemodeBuild.MODID, "textures/gui/menu.png");

	private static final ResourceLocation TABS = ResourceLocation.withDefaultNamespace("textures/gui/container/creative_inventory/tab_items.png");
	private static final ResourceLocation SCROLLER = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");

	private EditBox searchField;

	private float scrollAmount;
	private boolean draggingScroll;

	private String lastSearchFilter = "";

	public BuildScreen(final BuildContainer screenContainer, final Inventory inv, final Component titleIn) {
		super(screenContainer, inv, titleIn);
		imageWidth = 195;
		imageHeight = 136;
	}

	@Override
	protected void init() {
		super.init();

        searchField = new EditBox(font, leftPos + 82, topPos + 6, 80, 9, Component.translatable("itemGroup.search"));
        searchField.setMaxLength(50);
        searchField.setBordered(false);
        searchField.setVisible(true);
        searchField.setTextColor(CommonColors.WHITE);
		searchField.setResponder(searchFilter -> {
			if (!searchFilter.equals(lastSearchFilter)) {
				updateSearch(searchFilter);
			}
			lastSearchFilter = searchFilter;
		});
        addRenderableWidget(searchField);
	}

	@Override
	public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
		if (searchField.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		} else if (searchField.isFocused() && searchField.isVisible() && keyCode != InputConstants.KEY_ESCAPE) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private void updateSearch(final String searchFilter) {
		final BitSet filteredSlots = menu.applyFilter(searchFilter);
		PacketDistributor.sendToServer(new UpdateFilterMessage(filteredSlots));
		updateScroll(scrollAmount); // Refresh scrollbar
	}

	@Override
	public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTicks) {
		renderBackground(graphics, mouseX, mouseY, partialTicks);
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderTooltip(graphics, mouseX, mouseY);
	}

	@Override
	protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
		graphics.drawString(font, title, 8, 6, 0x404040, false);
	}

	@Override
	protected void renderBg(final GuiGraphics graphics, final float partialTicks, final int mouseX, final int mouseY) {
		graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if (menu.canScroll()) {
			final Rect2i rect = scrollRect();
			graphics.blitSprite(SCROLLER, rect.left, rect.top, rect.width, rect.height);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (menu.canScroll()) {
			final int scrollHeight = menu.scrollHeight();
			updateScroll((float) (scrollAmount - scrollY / scrollHeight));
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseClicked(final double x, final double y, final int button) {
		if (button == InputConstants.MOUSE_BUTTON_LEFT) {
			final Rect2i rect = scrollRect();
			if (rect.contains(x, y)) {
				draggingScroll = true;
				return true;
			}
		}
		return super.mouseClicked(x, y, button);
	}

	@Override
	public boolean mouseReleased(final double x, final double y, final int button) {
		if (draggingScroll) {
			draggingScroll = false;
			return true;
		}
		return super.mouseReleased(x, y, button);
	}

	@Override
	public boolean mouseDragged(final double x, final double y, final int button, final double deltaX, final double deltaY) {
		if (draggingScroll) {
			final Rect2i area = scrollArea();
			final Rect2i rect = scrollRect();
			updateScroll((float) (y - area.top - rect.height / 2.0F) / (area.height - rect.height));
			return true;
		}
		return super.mouseDragged(x, y, button, deltaX, deltaY);
	}

	private void updateScroll(final float amount) {
		scrollAmount = Mth.clamp(amount, 0.0F, 1.0F);

		final int scrollOffset = Math.round(scrollAmount * menu.scrollHeight());
		menu.setScrollOffset(scrollOffset);
	}

	private Rect2i scrollRect() {
		final Rect2i area = scrollArea();

		final int scrollLength = (int) ((area.height - 17) * scrollAmount);
		return new Rect2i(area.left, area.top + scrollLength, area.width, 15);
	}

	private Rect2i scrollArea() {
		final int scrollLeft = leftPos + 175;
		final int scrollTop = topPos + 18;
		return new Rect2i(scrollLeft, scrollTop, 12, 112);
	}

    private record Rect2i(int left, int top, int width, int height) {
        public boolean contains(final double x, final double y) {
            return x >= left && y >= top && x <= left + width && y <= top + height;
        }
    }
}
