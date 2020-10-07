package org.dimdev.vanillafix.bugs.mixins.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

@Mixin(EnumConnectionState.class)
public final class MixinEnumConnectionState {

    private final Int2ObjectMap<Supplier<? extends Packet<?>>> serverbound = new Int2ObjectArrayMap<>();
    private final Int2ObjectMap<Supplier<? extends Packet<?>>> clientbound = new Int2ObjectArrayMap<>();
    private int serverboundId = 0, clientboundId = 0;

    /**
     * @reason Makes packet construction faster
     */
    @Overwrite
    protected EnumConnectionState registerPacket(EnumPacketDirection direction, Class<? extends Packet<?>> packetClass) {
        Int2ObjectMap<Supplier<? extends Packet<?>>> suppliers = direction == EnumPacketDirection.SERVERBOUND ? serverbound : clientbound;
        Constructor<? extends Packet<?>> constructor;
        try {
            constructor = packetClass.getConstructor();
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
        constructor.setAccessible(true);
        suppliers.put(direction == EnumPacketDirection.SERVERBOUND ? serverboundId++ : clientboundId++, () -> {
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        });
        return (EnumConnectionState) (Object) this;
    }

    /**
     * @reason Makes packet construction faster
     */
    @Nullable
    @Overwrite
    public Packet<?> getPacket(EnumPacketDirection direction, int packetId) {
        Supplier<? extends Packet<?>> packet = (direction == EnumPacketDirection.SERVERBOUND ? serverbound : clientbound).get(packetId);
        return packet == null ? null : packet.get();
    }
}
