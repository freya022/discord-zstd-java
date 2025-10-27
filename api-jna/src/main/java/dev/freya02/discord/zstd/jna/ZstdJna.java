package dev.freya02.discord.zstd.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.nio.ByteBuffer;

public interface ZstdJna extends Library
{
    ZstdJna INSTANCE = Native.load(null, ZstdJna.class);

    Pointer ZSTD_createDStream();

    long ZSTD_freeDStream(Pointer zds);

    long ZSTD_initDStream(Pointer zds);

    long ZSTD_decompressStream(Pointer zds, ZSTD_outBuffer output, ZSTD_inBuffer input);

    int ZSTD_isError(long result);

    String ZSTD_getErrorName(long result);

    @Structure.FieldOrder({"dst", "size", "pos"})
    class ZSTD_outBuffer extends Structure
    {
        public ByteBuffer dst;
        public long size;
        public long pos;

        public ZSTD_outBuffer(int size)
        {
            this.dst = ByteBuffer.allocateDirect(size);
            this.size = size;
            this.pos = 0;

            allocateMemory();
        }
    }

    @Structure.FieldOrder({"src", "size", "pos"})
    class ZSTD_inBuffer extends Structure
    {
        public ByteBuffer src;
        public long size;
        public long pos;

        public ZSTD_inBuffer(byte[] data)
        {
            this.src = ByteBuffer.allocateDirect(data.length);
            src.put(data);
            src.position(0);
            this.size = data.length;
            this.pos = 0;

            allocateMemory();
        }
    }
}
