package com.bala.filesearch.service;

import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

public interface FolderSearchService {
    public void addFileToIndex(String filepath) throws IOException;

    public List<Document> searchFiles(String inField, String queryString);

}
