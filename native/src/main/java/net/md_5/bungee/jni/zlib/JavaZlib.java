package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class JavaZlib implements BungeeZlib
{

    private final byte[] buffer = new byte[ 8192 ];
    //
    private boolean compress;
    private Deflater deflater;
    private Inflater inflater;

    @Override
    public void init(boolean compress, int level)
    {
        this.compress = compress;
        free();

        if ( compress )
        {
            deflater = new Deflater( level );
        } else
        {
            inflater = new Inflater();
        }
    }

    @Override
    public void free()
    {
        if ( deflater != null )
        {
            deflater.end();
        }
        if ( inflater != null )
        {
            inflater.end();
        }
    }

    @Override
    public void process(ByteBuf in, ByteBuf out) throws DataFormatException
    {
        process( in, out, null );
    }

    @Override
    public void process(ByteBuf in, ByteBuf out, Function<ByteBuf, Boolean> firstReadCallback) throws DataFormatException
    {
        if ( compress )
        {
            byte[] inData = new byte[ in.readableBytes() ];
            in.readBytes( inData );
            deflater.setInput( inData );
            deflater.finish();

            while ( !deflater.finished() )
            {
                int count = deflater.deflate( buffer );
                out.writeBytes( buffer, 0, count );
            }

            deflater.reset();
        } else
        {
            int totalLength = in.readableBytes();
            byte[] inData = new byte[ Math.min( totalLength, 8192 ) ];
            int count;

            while ( !inflater.finished() && inflater.getTotalIn() < totalLength )
            {
                if ( inflater.needsInput() )
                {
                    count = Math.min( in.readableBytes(), 8192 );
                    in.readBytes( inData, 0, Math.min( in.readableBytes(), 8192 ) );
                    inflater.setInput( inData, 0, count );
                }

                count = inflater.inflate( buffer );
                out.writeBytes( buffer, 0, count );

                if ( firstReadCallback != null )
                {
                    if ( !firstReadCallback.apply( out ) )
                    {
                        break;
                    }
                    firstReadCallback = null;
                }
            }

            inflater.reset();
        }
    }
}
