package org.dimdev.vanillafix.bugs.mixins;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.minecraft.network.NettyCompressionEncoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.zip.Deflater;

@Mixin(NettyCompressionEncoder.class)
public abstract class MixinNettyCompressionEncoder extends MessageToByteEncoder<ByteBuf> {

    @Shadow @Final private Deflater deflater;

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        this.deflater.end();
    }
}
