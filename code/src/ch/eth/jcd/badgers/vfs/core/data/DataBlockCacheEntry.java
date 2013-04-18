package ch.eth.jcd.badgers.vfs.core.data;

public class DataBlockCacheEntry {

	/**
	 * Start address of the first block
	 */
	private final long firstBlockLocation;

	/**
	 * Start address of the last block
	 */
	private long lastBlockLocation;

	private final DataBlockCacheEntryState state;

	public DataBlockCacheEntry(long firstBlockLocation, long lastBlockLocation, DataBlockCacheEntryState state) {
		this.firstBlockLocation = firstBlockLocation;
		this.lastBlockLocation = lastBlockLocation;
		this.state = state;
	}

	public DataBlockCacheEntryState getState() {
		return state;
	}

	public long getFirstBlockLocation() {
		return firstBlockLocation;
	}

	public long getLastBlockLocation() {
		return lastBlockLocation;
	}

	public boolean contains(long dataBlockLocation) {
		return firstBlockLocation <= dataBlockLocation && lastBlockLocation >= dataBlockLocation;
	}

	public DataBlockCacheEntry splitLower(long dataBlockLocation) {
		if (dataBlockLocation <= firstBlockLocation) {
			return null;
		}

		return new DataBlockCacheEntry(firstBlockLocation, dataBlockLocation - DataBlock.BLOCK_SIZE, state);
	}

	public DataBlockCacheEntry splitUpper(long dataBlockLocation) {
		if (dataBlockLocation >= lastBlockLocation) {
			return null;
		}

		return new DataBlockCacheEntry(dataBlockLocation + DataBlock.BLOCK_SIZE, lastBlockLocation, state);
	}

	public DataBlockCacheEntry splitThis(long dataBlockLocation, DataBlockCacheEntryState state) {
		return new DataBlockCacheEntry(dataBlockLocation, dataBlockLocation, state);
	}

	public long getNumberOfInvolvedBlocks() {
		return (lastBlockLocation - firstBlockLocation) / DataBlock.BLOCK_SIZE + 1;
	}

	@Override
	public String toString() {
		return firstBlockLocation + " to " + lastBlockLocation + " - " + getNumberOfInvolvedBlocks() + " Blocks " + state;
	}

	public void mergeFromRight(DataBlockCacheEntry toMerge) {
		assert lastBlockLocation + DataBlock.BLOCK_SIZE == toMerge.getFirstBlockLocation();
		assert state == toMerge.state;

		lastBlockLocation = toMerge.lastBlockLocation;
	}
}
