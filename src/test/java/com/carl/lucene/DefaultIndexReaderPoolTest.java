package com.carl.lucene;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultIndexReaderPoolTest {

	@Test
	public void test() {
		DefaultIndexReaderPool defIndexPool = new DefaultIndexReaderPool("TEST", "D:/lucene", 10000);
	}

}
