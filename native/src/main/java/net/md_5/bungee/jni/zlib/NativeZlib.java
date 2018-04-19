package net.md_5.bungee.jni.zlib;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import java.util.zip.DataFormatException;
import lombok.Getter;

public class NativeZlib implements BungeeZlib
{

    @Getter
    private final NativeCompressImpl nativeCompress = new NativeCompressImpl();
    /*============================================================================*/
    private boolean compress;
    private long ctx;

    @Override
    public void init(boolean compress, int level)
    {
        free();

        this.compress = compress;
        this.ctx = nativeCompress.init( compress, level );
    }

    @Override
    public void free()
    {
        if ( ctx != 0 )
        {
            nativeCompress.end( ctx, compress );
            ctx = 0;
        }

        nativeCompress.consumed = 0;
        nativeCompress.finished = false;
    }

    @Override
    public void process(ByteBuf in, ByteBuf out) throws DataFormatException
    {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        Preconditions.checkState( ctx != 0, "Invalid pointer to compress!" );

        while ( !nativeCompress.finished && ( compress || in.isReadable() ) )
        {
            out.ensureWritable( 8192 );

            int processed = nativeCompress.process( ctx, in.memoryAddress() + in.readerIndex(), in.readableBytes(), out.memoryAddress() + out.writerIndex(), out.writableBytes(), compress );

            in.readerIndex( in.readerIndex() + nativeCompress.consumed );
            out.writerIndex( out.writerIndex() + processed );
        }

        nativeCompress.reset( ctx, compress );
        nativeCompress.consumed = 0;
        nativeCompress.finished = false;
    }

    @Override
    public void process(ByteBuf in, ByteBuf out, int length) throws DataFormatException
    {
        // Smoke tests
        in.memoryAddress();
        out.memoryAddress();
        Preconditions.checkState( ctx != 0, "Invalid pointer to compress!" );
        int finished = 0;

        while ( !nativeCompress.finished && ( compress || in.isReadable() && finished < length ) )
        {
            int remaining = Math.max( 8192, length - finished );
            out.ensureWritable( remaining );

            int processed = nativeCompress.process( ctx, in.memoryAddress() + in.readerIndex(), in.readableBytes(), out.memoryAddress() + out.writerIndex(), remaining, compress );
            finished += processed;
            
            in.readerIndex( in.readerIndex() + nativeCompress.consumed );
            out.writerIndex( out.writerIndex() + processed );
        }

        nativeCompress.reset( ctx, compress );
        nativeCompress.consumed = 0;
        nativeCompress.finished = false;
    }
}
