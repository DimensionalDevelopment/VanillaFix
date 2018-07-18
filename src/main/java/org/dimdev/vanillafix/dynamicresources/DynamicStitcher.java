package org.dimdev.vanillafix.dynamicresources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.Stitcher.Holder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Set;

public class DynamicStitcher {
    private final int mipmapLevelStitcher;
    private final Set<Holder> setStitchHolders = Sets.newHashSetWithExpectedSize(256);
    private final List<Slot> stitchSlots = Lists.newArrayListWithCapacity(256);
    private int currentWidth;
    private int currentHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxTileDimension;

    public DynamicStitcher(int maxWidthIn, int maxHeightIn, int maxTileDimensionIn, int mipmapLevelStitcherIn) {
        mipmapLevelStitcher = mipmapLevelStitcherIn;
        maxWidth = maxWidthIn;
        maxHeight = maxHeightIn;
        maxTileDimension = maxTileDimensionIn;

        currentWidth = 512;
        currentHeight = 512;
        stitchSlots.add(new Slot(0, 0, currentWidth, currentHeight));
    }

    public int getCurrentWidth() {
        return currentWidth;
    }

    public int getCurrentHeight() {
        return currentHeight;
    }

    public void addSprite(TextureAtlasSprite sprite) {
        Holder holder = new Holder(sprite, mipmapLevelStitcher);

        if (maxTileDimension > 0) {
            holder.setNewDimension(maxTileDimension);
        }

        setStitchHolders.add(holder);

        Slot slot = allocateSlot(holder);

        if (slot == null) {
            throw new StitcherException(null, String.format("Unable to fit: %s - size: %dx%d", holder.getAtlasSprite().getIconName(), holder.getAtlasSprite().getIconWidth(), holder.getAtlasSprite().getIconHeight()));
        }

        currentWidth = MathHelper.smallestEncompassingPowerOfTwo(currentWidth);
        currentHeight = MathHelper.smallestEncompassingPowerOfTwo(currentHeight);

        sprite.initSprite(currentWidth, currentHeight, slot.getOriginX(), slot.getOriginY(), holder.isRotated());
    }

    public List<TextureAtlasSprite> getStichSlots() {
        List<Slot> slots = Lists.newArrayList();

        for (Slot stitchSlot : stitchSlots) {
            stitchSlot.getAllStitchSlots(slots);
        }

        List<TextureAtlasSprite> sprites = Lists.newArrayList();

        for (Slot slot : slots) {
            Holder holder = slot.getStitchHolder();
            TextureAtlasSprite sprite = holder.getAtlasSprite();
            sprite.initSprite(currentWidth, currentHeight, slot.getOriginX(), slot.getOriginY(), holder.isRotated());
            sprites.add(sprite);
        }

        return sprites;
    }

    private Slot allocateSlot(Holder holder) {
        TextureAtlasSprite sprite = holder.getAtlasSprite();
        boolean notSquare = sprite.getIconWidth() != sprite.getIconHeight();

        for (Slot stitchSlot : stitchSlots) {
            Slot slot = stitchSlot.addSlot(holder);
            if (slot != null) {
                return slot;
            }

            if (notSquare) {
                holder.rotate();

                slot = stitchSlot.addSlot(holder);

                if (slot != null) {
                    return slot;
                }

                holder.rotate();
            }
        }

        // TODO: Fix this, expanding the atlas changes all previous UV coordinates
        return expandAndAllocateSlot(holder);
    }

