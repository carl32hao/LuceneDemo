package com.carl.lucene;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.QueryBuilder;

import com.carl.lucene.enums.IndexEnum;
import com.carl.lucene.utils.FieldUtil;

/**
 * Lucene学习
 * Field.Store存储域选项（YES|NO）
 * 
 * YES:表示会把这个域中的内容完全存储到（lucene产生的）文件中，方便进行文本的还原
 * NO:表示把这个域的内容不存储到文件中，但是可以被索引，此时内容无法完全还原（即通过document.get无法获取文本）
 * 
 * 
 * Field.Index索引域选项
 * Index.ANALYZED:进行分词和索引，适用于标题、内容等
 * Index.NOT_ANALYZED:进行索引但是不进行分词，如身份证号，姓名，ID等，适用于精确搜索
 * Index.ANALYZED_NOT_NORMS:进行分词但是不存储NORMS信息，这个norms中包括了创建索引的时间和权值等信息
 * Index.NOT_ANALYZED_NOT_NORMS:即不分词也不存储norms信息（比较少用）
 * Index.NO:不进行索引
 * 
 */
public class App {

	public static void main(String[] args) {
		System.out.println("Hello World!");
	}

	public void index(IndexEnum indexEnum) {
		IndexWriter indexWiter = null;
		try {
			// 创建directory
			// Directory directory = new RAMDirectory();
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath("D:/lucene/index0"));

			// 创建indexWriter
			IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
			indexWiter = new IndexWriter(directory, iwc);
			indexWiter.deleteAll();
			// 创建 document
			Document document = null;
			// 为document添加Field
			File file = new File("D:/lucene/example");
			for (File f : file.listFiles()) {
				document = new Document();
				if (indexEnum.equals(IndexEnum.SF)) {
					FieldUtil.indexByStringField(document, f);
				} else if (indexEnum.equals(IndexEnum.TF)) {
					FieldUtil.indexByTextField(document, f);
				} else {
					FieldUtil.indexByField(document, f);
				}
				indexWiter.addDocument(document);
			}
			// 通过indexwriter添加文档到索引中
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (indexWiter != null) {
				try {
					indexWiter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	

	public void searcher(String key) {
		try {
			// 创建Directory
			Directory directory = FSDirectory.open(FileSystems.getDefault().getPath("D:/lucene/index0"));

			// 创建IndexReader
			IndexReader indexReader = DirectoryReader.open(directory);
			// 根据IndexReader创建IndexSearcher
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			// 创建搜索的Query
			Analyzer analyzer = new StandardAnalyzer();
			// 创建Query表示搜索域为content包含java的文档
			Query query = new QueryBuilder(analyzer).createPhraseQuery("content", key,0);

			// 根据searcher搜索并且返回TopDocs
			TopDocs topDocs = indexSearcher.search(query, 10);
			// 根据TopDocs获取ScoreDoc对象
			for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
				// 根据searcher和scoreDoc对象获取具体的Document对象
				Document document = indexSearcher.doc(scoreDoc.doc);
				// 根据Document对象获取需要的值
				System.out.println(document.get("filename") + ",path[" + document.get("path") + "]"+",content["+document.get("content")+"]");
			}
			// 关闭reader
			indexReader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
