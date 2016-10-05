package com.carl.lucene;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultIndexReaderPoolTest {

	@Test
	public void test() {
		DefaultIndexReaderPool defIndexPool = new DefaultIndexReaderPool("TEST", "D:/lucene", 2000);
		while(true){
			try {
				Thread.sleep(10000);
				break;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		defIndexPool.release();
	}

}
