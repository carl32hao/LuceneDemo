package com.carl.lucene;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class SearchDemo {

	private String[] ids = { "1", "2", "3", "4", "5", "6" };
	private String[] mails = { "aa@itat.com", "bb@gew.com", "cc@acl.com", "dd@zzed.com", "ee@itat.com", "ff@clw.com" };
	private String[] contents = { "It is a test mail!", "Wellcome to our wedding Tomorrow!", "Cheeps,go ahead!",
			"It is a goal!", "This is my favoriest food!", "Catty,let's go to swiming" };
	private int[] attaches = { 1, 2, 3, 4, 5, 5 };
	private int[] cces = { 2, 4, 3, 1, 4, 13 };
	private String[] names = { "John", "carl", "catty", "liting", "mike", "jacky" };
	private Directory directory = null;
	private Date[] dates = null;
	private IndexReader reader = null;

	public SearchDemo() {
		try {
			dates = initDates();
			directory = new RAMDirectory();
			index();
			reader = DirectoryReader.open(directory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Date[] initDates() {
		Date[] newDates = new Date[6];
		try {
			newDates[0] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-1-2 22:12:10");
			newDates[1] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-2-2 22:12:10");
			newDates[2] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-3-2 22:12:10");
			newDates[3] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-4-2 22:12:10");
			newDates[4] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-5-2 22:12:10");
			newDates[5] = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2016-6-2 22:12:10");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return newDates;
	}

	/**
	 * 创建索引
	 */
	public void index() {
		IndexWriter writer = null;
		try {
			IndexWriterConfig iwc = new IndexWriterConfig();
			writer = new IndexWriter(directory, iwc);
			Document document = null;
			writer.deleteAll();
			for (int i = 0; i < ids.length; i++) {
				document = new Document();
				document.add(new Field("id", ids[i], TextField.TYPE_STORED));
				// 给mail这个field加权 默认为1
				Field mailFld = new Field("mail", mails[i], TextField.TYPE_STORED);
				document.add(mailFld);
				document.add(new Field("content", contents[i], TextField.TYPE_STORED));
				Field nameFld = new Field("name", names[i], TextField.TYPE_STORED);
				if (names[i].indexOf("ca") != -1) {
					nameFld.setBoost(1.8f);
				}
				document.add(nameFld);
				// 索引排序
				document.add(new NumericDocValuesField("attach", attaches[i]));
				// 为了数值型数据查询--附件数查询
				document.add(new IntPoint("attach", attaches[i]));
				// 为了数值型数据查询--附件数和抄送人数查询
				document.add(new IntPoint("attachandcc", attaches[i],cces[i]));
				// 存储数值型数据
				document.add(new StoredField("attach", attaches[i]));
				// 存储数值型数据
				document.add(new StoredField("cc", cces[i]));
				// 索引日期
				// document.add(new NumericDocValuesField("date",
				// dates[i].getTime()));
				document.add(new StoredField("date", dates[i].getTime()));
				writer.addDocument(document);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public IndexSearcher getSearcher() {
		try {
			IndexReader tr = DirectoryReader.openIfChanged((DirectoryReader) reader);
			if (tr != null) {
				reader.close();
				reader = tr;
			}
			return new IndexSearcher(reader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void searchByTerm(String fld, String key, int num) {

		try {
			IndexSearcher searcher = getSearcher();
			Query query = new TermQuery(new Term(fld, key));
			TopDocs topDocs = searcher.search(query, num);
			System.out.println("查询到的结果数：" + topDocs.totalHits);
			for (ScoreDoc sd : topDocs.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("score:" + sd.score + ",id:" + doc.get("id") + ",name:" + doc.get("name") + ",mail:"
						+ doc.get("mail") + ",attach:" + doc.get("attach") + ",content:" + doc.get("content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 根据ASCII码值比较进行范围查询，只适用于英文的查询
	 * 
	 * @param fld
	 * @param start
	 * @param end
	 * @param num
	 */
	public void searchByTermRange(String fld, String start, String end, int num) {

		try {
			IndexSearcher searcher = getSearcher();
			BytesRef bytesRefStart = start == null ? null : new BytesRef(start.getBytes());
			BytesRef bytesRefEnd = end == null ? null : new BytesRef(end.getBytes());
			Query query = new TermRangeQuery(fld, bytesRefStart, bytesRefEnd, true, true);
			TopDocs topDocs = searcher.search(query, num);
			System.out.println("查询到的结果数：" + topDocs.totalHits);
			for (ScoreDoc sd : topDocs.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("score:" + sd.score + ",id:" + doc.get("id") + ",name:" + doc.get("name") + ",mail:"
						+ doc.get("mail") + ",content:" + doc.get("content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 一维整型数据查询，生成索引时需要增加IntPoint
	 * 
	 * @param fld
	 * @param start
	 * @param end
	 * @param num
	 */
	public void searchByIntRange(String fld, int start, int end, int num) {

		try {
			IndexSearcher searcher = getSearcher();
			Query query = IntPoint.newRangeQuery(fld, start, end);
			TopDocs topDocs = searcher.search(query, num);
			System.out.println("查询到的结果数：" + topDocs.totalHits);
			for (ScoreDoc sd : topDocs.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("score:" + sd.score + ",id:" + doc.get("id") + ",name:" + doc.get("name") + ",mail:"
						+ doc.get("mail") + ",content:" + doc.get("content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * N维整型数据查询，生成索引时需要增加IntPoint
	 * 
	 * @param fld
	 * @param start
	 * @param end
	 * @param num
	 */
	public void searchByIntNDimensionRange(String fld, int[] start, int[] end, int num) {

		try {
			IndexSearcher searcher = getSearcher();
			Query query = IntPoint.newRangeQuery(fld, start, end);
			TopDocs topDocs = searcher.search(query, num);
			System.out.println("查询到的结果数：" + topDocs.totalHits);
			for (ScoreDoc sd : topDocs.scoreDocs) {
				Document doc = searcher.doc(sd.doc);
				System.out.println("id:" + doc.get("id") + ",name:" + doc.get("name") + ",mail:"
						+ doc.get("mail") + ",attach:"+doc.get("attach")+",cc:"+doc.get("cc")+",content:" + doc.get("content"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
