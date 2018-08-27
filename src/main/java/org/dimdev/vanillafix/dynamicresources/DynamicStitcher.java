package org.dimdev.vanillafix.dynamicresources;

import com.google.common.collect.Lists;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.renderer.texture.Stitcher.Holder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class DynamicStitcher {
    public static final int BASE_WIDTH = 32;
    public static final int BASE_HEIGHT = 32;

    private final int mipmapLevels;
    private final List<Slot> slots = Lists.newArrayListWithCapacity(256);
    private int currentWidth;
    private int currentHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final int maxSpriteSize;

    public DynamicStitcher(int maxWidth, int maxHeight, int maxSpriteSize, int mipmapLevels) {
        this.mipmapLevels = mipmapLevels;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.maxSpriteSize = maxSpriteSize;
    }

    public int getImageWidth() {
        return Math.max(BASE_WIDTH, MathHelper.smallestEncompassingPowerOfTwo(currentWidth));
    }

    public int getImageHeight() {
        return Math.max(BASE_HEIGHT, MathHelper.smallestEncompassingPowerOfTwo(currentHeight));
    }

    public void addSprite(TextureAtlasSprite sprite) {
        Holder holder = new Holder(sprite, mipmapLevels);

        if (maxSpriteSize > 0) {
            holder.setNewDimension(maxSpriteSize);
        }

        Slot slot = allocateSlot(holder);

        if (slot == null) {
            throw new StitcherException(null, String.format("Unable to fit %s (size %dx%d)", sprite.getIconName(), sprite.getIconWidth(), sprite.getIconHeight()));
        }

        sprite.initSprite(BASE_WIDTH, BASE_HEIGHT, slot.getOriginX(), slot.getOriginY(), holder.isRotated());
    }

    public List<TextureAtlasSprite> getAllSprites() {
        List<Slot> allSlots = Lists.newArrayList();

        for (Slot slot : slots) {
            slot.getAllStitchSlots(allSlots);
        }

        List<TextureAtlasSprite> sprites = Lists.newArrayList();

        for (Slot slot : allSlots) {
            sprites.add(slot.getStitchHolder().getAtlasSprite());
        }

        return sprites;
    }

    private Slot allocateSlot(Holder holder) {
        TextureAtlasSprite sprite = holder.getAtlasSprite();
        boolean notSquare = sprite.getIconWidth() != sprite.getIconHeight();

        for (Slot stitchSlot : slots) {
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

        return expandAndAllocateSlot(holder);
    }

    private Slot expandAndAllocateSlot(Holder holder) {
        int smallestDimension = Math.min(holder.getWidth(), holder.getHeight());

        int currentImageWidth = MathHelper.smallestEncompassingPowerOfTwo(currentWidth);
        int currentImageHeight = MathHelper.smallestEncompassingPowerOfTwo(currentHeight);

        int increasedImageWidth = MathHelper.smallestEncompassingPowerOfTwo(currentWidth + smallestDimension);
        int increasedImageHeight = MathHelper.smallestEncompassingPowerOfTwo(currentHeight + smallestDimension);

        boolean canExpandWidth = increasedImageWidth <= maxWidth;
        boolean canExpandHeight = increasedImageHeight <= maxHeight;

        if (!canExpandWidth && !canExpandHeight) {
            return null;
        }

        boolean imageWidthWouldIncrease = canExpandWidth && increasedImageWidth != currentImageWidth;
        boolean imageHeightWouldIncrease = canExpandHeight && increasedImageHeight != currentImageHeight;

        boolean expandWidth = imageWidthWouldIncrease == imageHeightWouldIncrease
                              ? currentImageWidth <= currentImageHeight
                              : !imageWidthWouldIncrease;

        expandWidth &= canExpandWidth;

        Slot newSlot;
        if (expandWidth) {
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
//            if (holder.getHeight() > holder.getWidth()) {
//                holder.rotate();
//            }

            newSlot = new Slot(0, currentHeight, currentWidth, holder.getHeight());
            currentHeight += holder.getHeight();
        }

        Slot slot = newSlot.addSlot(holder);
        slots.add(newSlot);

        return slot;
    }

    public static class Slot {
        private final int originX;
        private final int originY;
        private final int width;
        private final int height;
        private List<Slot> subSlots;
        private Holder holder;

        public Slot(int originX, int originY, int width, int height) {
            this.originX = originX;
            this.originY = originY;
            this.width = width;
            this.height = height;
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
                for (Slot slot : subSlots) {
                    slot.getAllStitchSlots(target);
                }
            }
        }

        public String toString() {
            return "Slot{originX=" + originX + ", originY=" + originY + ", width=" + width + ", height=" + height + ", texture=" + holder + ", subSlots=" + subSlots + '}';
        }
    }
}
