package ch.eth.jcd.badgers.vfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * $Id$
 * 
 * copied from: http://thomaswabner.wordpress.com/2007/10/09/fast-stream-copy-using-javanio-channels/
 * 
 * TODO describe ChannelTools
 * 
 */
public final class ChannelUtil {
	/**
	 * Copies input to output. Closes both streams a the end!
	 * 
	 * @param input
	 * @param output
	 * @throws IOException
	 */
	public static void fastStreamCopy(final InputStream input, final OutputStream output) throws IOException {
		ReadableByteChannel inputChannel = null;
		WritableByteChannel outputChannel = null;

		try {
			// get an channel from the stream
			inputChannel = Channels.newChannel(input);
			outputChannel = Channels.newChannel(output);
			// copy the channels
			ChannelUtil.fastChannelCopy(inputChannel, outputChannel);
		} finally {
			// closing the channels
			if (inputChannel != null) {
				inputChannel.close();
			}

			if (outputChannel != null) {
				outputChannel.close();
			}
		}
	}

	/**
	 * Copies src to dst
	 * 
	 * @param src
	 * @param dest
	 * @throws IOException
	 */
	public static void fastChannelCopy(final ReadableByteChannel src, final WritableByteChannel dest) throws IOException {
		final ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
		while (src.read(buffer) != -1) {
			// prepare the buffer to be drained
			buffer.flip();
			// write to the channel, may block
			dest.write(buffer);
			// If partial transfer, shift remainder down
			// If buffer is empty, same as doing clear()
			buffer.compact();
		}
		// EOF will leave buffer in fill state
		buffer.flip();
		// make sure the buffer is fully drained.
		while (buffer.hasRemaining()) {
			dest.write(buffer);
		}
	}
}
