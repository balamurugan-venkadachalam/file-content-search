package com.bala.filesearch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import com.bala.filesearch.service.FolderSearchServiceImpl;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
import org.junit.Test;

public class LuceneFileSearchIntegrationTest {

    @Test
    public void verifySearchQueryWhenFetchedFileNameCorrect() throws IOException, URISyntaxException {
        String indexPath = "index";
        String dataPath = "src/main/resources/file1.txt";

        Directory directory = FSDirectory.open(Paths.get(indexPath));
        FolderSearchServiceImpl luceneFileSearch = new FolderSearchServiceImpl(directory, new StandardAnalyzer());

        luceneFileSearch.addFileToIndex(dataPath);

        List<Document> docs = luceneFileSearch.searchFiles("contents", "text");

        Assert.assertEquals("file1.txt", docs.get(0).get("filename"));
    }

}