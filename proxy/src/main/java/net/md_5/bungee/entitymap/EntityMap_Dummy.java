package net.md_5.bungee.entitymap;

import io.netty.buffer.ByteBuf;

public class EntityMap_Dummy extends EntityMap
{

    public static final EntityMap_Dummy INSTANCE = new EntityMap_Dummy();

    EntityMap_Dummy()
    {
    }

    @Override
    public void rewriteServerbound(ByteBuf packet, int oldId, int newId)
    {
    }

    @Override
    public void rewriteServerbound(ByteBuf packet, int oldId, int newId, int protocolVersion)
    {
    }

    @Override
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId)
    {
    }

    @Override
    public void rewriteClientbound(ByteBuf packet, int oldId, int newId, int protocolVersion)
    {
    }
}
