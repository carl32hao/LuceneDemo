package com.carl.lucene;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SearchDemoTest {
	private SearchDemo sd;
	
	@Before
	public void init(){
		sd = new SearchDemo();
	}
	@Test
	public void testSearchByTerm() {
		sd.searchByTerm("name", "catty", 3);
	}
	@Test
	public void testSearchByRangeInt() {
		sd.searchByIntRange("attach", 1, 3, 9);
	}
	@Test
	public void testSearchByNDimensionRangeInt() {
		sd.searchByIntNDimensionRange("attachandcc", new int[]{1,1},new int[]{3,3}, 9);
	}
	@Test
	public void testSearchByTermRangeStr() {
		sd.searchByTermRange("id", "1", "3", 6);
		System.out.println("=================================================");
		sd.searchByTermRange("name", "a", "d", 10);
	}

}
