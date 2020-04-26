package com.bala.filesearch.service;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class FolderSearchServiceImpl implements FolderSearchService {

    private static Logger logger = LoggerFactory.getLogger(FolderSearchServiceImpl.class.getName());

    private Directory indexDirectory;
    private StandardAnalyzer analyzer;

    @Value("${file.list}")
    private List<String> fileTypeList;

    @Autowired
    public FolderSearchServiceImpl(Directory indexDirectory, StandardAnalyzer analyzer) {
        super();
        this.indexDirectory = indexDirectory;
        this.analyzer = analyzer;
    }

    private void indexDocs(final IndexWriter writer, Path path) throws IOException {
        logger.info("Path {}", path);
        //Directory?
        if (Files.isDirectory(path)) {
            logger.info("directory {}", path);
            //Iterate directory
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        logger.info("file {}", file);
                        String fileName = file.getFileName().toString();
                        if(fileTypeList.contains(fileName.substring(fileName.lastIndexOf(".")+1)) ) {
                            //Index this file
                            indexDoc(writer, file, attrs.lastModifiedTime().toMillis());
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            //Index this file
            indexDoc(writer, path, Files.getLastModifiedTime(path).toMillis());
        }
    }

    private void indexDoc(IndexWriter writer, Path file, long lastModified) throws IOException {
        logger.info("indexDoc {}", file);
        try (InputStream stream = Files.newInputStream(file)) {
            //Create lucene Document
            Document doc = new Document();

            doc.add(new StringField("path", file.toString(), Field.Store.YES));
            doc.add(new LongPoint("modified", lastModified));
            doc.add(new TextField("contents", new String(Files.readAllBytes(file)), Field.Store.YES));

            //Updates a document by first deleting the document(s)
            //containing <code>term</code> and then adding the new
            //document.  The delete and then add are atomic as seen
            //by a reader on the same index
            writer.updateDocument(new Term("path", file.toString()), doc);
        }
    }

    public void addFileToIndex(String filepath) throws IOException {
        Path path = Paths.get(filepath);
        File file = path.toFile();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        IndexWriter indexWriter = new IndexWriter(indexDirectory, indexWriterConfig);
        indexDocs(indexWriter, path);
        indexWriter.close();
    }

    public List<Document> searchFiles(String inField, String queryString) {
        try {
            Query query = new QueryParser(inField, analyzer).parse(queryString);
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            IndexSearcher searcher = new IndexSearcher(indexReader);
            TopDocs topDocs = searcher.search(query, 10);
            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                documents.add(searcher.doc(scoreDoc.doc));
            }
            return documents;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
