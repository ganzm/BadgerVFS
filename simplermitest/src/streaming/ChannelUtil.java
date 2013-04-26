package streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * $Id: ChannelUtil.java 48 2013-03-27 12:24:33Z rohrp $
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

		// get an channel from the stream
		final ReadableByteChannel inputChannel = Channels.newChannel(input);
		final WritableByteChannel outputChannel = Channels.newChannel(output);
		// copy the channels
		ChannelUtil.fastChannelCopy(inputChannel, outputChannel);
		// closing the channels
		inputChannel.close();
		outputChannel.close();
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
