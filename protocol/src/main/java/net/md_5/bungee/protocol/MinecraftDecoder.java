package net.md_5.bungee.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class MinecraftDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Setter
    private Protocol protocol;
    private final boolean server;
    @Getter
    @Setter
    private int protocolVersion;
    private boolean broken;

    public MinecraftDecoder(Protocol protocol, boolean server, int protocolVersion)
    {
        this.protocol = protocol;
        this.server = server;
        this.protocolVersion = protocolVersion;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        if ( broken )
        {
            return;
        }

        Protocol.DirectionData prot = getDirectionData();
        ByteBuf slice = in.copy(); // Can't slice this one due to EntityMap :(

        try
        {
            int packetId = DefinedPacket.readVarInt( in );

            DefinedPacket packet = prot.createPacket( packetId, protocolVersion );
            if ( packet != null )
            {
                packet.read( in, prot.getDirection(), protocolVersion );

                if ( in.isReadable() )
                {
                    throw new BadPacketException( "Did not read all bytes from packet " + packet.getClass() + " " + packetId + " Protocol " + protocol + " Direction " + prot.getDirection() );
                }
            } else
            {
                in.skipBytes( in.readableBytes() );
            }

            out.add( new PacketWrapper( packet, slice, false ) );
            slice = null;
        } catch ( Exception ex )
        {
            broken = true;
            ctx.channel().config().setAutoRead( false );
            NettyReflection.invokeExceptionFromRead( ctx, ex );
        } finally
        {
            if ( slice != null )
            {
                slice.release();
            }
        }
    }

    public Protocol.DirectionData getDirectionData()
    {
        return ( server ) ? protocol.TO_SERVER : protocol.TO_CLIENT;
    }
}
