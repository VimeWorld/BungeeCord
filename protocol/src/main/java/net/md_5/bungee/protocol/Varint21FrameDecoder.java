package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import java.util.List;

public class Varint21FrameDecoder extends ByteToMessageDecoder
{

    private static boolean DIRECT_WARNING;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        in.markReaderIndex();

        for ( int i = 0; i < 3; i++ )
        {
            if ( !in.isReadable() )
            {
                in.resetReaderIndex();
                return;
            }

            byte b = in.readByte();
            if ( b >= 0 )
            {
                in.resetReaderIndex();
                int length = DefinedPacket.readVarInt( in );
                if ( length == 0 )
                {
                    throw new CorruptedFrameException( "Empty Packet!" );
                }

                if ( in.readableBytes() < length )
                {
                    in.resetReaderIndex();
                    return;
                } else
                {
                    if ( in.hasMemoryAddress() )
                    {
                        out.add( in.slice( in.readerIndex(), length ).retain() );
                        in.skipBytes( length );
                    } else
                    {
                        if ( !DIRECT_WARNING )
                        {
                            DIRECT_WARNING = true;
                            System.out.println( "Netty is not using direct IO buffers." );
                        }

                        // See https://github.com/SpigotMC/BungeeCord/issues/1717
                        ByteBuf dst = ctx.alloc().directBuffer( length );
                        in.readBytes( dst );
                        out.add( dst );
                    }
                    return;
                }
            }
        }

        throw new CorruptedFrameException( "length wider than 21-bit" );
    }
}
