package com.carl.lucene;

import org.junit.Before;
import org.junit.Test;

public class IndexDemoTest {
	private IndexDemo ido;
	
	@Before 
	public void init(){
		 ido = new IndexDemo();
	}
	@Test
	public void testIndex() {
		
		ido.index();
	}

	@Test
	public void testQuery() {
		ido.query("content","go");
	}
	@Test
	public void testQueryAll() {
		ido.queryAll();
	}
	
	
	@Test
	public void testDelete() {
		ido.delete("id","1");
	}
	@Test
	public void testUnDelete() {
		ido.undelete();
	}
	/**
	 * 先执行testIndex,然后执行testQuery（“id”,"1"），再执行testUpdate，再执行testQuery（"id","0"）
	 */
	@Test
	public void testUpdate() {
		ido.update("1");
	}

}