    private Slot expandAndAllocateSlot(Holder holder) {
        int i = Math.min(holder.getWidth(), holder.getHeight());
        int k = MathHelper.smallestEncompassingPowerOfTwo(currentWidth);
        int l = MathHelper.smallestEncompassingPowerOfTwo(currentHeight);
        int i1 = MathHelper.smallestEncompassingPowerOfTwo(currentWidth + i);
        int j1 = MathHelper.smallestEncompassingPowerOfTwo(currentHeight + i);
        boolean flag1 = i1 <= maxWidth;
        boolean flag2 = j1 <= maxHeight;

        if (!flag1 && !flag2) {
            return null;
        } else {
            boolean flag3 = flag1 && k != i1;
            boolean flag4 = flag2 && l != j1;
            boolean flag;

            if (flag3 ^ flag4) {
                flag = !flag3 && flag1; //Forge: Fix DynamicStitcher not expanding entire height before growing width, and {potentially} growing larger then the max size.
            } else {
                flag = flag1 && k <= l;
            }

            Slot newSlot;

            if (flag) {
                if (holder.getWidth() > holder.getHeight()) {
                    holder.rotate();
                }

                if (currentHeight == 0) {
                    currentHeight = holder.getHeight();
                }

                if (currentWidth == 0) {
                    currentWidth = holder.getWidth();
                }

                newSlot = new Slot(currentWidth, 0, holder.getWidth(), currentHeight);
                currentWidth += holder.getWidth();
            } else {
                newSlot = new Slot(0, currentHeight, currentWidth, holder.getHeight());
                currentHeight += holder.getHeight();
            }

            Slot slot = newSlot.addSlot(holder);
            stitchSlots.add(newSlot);

            return slot;
        }
    }

    public static class Slot {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Slot> subSlots;
        private Holder holder;

        public Slot(int originXIn, int originYIn, int widthIn, int heightIn) {
            originX = originXIn;
            originY = originYIn;
            width = widthIn;
            height = heightIn;
        }

        public Holder getStitchHolder() {
            return holder;
        }

        public int getOriginX() {
            return originX;
        }

        public int getOriginY() {
            return originY;
        }

        public Slot addSlot(Holder holder) {
            if (this.holder != null) {
                return null;
            } else {
                int spriteWidth = holder.getWidth();
                int spriteHeight = holder.getHeight();

                if (spriteWidth > width || spriteHeight > height) {
                    return null;
                }

                if (spriteWidth == width && spriteHeight == height) {
                    this.holder = holder;
                    return this;
                }

                if (subSlots == null) {
                    subSlots = Lists.newArrayListWithCapacity(1);
                    subSlots.add(new Slot(originX, originY, spriteWidth, spriteHeight));

                    int widthRemaining = width - spriteWidth;
                    int heightRemaining = height - spriteHeight;

                    if (heightRemaining > 0 && widthRemaining > 0) {
                        int largestSideRight = Math.max(height, widthRemaining);
                        int largestSideLeft = Math.max(width, heightRemaining);

                        if (largestSideRight >= largestSideLeft) {
                            subSlots.add(new Slot(originX, originY + spriteHeight, spriteWidth, heightRemaining));
                            subSlots.add(new Slot(originX + spriteWidth, originY, widthRemaining, height));
                        } else {
                            subSlots.add(new Slot(originX + spriteWidth, originY, widthRemaining, spriteHeight));
                            subSlots.add(new Slot(originX, originY + spriteHeight, width, heightRemaining));
                        }
                    } else if (widthRemaining == 0) {
                        subSlots.add(new Slot(originX, originY + spriteHeight, spriteWidth, heightRemaining));
                    } else if (heightRemaining == 0) {
                        subSlots.add(new Slot(originX + spriteWidth, originY, widthRemaining, spriteHeight));
                    }
                }

                for (Slot subSlots : subSlots) {
                    Slot slot = subSlots.addSlot(holder);
                    if (slot != null) {
                        return slot;
                    }
                }

                return null;
            }
        }

        public void getAllStitchSlots(List<Slot> target) {
            if (holder != null) {
                target.add(this);
            } else if (subSlots != null) {
                for (Slot stitcher$slot : subSlots) {
                    stitcher$slot.getAllStitchSlots(target);
                }
            }
        }

        public String toString() {
            return "Slot{originX=" + originX + ", originY=" + originY + ", width=" + width + ", height=" + height + ", texture=" + holder + ", subSlots=" + subSlots + '}';
        }
    }
}
