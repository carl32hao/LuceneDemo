package com.carl.lucene;



import org.junit.Test;

import com.carl.lucene.enums.IndexEnum;

public class AppTest {

	@Test
	public void test_index_TF() {
		App app = new App();
		app.index(IndexEnum.TF);
	}
	
	@Test
	public void test_index_F() {
		App app = new App();
		app.index(IndexEnum.F);
	}
	
	@Test
	public void test_index_SF() {
		App app = new App();
		app.index(IndexEnum.SF);
	}
	
	@Test
	public void test_search() {
		App app = new App();
		app.searcher("2016100300011");
	}
}
