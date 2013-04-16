package ch.eth.jcd.badgers.vfs.core.model;

public class DiskSpaceUsage {
	private long maxData;
	private long freeData;
	private long maxDataBlocks;
	private long freeDataBlocks;
	private long maxDirectoryBlocks;
	private long freeDirectoryBlocks;

	public long getMaxData() {
		return maxData;
	}

	public void setMaxData(long maxData) {
		this.maxData = maxData;
	}

	public long getFreeData() {
		return freeData;
	}

	public void setFreeData(long freeData) {
		this.freeData = freeData;
	}

	public void setMaxDataBlocks(long maxDataBlocks) {
		this.maxDataBlocks = maxDataBlocks;
	}

	public void setFreeDataBlocks(long freeDataBlocks) {
		this.freeDataBlocks = freeDataBlocks;
	}

	public void setMaxDirectoryBlocks(long maxDirectoryBlocks) {
		this.maxDirectoryBlocks = maxDirectoryBlocks;
	}

	public void setFreeDirectoryBlocks(long freeDirectoryBlocks) {
		this.freeDirectoryBlocks = freeDirectoryBlocks;
	}

	public long getMaxDataBlocks() {
		return maxDataBlocks;
	}

	public long getFreeDataBlocks() {
		return freeDataBlocks;
	}

	public long getMaxDirectoryBlocks() {
		return maxDirectoryBlocks;
	}

	public long getFreeDirectoryBlocks() {
		return freeDirectoryBlocks;
	}

}
