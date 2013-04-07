package ch.eth.jcd.badgers.vfs.core.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public class DataBlockCache {

	private static final Logger LOGGER = Logger.getLogger(DataBlockCache.class);

	private final List<DataBlockCacheEntry> cache = new ArrayList<>();

	public DataBlockCache() {

	}

	public DataBlockCache(long firstBlockLocation, long lastBlockLocation) {
		cache.add(new DataBlockCacheEntry(firstBlockLocation, lastBlockLocation, DataBlockCacheEntryState.UNKNOWN));
	}

	public DataBlockCacheEntry getNextFreeOrUnkownDataBlocks() {
		for (DataBlockCacheEntry entry : cache) {
			if (entry.getState() != DataBlockCacheEntryState.OCCUPIED) {
				return entry;
			}
		}
		return null;
	}

	public void markOccupied(long dataBlockLocation) {
		LOGGER.debug("Mark Occupied Block " + dataBlockLocation);
		markBlock(dataBlockLocation, DataBlockCacheEntryState.OCCUPIED);
		mergeBlocks();
	}

	public void markFree(long dataBlockLocation) {
		LOGGER.debug("Mark Free Block " + dataBlockLocation);
		markBlock(dataBlockLocation, DataBlockCacheEntryState.FREE);
		mergeBlocks();
	}

	public void addFreeBlocks(long firstBlockLocation, long lastBlockLocation) {
		LOGGER.debug("Add Free Blocks from " + firstBlockLocation + " to " + lastBlockLocation);
		cache.add(new DataBlockCacheEntry(firstBlockLocation, lastBlockLocation, DataBlockCacheEntryState.FREE));
	}

	private void markBlock(long dataBlockLocation, DataBlockCacheEntryState state) {
		for (int i = 0; i < cache.size(); i++) {
			DataBlockCacheEntry current = cache.get(i);
			if (current.contains(dataBlockLocation)) {
				cache.remove(i);

				DataBlockCacheEntry lower = current.splitLower(dataBlockLocation);
				DataBlockCacheEntry thisEntry = current.splitThis(dataBlockLocation, state);
				DataBlockCacheEntry upper = current.splitUpper(dataBlockLocation);

				if (upper != null) {
					cache.add(i, upper);
					if (thisEntry != null) {
						cache.add(i, thisEntry);
					}
					if (lower != null) {
						cache.add(i, lower);
					}
				}

				// block marked
				return;
			}
		}
	}

	private void mergeBlocks() {
		Iterator<DataBlockCacheEntry> iter = cache.iterator();

		DataBlockCacheEntry previous = null;
		while (iter.hasNext()) {
			DataBlockCacheEntry current = iter.next();
			if (previous != null && previous.getState() == current.getState()) {
				previous.mergeFromRight(current);
				iter.remove();
			} else {
				previous = current;
			}
		}
	}
}
