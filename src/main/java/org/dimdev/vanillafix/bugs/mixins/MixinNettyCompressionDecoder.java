package org.dimdev.vanillafix.bugs.mixins;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.network.NettyCompressionDecoder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.zip.Inflater;

@Mixin(NettyCompressionDecoder.class)
public abstract class MixinNettyCompressionDecoder extends ByteToMessageDecoder {

    @Shadow @Final private Inflater inflater;

    @Override
    public void handlerRemoved0(ChannelHandlerContext ctx) throws Exception {
        this.inflater.end();
    }
}
