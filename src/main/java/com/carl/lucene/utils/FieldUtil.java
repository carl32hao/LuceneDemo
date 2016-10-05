package com.carl.lucene.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public final class FieldUtil {
	private FieldUtil(){}
	public static void indexByField(Document document, File f) throws FileNotFoundException {
		// Field:文件中的内容会被标识和索引，搜索时不需要完全匹配即可搜索到 --（目前只知道这些，后面继续总结）
		document.add(new Field("content", new FileReader(f), TextField.TYPE_NOT_STORED));
		document.add(new Field("filename", f.getName(), TextField.TYPE_STORED));
		document.add(new Field("path", f.getAbsolutePath(), TextField.TYPE_STORED));
	}

	public static void indexByTextField(Document document, File f) throws IOException {
		// TextField:文件中的内容会被标识和索引，搜索时不需要完全匹配即可搜索到 --（目前只知道这些，后面继续总结）
		document.add(new TextField("content", FileUtils.readFileToString(f), Field.Store.YES));//存储文档的内容，以便可以在查找时获取文档的内容，但是一般建议这样做
		document.add(new TextField("filename", f.getName(), Field.Store.YES));
		document.add(new TextField("path", f.getAbsolutePath(), Field.Store.YES));
	}

	public static void indexByStringField(Document document, File f) throws IOException {
		// StringField:文件中的所有内容都会被索引，搜索时要完全匹配内容才能搜索到 --（目前只知道这些，后面继续总结）
		document.add(new StringField("content", FileUtils.readFileToString(f), Field.Store.YES));//存储文档的内容
		document.add(new StringField("filename", f.getName(), Field.Store.YES));
		document.add(new StringField("path", f.getAbsolutePath(), Field.Store.YES));
	}
	
	
}
