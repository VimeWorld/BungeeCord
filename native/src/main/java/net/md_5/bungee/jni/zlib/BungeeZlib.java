package net.md_5.bungee.jni.zlib;

import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import java.util.zip.DataFormatException;

public interface BungeeZlib
{

    void init(boolean compress, int level);

    void free();

    void process(ByteBuf in, ByteBuf out) throws DataFormatException;

    void process(ByteBuf in, ByteBuf out, Function<ByteBuf, Boolean> firstReadCallback) throws DataFormatException;
}
