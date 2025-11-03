package dev.freya02.discord.zstd.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

/**
 * Streaming decompression - HowTo
 *
 * <p>A {@code ZSTD_DStream} object is required to track streaming operations.
 * Use {@link #ZSTD_createDStream()} and {@link #ZSTD_freeDStream(MemorySegment)} to create/release resources.
 * {@code ZSTD_DStream} objects can be re-employed multiple times.
 *
 * <p>Use {@link #ZSTD_initDStream(MemorySegment)} to start a new decompression operation.
 * - return : recommended first input size
 *
 * <p>Alternatively, use advanced API to set specific properties.
 *
 * <p>Use {@link #ZSTD_decompressStream(MemorySegment, MemorySegment, MemorySegment)} repetitively to consume your input.
 * The function will update both {@code pos} fields.
 * If {@code input.pos < input.size}, some input has not been consumed.
 * It's up to the caller to present again remaining data.
 *
 * <p>The function tries to flush all data decoded immediately, respecting output buffer size.
 * If {@code output.pos < output.size}, decoder has flushed everything it could.
 *
 * <p>However, when {@code output.pos == output.size}, it's more difficult to know.
 * If @return > 0, the frame is not complete, meaning
 * either there is still some data left to flush within internal buffers,
 * or there is more input to read to complete the frame (or both).
 * In which case, call {@link #ZSTD_decompressStream(MemorySegment, MemorySegment, MemorySegment)} again to flush whatever remains in the buffer.
 * Note : with no additional input provided, amount of data flushed is necessarily &lt;= 131072.
 *  - return : 0 when a frame is completely decoded and fully flushed,
 *       or an error code, which can be tested using {@link #ZSTD_isError(long)},
 *       or any other value > 0, which means there is still some decoding or flushing to do to complete current frame :
 *                               the return value is a suggested next input size (just a hint for better latency)
 *                               that will never request more than the remaining content of the compressed frame.
 */
public final class Zstd {

    private Zstd() {
        // Should not be called directly
    }

    private static final boolean TRACE_DOWNCALLS = Boolean.getBoolean("jextract.trace.downcalls");

    private static void traceDowncall(String name, Object... args) {
         String traceArgs = Arrays.stream(args)
                       .map(Object::toString)
                       .collect(Collectors.joining(", "));
         System.out.printf("%s(%s)\n", name, traceArgs);
    }

    private static MemorySegment findOrThrow(String symbol) {
        return SYMBOL_LOOKUP.find(symbol)
            .orElseThrow(() -> new UnsatisfiedLinkError("unresolved symbol: " + symbol));
    }

    private static final SymbolLookup SYMBOL_LOOKUP = SymbolLookup.loaderLookup()
            .or(Linker.nativeLinker().defaultLookup());

