package me.alikomi.endminecraft.utils;

import java.util.Random;

public class Util {
	public static void log(Object msg) {
		System.out.println(msg);
	}

	public static void log(Object... msg) {
		for (Object o : msg) {
			System.out.println(o);
		}
	}

	public static String getRandomString(int length) {
		String str = "_abcde_fghijk_lmnopqrst_uvw_xyzABCD_EFGHIJKLM_NOPQRSTUVWXY_Z012345_6789_";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; ++i) {
			int number = random.nextInt(72);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}

	private static byte[] subBytes(byte[] src, int begin, int count) {
		byte[] bs = new byte[count];
		System.arraycopy(src, begin, bs, 0, count);
		return bs;
	}

	public static byte[] fb(byte[] old) {
		if (old.length <= 0) {
			log("1");
			return old;
		}
		for (int i = old.length - 1; i >= 0; i--) {
			if (old[i] != (byte) 0) {
				return subBytes(old, 0, ++i);
			}
		}
		return old;
	}

	@SuppressWarnings("unchecked")
	protected static <T> T getCo(String date, T def) {
		if (date.equals("")) {
			return def;
		}
		return (T) date;
	}

	protected static int getCo(String date, int def) {
		if (date.equals("")) {
			return def;
		}
		return Integer.parseInt(date);
	}
}
