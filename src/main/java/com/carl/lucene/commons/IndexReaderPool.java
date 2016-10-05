package com.carl.lucene.commons;

import org.apache.lucene.index.IndexReader;

public interface IndexReaderPool {
	/**
	 * 获取IndexReader，如有则返回对应的IndexReader对象，否则返回空
	 * @param fullPath 目录名
	 * @return 
	 */
	public IndexReader getIndexReader(String directoryName);
	/**
	 * 强制刷新IndexReader
	 */
	public void forceRefresh();
	/**
	 * 如需要则刷新IndexReader
	 */
	public void refreshIfNeed();
	/**
	 * 释放所有资源
	 * @return
	 */
	public boolean release();
}
