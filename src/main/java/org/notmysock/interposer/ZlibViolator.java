package org.notmysock.interposer;

import java.io.IOException;
import java.lang.reflect.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import org.apache.hadoop.io.compress.zlib.ZlibDecompressor;

public final class ZlibViolator extends ZlibDecompressor {
	private static Field compressedDirectBuf;
	private static Field compressedDirectBufOff;
	private static Field compressedDirectBufLen;
	private static Field uncompressedDirectBuf;
	private static Field directBufferSize;
	private static Field nativeZlibLoaded;
	private static Method inflateBytesDirect; 
	private static boolean enabled = true;
	
	static {
		try {
			compressedDirectBuf = ZlibDecompressor.class.getDeclaredField("compressedDirectBuf");
			compressedDirectBufOff = ZlibDecompressor.class.getDeclaredField("compressedDirectBufOff");
			compressedDirectBufLen = ZlibDecompressor.class.getDeclaredField("compressedDirectBufLen");
			uncompressedDirectBuf = ZlibDecompressor.class.getDeclaredField("uncompressedDirectBuf");
			directBufferSize = ZlibDecompressor.class.getDeclaredField("directBufferSize");
			nativeZlibLoaded = ZlibDecompressor.class.getDeclaredField("nativeZlibLoaded");
			compressedDirectBuf.setAccessible(true);
			compressedDirectBufOff.setAccessible(true);
			compressedDirectBufLen.setAccessible(true);
			uncompressedDirectBuf.setAccessible(true);
			directBufferSize.setAccessible(true);
			nativeZlibLoaded.setAccessible(true);
		} catch(NoSuchFieldException nse) {
			enabled = false;
		}
		
		try {
			inflateBytesDirect = ZlibDecompressor.class.getDeclaredMethod("inflateBytesDirect");
			inflateBytesDirect.setAccessible(true);
		} catch(NoSuchMethodException nsme) {
			enabled = false;
		}
		
		try {
			if(!nativeZlibLoaded.getBoolean(null)) {
				enabled = false;
			}
		} catch(IllegalAccessException err) {
			enabled = false;
		}
	}
	
	public static boolean isEnabled() {
		return enabled;
	}
	
	
	public int decompress(ByteBuffer dst, ByteBuffer src) throws IOException {
		assert dst.isDirect();
		assert src.isDirect();
		assert dst.remaining() > 0;
		ByteBuffer presliced = null;
		if(dst.position() > 0) {
			presliced = dst;
			dst = dst.slice();
		}
		try {
			Object originalCompressed = compressedDirectBuf.get(this);
			Object originalUncompressed = uncompressedDirectBuf.get(this);
			int originalBufferSize = directBufferSize.getInt(this);
			compressedDirectBuf.set(this, src);
			compressedDirectBufOff.set(this, src.position());
			compressedDirectBufLen.set(this, src.remaining());
			uncompressedDirectBuf.set(this, dst);
			directBufferSize.set(this, dst.remaining());
			Integer n = (Integer)inflateBytesDirect.invoke(this, new Object[0]);
			dst.limit(n.intValue());
			if(presliced != null) {
				presliced.limit(presliced.position()+n.intValue());
			}
		
			if(compressedDirectBufLen.getInt(this) > 0) {
				src.position(compressedDirectBufOff.getInt(this));
			} else {
				src.position(src.limit());
			}
			compressedDirectBuf.set(this, originalCompressed);
			uncompressedDirectBuf.set(this, originalUncompressed);
			compressedDirectBufOff.setInt(this, 0);
			compressedDirectBufLen.setInt(this, 0);
			directBufferSize.setInt(this, originalBufferSize);
			return n.intValue();			
		} catch(IllegalAccessException ie) {
			throw new IOException(ie);
		} catch(InvocationTargetException ite) {
			throw new IOException(ite);
		}
	}
	/*
	 * 
   public int decompress(ByteBuffer dst, ByteBuffer src) throws IOException {
    assert dst.remaining() > 0 : "dst.remaining == 0";
    int n = 0;
    
    if((src != null && src.isDirect()) && dst.isDirect() && userBuf == null) {     
      boolean cleanDst = (dst.position() == 0 && dst.remaining() == dst.capacity() && dst.remaining() >= directBufferSize);
      boolean cleanState = (compressedDirectBufLen == 0 && uncompressedDirectBuf.remaining() == 0);
      if(cleanDst && cleanState) {
        Buffer originalCompressed = compressedDirectBuf;
        Buffer originalUncompressed = uncompressedDirectBuf;
        int originalBufferSize = directBufferSize;
        compressedDirectBuf = src;
        compressedDirectBufOff = src.position();
        compressedDirectBufLen = src.remaining();
        uncompressedDirectBuf = dst;
        directBufferSize = dst.remaining();
        // Compress data
        n = inflateBytesDirect();
        dst.position(n);
        if(compressedDirectBufLen > 0) {
          src.position(compressedDirectBufOff);
        } else {
          src.position(src.limit());
        }
        compressedDirectBuf = originalCompressed;
        uncompressedDirectBuf = originalUncompressed;        
        compressedDirectBufOff = 0;
        compressedDirectBufLen = 0;
        directBufferSize = originalBufferSize;
        return n;
      }
    }  
  }*/	
}