    public static final ValueLayout.OfInt C_INT = ValueLayout.JAVA_INT;
    public static final ValueLayout.OfLong C_LONG_LONG = ValueLayout.JAVA_LONG;
    public static final AddressLayout C_POINTER = ValueLayout.ADDRESS
            .withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE));

    private static class ZSTD_getErrorString {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_POINTER,
            Zstd.C_INT
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_getErrorString");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * const char *ZSTD_getErrorString(ZSTD_ErrorCode code)
     * }
     */
    public static MemorySegment ZSTD_getErrorString(int code) {
        var mh$ = ZSTD_getErrorString.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_getErrorString", code);
            }
            return (MemorySegment)mh$.invokeExact(code);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_versionString {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_POINTER    );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_versionString");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * const char *ZSTD_versionString()
     * }
     */
    public static MemorySegment ZSTD_versionString() {
        var mh$ = ZSTD_versionString.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_versionString");
            }
            return (MemorySegment)mh$.invokeExact();
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_isError {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_INT,
            Zstd.C_LONG_LONG
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_isError");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * unsigned int ZSTD_isError(size_t result)
     * }
     */
    public static int ZSTD_isError(long result) {
        var mh$ = ZSTD_isError.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_isError", result);
            }
            return (int)mh$.invokeExact(result);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_getErrorName {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_POINTER,
            Zstd.C_LONG_LONG
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_getErrorName");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * const char *ZSTD_getErrorName(size_t result)
     * }
     */
    public static MemorySegment ZSTD_getErrorName(long result) {
        var mh$ = ZSTD_getErrorName.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_getErrorName", result);
            }
            return (MemorySegment)mh$.invokeExact(result);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_createDStream {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_POINTER    );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_createDStream");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * ZSTD_DStream *ZSTD_createDStream()
     * }
     */
    public static MemorySegment ZSTD_createDStream() {
        var mh$ = ZSTD_createDStream.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_createDStream");
            }
            return (MemorySegment)mh$.invokeExact();
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_freeDStream {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_LONG_LONG,
            Zstd.C_POINTER
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_freeDStream");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     * {@snippet lang=c :
     * size_t ZSTD_freeDStream(ZSTD_DStream *zds)
     * }
     */
    public static long ZSTD_freeDStream(MemorySegment zds) {
        var mh$ = ZSTD_freeDStream.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_freeDStream", zds);
            }
            return (long)mh$.invokeExact(zds);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_initDStream {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_LONG_LONG,
            Zstd.C_POINTER
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_initDStream");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     *  Initialize/reset DStream state for new decompression operation.
     *  Call before new decompression operation using same DStream.
     *
     *  <p>Note : This function is redundant with the advanced API and equivalent to:
     *  {@snippet lang=c:
     *  ZSTD_DCtx_reset(zds, ZSTD_reset_session_only);
     *  ZSTD_DCtx_refDDict(zds, NULL);
     *  }
     *
     *  <p>{@snippet lang = c:
     *   size_t ZSTD_initDStream(ZSTD_DStream *zds)
     *}
     */
    public static long ZSTD_initDStream(MemorySegment zds) {
        var mh$ = ZSTD_initDStream.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_initDStream", zds);
            }
            return (long)mh$.invokeExact(zds);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class ZSTD_decompressStream {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Zstd.C_LONG_LONG,
            Zstd.C_POINTER,
            Zstd.C_POINTER,
            Zstd.C_POINTER
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_decompressStream");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    private static class ZSTD_DStreamOutSize {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
                C_LONG_LONG
        );

        public static final MemorySegment ADDR = Zstd.findOrThrow("ZSTD_DStreamOutSize");

        public static final MethodHandle HANDLE = Linker.nativeLinker().downcallHandle(ADDR, DESC);
    }

    /**
     *  Streaming decompression function.
     *
     *  <p>Call repetitively to consume full input updating it as necessary.
     *
     *  <p>Function will update both input and output {@code pos} fields exposing current state via these fields:
     *  <ul>
     *      <li>
     *          {@code input.pos < input.size}, some input remaining and caller should provide remaining input
     *          on the next call.
     *      </li>
     *      <li>
     *          {@code output.pos < output.size}, decoder flushed internal output buffer.
     *      </li>
     *      <li>
     *          {@code output.pos == output.size}, unflushed data potentially present in the internal buffers,
     *          check {@code ZSTD_decompressStream()} @return value,
     *          if > 0, invoke it again to flush remaining data to output.
     *      </li>
     *  </ul>
     *
     *  <p>Note : with no additional input, amount of data flushed &lt;= 131072.
     *
     *  <p>Note: when an operation returns with an error code, the {@code zds} state may be left in undefined state.
     *  It's UB to invoke {@code ZSTD_decompressStream()} on such a state.
     *  In order to re-use such a state, it must be first reset,
     *  which can be done explicitly ({@code ZSTD_DCtx_reset()}),
     *  or is implied for operations starting some new decompression job ({@code ZSTD_initDStream}, {@code ZSTD_decompressDCtx()}, {@code ZSTD_decompress_usingDict()})
     *
     *  @return 0 when a frame is completely decoded and fully flushed,
     *          or an error code, which can be tested using {@link #ZSTD_isError(long)},
     *          or any other value > 0, which means there is some decoding or flushing to do to complete current frame.
     *
     *  <p>{@snippet lang=c :
     *   size_t ZSTD_decompressStream(ZSTD_DStream *zds, ZSTD_outBuffer *output, ZSTD_inBuffer *input)
     *   }
     */
    public static long ZSTD_decompressStream(MemorySegment zds, MemorySegment output, MemorySegment input) {
        var mh$ = ZSTD_decompressStream.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_decompressStream", zds, output, input);
            }
            return (long)mh$.invokeExact(zds, output, input);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    public static long ZSTD_DStreamOutSize() {
        var mh$ = ZSTD_DStreamOutSize.HANDLE;
        try {
            if (TRACE_DOWNCALLS) {
                traceDowncall("ZSTD_DStreamOutSize");
            }
            return (long)mh$.invokeExact();
        } catch (Throwable ex$) {
            throw new AssertionError("should not reach here", ex$);
        }
    }
}
