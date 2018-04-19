package net.md_5.bungee.compress;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import net.md_5.bungee.jni.zlib.BungeeZlib;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.MinecraftDecoder;
import net.md_5.bungee.protocol.PacketWrapper;

public class PacketDecompressor extends MessageToMessageDecoder<ByteBuf>
{

    private final BungeeZlib zlib = CompressFactory.zlib.newInstance();
    private MinecraftDecoder decoder;

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        zlib.init( false, 0 );
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        zlib.free();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        int size = DefinedPacket.readVarInt( in );
        if ( size == 0 )
        {
            out.add( in.slice().retain() );
            in.skipBytes( in.readableBytes() );
        } else
        {
            ByteBuf decompressed = ctx.alloc().directBuffer();

            try
            {
                if ( decoder == null )
                {
                    decoder = ctx.pipeline().get( MinecraftDecoder.class );
                }

                in.markReaderIndex();

                zlib.process( in, decompressed, (out0)
                        -> 
                        {
                            out0.markReaderIndex();
                            int id = DefinedPacket.readVarInt( out0 );
                            out0.readerIndex( 0 );
                            if ( !decoder.getDirectionData().isPacketDefined( id, decoder.getProtocolVersion() ) )
                            {
                                in.readerIndex( 0 );
                                out0.writerIndex( 0 );
                                out.add( new PacketWrapper( null, in.retain(), true ) );
                                return Boolean.FALSE;
                            }
                            return Boolean.TRUE;
                } );

                if ( decompressed.writerIndex() != 0 )
                {
                    Preconditions.checkState( decompressed.readableBytes() == size, "Decompressed packet size mismatch" );
                    out.add( decompressed );
                    decompressed = null;
                }
            } finally
            {
                if ( decompressed != null )
                {
                    decompressed.release();
                }
            }
        }
    }
}
