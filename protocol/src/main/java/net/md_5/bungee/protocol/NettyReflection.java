package net.md_5.bungee.protocol;

import io.netty.channel.ChannelHandlerContext;
import java.lang.reflect.Method;

public class NettyReflection
{

    private static final Method INVOKE_EXCEPTION_CAUSE;

    static
    {
        Method temp = null;
        try
        {
            Class<?> c = Class.forName( "io.netty.channel.AbstractChannelHandlerContext" );
            temp = c.getDeclaredMethod( "invokeExceptionCaught", Throwable.class );
            temp.setAccessible( true );
        } catch ( Exception e )
        {
            e.printStackTrace();
            System.exit( 0 );
        }
        INVOKE_EXCEPTION_CAUSE = temp;
    }

    public static void invokeExceptionFromRead(ChannelHandlerContext ctx, Throwable ex) throws Exception
    {
        INVOKE_EXCEPTION_CAUSE.invoke( ctx, ex );
    }
}
