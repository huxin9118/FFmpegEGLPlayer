package com.example.ffmpegsdlplayer.media.tools;

public class H264Utils {

	public static int ffAvcFindStartcode(byte[] data, int offset, int end) {
		int out = ffAvcFindStartcodeInternal(data, offset, end);
		if (offset < out && out < end && data[out - 1] == 0)
			out--;
		return out;
	}

	private static int ffAvcFindStartcodeInternal(byte[] data, int offset, int end) {
		int a = offset + 4 - (offset & 3);

		for (end -= 3; offset < a && offset < end; offset++) {
			if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1)
				return offset;
		}

		for (end -= 3; offset < end; offset += 4) {
			int x = ((data[offset] << 8 | data[offset + 1]) << 8 | data[offset + 2]) << 8 | data[offset + 3];
//			System.out.println(Integer.toHexString(x));
			// if ((x - 0x01000100) & (~x) & 0x80008000) // little endian
			// if ((x - 0x00010001) & (~x) & 0x00800080) // big endian
			if (((x - 0x01010101) & (~x) & 0x80808080) != 0) { // generic
				if (data[offset + 1] == 0) {
					if (data[offset] == 0 && data[offset + 2] == 1)
						return offset;
					if (data[offset + 2] == 0 && data[offset + 3] == 1)
						return offset + 1;
				}
				if (data[offset + 3] == 0) {
					if (data[offset + 2] == 0 && data[offset + 4] == 1)
						return offset + 2;
					if (data[offset + 4] == 0 && data[offset + 5] == 1)
						return offset + 3;
				}
			}
		}

		for (end += 3; offset < end; offset++) {
			if (data[offset] == 0 && data[offset + 1] == 0 && data[offset + 2] == 1)
				return offset;
		}

		return end + 3;
	}
	
}
