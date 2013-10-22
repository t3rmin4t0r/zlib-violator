package org.notmysock.interposer;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.zip.*;

public class ZlibViolatorTest {
	public static void main(String[] args) throws Exception {
		ByteBuffer src = ByteBuffer.allocateDirect(4096);
		ByteBuffer dst = ByteBuffer.allocateDirect(4096);
		src.clear();
		src.put(compress(sampleText.getBytes()));
		src.flip();
		dst.clear();
		System.err.println("Is native zlib available? = " + ZlibViolator.isEnabled());
		if(ZlibViolator.isEnabled()) {
			ZlibViolator zz = new ZlibViolator();
			int n = zz.decompress(dst, src);
			System.err.println("We got = " + n);
			System.err.println("dst = " + dst);
			System.err.println("finished = " + zz.finished());
			while(dst.remaining() > 0) {
				int c = dst.get() & 0xFF;
				System.out.printf("%c",c);
			}
		}
	}   

	private static final String sampleText = ""
			+ "It is very strange, this domination of our intellect by our digestive "
			+ "organs.  We cannot work, we cannot think, unless our stomach wills so."
			+ "It dictates to us our emotions, our passions.\n"
			+ "We are but the veriest, sorriest slaves of our stomach.  Reach not after "
			+ "morality and righteousness, my friends; watch vigilantly your stomach, "
			+ "and diet it with care and judgment.  Then virtue and contentment will "
			+ "come and reign within your heart, unsought by any effort of your own; and "
			+ "you will be a good citizen, a loving husband, and a tender father - a "
			+ "noble, pious man.\n";

	private static byte[] compress(byte[] buffer) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				buffer.length + 12);
		DeflaterOutputStream dos = new DeflaterOutputStream(baos);
		dos.write(buffer);
		dos.flush();
		dos.close();
		return baos.toByteArray();
	}

}