package com.carl.lucene;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.carl.lucene.commons.IndexReaderPool;

/**
 * 根据不同的目录维护一个IndexReader池，提高IO性能 
 * @author caiyongfeng
 *
 */
public class DefaultIndexReaderPool implements IndexReaderPool {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	//默认的旧InderReader关闭时间间隔
	private final static int DEFAULT_CLOSE_INTERVAL = 5000; 
	//池的名称
	private String poolName;
	//池的根目录
	private String rootDirectory;
	//旧IndexReader关闭时间间隔，强制刷新或有需刷新后旧的IndexReader会保存到这个集合中
	private int staleReaderClsTime;
	//旧IndexReader后台回收线程
	private ExecutorService es;
	//同步锁
	private final Object lock = new Object();
	//正常的IndexReader集合，初始化或刷新后会把新的IndexReader保存到这个集合中
	private final ConcurrentHashMap<String, IndexReader>indexReaderMap = new ConcurrentHashMap<>();
	//旧的IndexReader集合
	private final ConcurrentHashMap<Long,IndexReader>staleReaderMap = new ConcurrentHashMap<>();
	/**
	 * 以poolName创建一个IndexReader池，根目录为rootDirectory，旧的reader清除时间间隔是staleReaderClsTime毫秒
	 * @param poolName
	 * @param rootDirectory
	 * @param staleReaderClsTime
	 */
	public DefaultIndexReaderPool(String poolName,String rootDirectory,int staleReaderClsTime) {
		this.poolName = poolName;
		this.rootDirectory = rootDirectory;
		this.staleReaderClsTime = staleReaderClsTime;
		//初始化
		init();
	}
	/**
	 * 以poolName创建一个IndexReader池，根目录为rootDirectory，旧的reader清除时间间隔是DEFAULT_CLOSE_INTERVAL毫秒
	 * @param poolName
	 * @param rootDirectory
	 */
	public DefaultIndexReaderPool(String poolName,String rootDirectory) {
		this(poolName,rootDirectory,DEFAULT_CLOSE_INTERVAL);
	}
	/**
	 * 以根目录为rootDirectory创建一个IndexReader池
	 * @param rootDirectory
	 */
	public DefaultIndexReaderPool(String rootDirectory) {
		this(null,rootDirectory,DEFAULT_CLOSE_INTERVAL);
	}
	/**
	 * 初始化IndexReader池，并启动一个后台线程执行旧IndexReader的回收工作
	 */
	private void init(){
		//初始化IndexReader
		initIndexReader();
		//启动旧Reader后台回收线程
		activeStaleReaderRecycleThread();
	}
	
	/*
	 *  启动旧Reader后台回收线程
	 */
	private void activeStaleReaderRecycleThread() {
		//启动旧的IndexReader后台回收线程
		es = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName(poolName + "-staleIndexReader-Thread");
				return t;
			}
		});
		
		es.submit(new Runnable() {
			@Override
			public void run() {
				logger.info("stale reader recycle thread actived!");
				if(staleReaderMap.size() > 0){
					for(Entry<Long,IndexReader> entry : staleReaderMap.entrySet()){
						try {
							//超时则回收资源
							if((entry.getKey() + staleReaderClsTime) > System.currentTimeMillis()){
								entry.getValue().close();
								logger.info(Thread.currentThread().getName() + "recycled  successfully");
							}
						} catch (IOException e) {
							logger.warn("staleReader close error!");
						}
					}
				}
			}
		});
	}
	
	/*
	 * 初始化IndexReader 
	 */
	private void initIndexReader() {
		//获取要目录下的所有子目录
		if(StringUtils.isEmpty(rootDirectory)){
			throw new IllegalArgumentException("rootDirectory would not be empty!");
		}
		File rootDir = new File(rootDirectory);
		//根据子目录列表初始化IndexReader
		for(File f:rootDir.listFiles()){
			if(f.isDirectory()){
				try {
					Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(f.getAbsolutePath()));
					IndexReader indexReader = DirectoryReader.open(directory); 
					if(indexReader!=null){
						synchronized (lock) {
							if(this.indexReaderMap.get(f.getName())==null){							
								this.indexReaderMap.put(f.getName(), indexReader);
							}
						}
					}
				} catch (IOException e) {
					logger.error("open directory error!",e);
					throw new RuntimeException("init indexReader error!");
				}
			}
		}
	}
	
	/**
	 * 根据目录名获取IndexReader
	 */
	@Override
	public IndexReader getIndexReader(String directoryName) {
		if(StringUtils.isEmpty(directoryName)){
			logger.error("argument is empty -{}",directoryName);
			throw new IllegalArgumentException("argument is not allow empty!");
		}
		return this.indexReaderMap.get(directoryName);
	}
	
	/***
	 * 强制刷新IndexReader，但是旧的IndexReader不清空
	 */
	@Override
	public void forceRefresh() {
		//清空IndexReaderMap中已经初始化的IndexReader
		this.indexReaderMap.clear();
		//重新初始化
		initIndexReader();
	}

	@Override
	public void refreshIfNeed() {
		//获取要目录下的所有子目录
		if(StringUtils.isEmpty(rootDirectory)){
			throw new IllegalArgumentException("rootDirectory would not be empty!");
		}
		File rootDir = new File(rootDirectory);
		//根据子目录列表更新IndexReader
		for(File f:rootDir.listFiles()){
			if(f.isDirectory()){
				try {
					IndexReader oldReader = this.indexReaderMap.get(f.getName());
					IndexReader indexReader = DirectoryReader.openIfChanged((DirectoryReader) oldReader); 
					if(indexReader!=null){
						synchronized (lock) {
							if(this.indexReaderMap.get(f.getName())==null){							
								this.indexReaderMap.put(f.getName(), indexReader);
							}
						}
					}
				} catch (IOException e) {
					logger.error("refresh indexReader error!",e);
					throw new RuntimeException("refresh indexReader error!");
				}
			}
		}
	}

	@Override
	public boolean release() {
		//关闭indexReaderMap中的indexReader
		if(this.indexReaderMap.size()>0){
			for (Entry<String,IndexReader> entry:indexReaderMap.entrySet()) {
				try {
					entry.getValue().close();
				} catch (IOException e) {
					logger.error("indexReader close error!");
					return false;
				}
			}
		}
		//停止旧Reader回收线程
		es.shutdown();
		//关闭staleReaderMap中的indexReader
		if(this.staleReaderMap.size()>0){
			for (Entry<Long,IndexReader> entry:staleReaderMap.entrySet()) {
				try {
					entry.getValue().close();
				} catch (IOException e) {
					logger.error("stale indexReader close error!");
					return false;
				}
			}
		}
		return true;
	}
	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public String getRootDirectory() {
		return rootDirectory;
	}

	public void setRootDirectory(String rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	public int getStaleReaderClsTime() {
		return staleReaderClsTime;
	}
	public void setStaleReaderClsTime(int staleReaderClsTime) {
		this.staleReaderClsTime = staleReaderClsTime;
	}
	
}
