package com.carl.lucene;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

/**
 * 索引示例
 * 
 * @author caiyongfeng
 *
 */
public class IndexDemo {
	private String[] ids = { "1", "2", "3", "4", "5", "6" };
	private String[] mails = { "aa@itat.com", "bb@gew.com", "cc@acl.com", "dd@zzed.com", "ee@itat.com",
			"ff@clw.com" };
	private String[] contents = { "It is a test mail!", "Wellcome to our wedding Tomorrow!", "Cheeps,go ahead!",
			"It is a goal!", "This is my favoriest food!", "Catty,let's go to swiming" };
	private int[] attaches = { 1, 2, 3, 4, 5, 5 };
	private String[] names = { "John", "carl", "catty", "liting", "mike", "jacky" };
	private Directory directory = null;
	private Date[] dates = null;
	public IndexDemo() {
		try {
			dates= initDates();
			directory = FSDirectory.open(FileSystems.getDefault().getPath("d:/lucene/index1"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Date[] initDates(){
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
				//给mail这个field加权  默认为1  
				Field mailFld = new Field("mail", mails[i], TextField.TYPE_STORED);
				document.add(mailFld);
				document.add(new Field("content", contents[i], TextField.TYPE_STORED));
				Field nameFld = new Field("name", names[i], TextField.TYPE_STORED);
				if(names[i].indexOf("ca")!=-1){
					nameFld.setBoost(1.8f);
				}
				document.add(nameFld);
				// 索引排序
				document.add(new NumericDocValuesField("attach", attaches[i]));
				// 存储数值型数据
				document.add(new StoredField("attach", attaches[i]));
				//索引日期
				//document.add(new NumericDocValuesField("date", dates[i].getTime()));
				document.add(new StoredField("date",dates[i].getTime()));
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
	/**
	 * 查询
	 * @param fld
	 * @param key
	 */
	public void query(String fld, String key) {
		try {
			//索引读取器对象
			IndexReader reader = DirectoryReader.open(directory);
			System.out.println("索引文档个数：" + reader.numDocs());
			System.out.println("索引文档最大个数：" + reader.maxDoc());
			System.out.println("索引文档删除个数：" + reader.numDeletedDocs());
			//索引搜索器
			IndexSearcher indexSearcher = new IndexSearcher(reader);
			//根据查找条件及指定的field创建查询对象
			Query query = new QueryBuilder(new StandardAnalyzer()).createPhraseQuery(fld, key);
			//查找文档
			TopDocs topDocs = indexSearcher.search(query, 10);
			System.out.println("topDocs.scoreDocs.length:" + topDocs.scoreDocs.length);
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				Document doc = indexSearcher.doc(scoreDoc.doc);
				System.out.println("score:"+scoreDoc.score +",id:" + doc.get("id") + ",mail:" + doc.get("mail") + ",content:" + doc.get("content")
						+ ",name:" + doc.get("name") + ",attach:" + doc.get("attach")+",date:"+doc.get("date"));

			}
			//关闭reader
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 删除索引
	 * @param fld
	 * @param key
	 */
	public void delete(String fld, String key) {
		IndexWriter writer = null;
		try {
			//writer配置，默认使用StandardAnyalyzer
			IndexWriterConfig iwc = new IndexWriterConfig();
			writer = new IndexWriter(directory, iwc);
			//创建reader对象
			IndexReader reader = DirectoryReader.open(directory);
			//创建查找器
			IndexSearcher indSer = new IndexSearcher(reader);
			// query 删除符合query条件的集合
			Query query = new QueryBuilder(new StandardAnalyzer()).createPhraseQuery(fld, key);
			//查找文档
			TopDocs topDocs = indSer.search(query, 10);
			System.out.println("符合条件的document有：" + topDocs.scoreDocs.length);
			//根据查询条件对象删除索引
			writer.deleteDocuments(query);
			System.out.println("AFTER DEL 符合条件的document有：" + indSer.search(query, 10).scoreDocs.length);
			// term精确删除一项
			Term term = new Term(fld,"liting");
			writer.deleteDocuments(term);
			//关闭reader
			reader.close();

		} catch (Exception e) {
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
	/**
	 * 还原
	 */
	public void undelete() {
		// do nothing ,because officer don't give a method to call!!!!
		// 官方说4.0之后把IndexReader的undeleteAll方法删除了，并且没有替代这个方法的新方法
	}
	
	/**
	 * 更新索引文档
	 * @param id 要更新的文档id
	 * @param doc 更新后的索引文档
	 */
	public void update(String id){
		IndexWriter writer =null;
		try{
			writer = new IndexWriter(directory, new IndexWriterConfig());
			Document document = new Document();
			document.add(new Field("id", "0", TextField.TYPE_STORED));
			document.add(new Field("mail", mails[0], TextField.TYPE_STORED));
			document.add(new Field("content", contents[0], TextField.TYPE_STORED));
			document.add(new Field("name", names[0], TextField.TYPE_STORED));
			// 索引排序
			document.add(new NumericDocValuesField("attach", attaches[0]));
			// 存储数值型数据
			document.add(new StoredField("attach", attaches[0]));
			writer.updateDocument(new Term("id",id), document);
			writer.flush();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void queryAll() {
		try {
			//索引读取器对象
			IndexReader reader = DirectoryReader.open(directory);
			System.out.println("索引文档个数：" + reader.numDocs());
			System.out.println("索引文档最大个数：" + reader.maxDoc());
			System.out.println("索引文档删除个数：" + reader.numDeletedDocs());
			//索引搜索器
			IndexSearcher indexSearcher = new IndexSearcher(reader);
			
			//查找文档
			for (int i = 0;i<reader.maxDoc();i++) {
				Document doc = indexSearcher.doc(i);
				System.out.println("id:" + doc.get("id") + ",mail:" + doc.get("mail") + ",content:" + doc.get("content")
						+ ",name:" + doc.get("name") + ",attach:" + doc.get("attach")+",date:"+doc.get("date"));

			}
			//关闭reader
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
