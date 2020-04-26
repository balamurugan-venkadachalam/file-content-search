package com.bala.filesearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.bala.filesearch.service.FolderSearchService;
import org.apache.lucene.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@SpringBootApplication
public class Application {

    @Autowired
    private FolderSearchService folderSearchService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @ShellMethod("Add file to index: addFileToIndex <filePath>")
    public String addFileToIndex(String filePath) {
        String rtnVal = "added...";
        try {
            folderSearchService.addFileToIndex(filePath);
        } catch (IOException e) {
            rtnVal = e.getMessage();
            e.printStackTrace();
        }
        return rtnVal;
    }

    @ShellMethod("Search Content In Files: searchContent <queryString>")
    public List<String> searchContent(String queryString) throws IOException {
        List<String> rtnPath = new ArrayList<>();
        try {
            List<Document> rtnDocument = folderSearchService.searchFiles("contents", queryString);
            rtnDocument.stream().map(r -> r.getField("path").stringValue()).forEachOrdered(rtnPath::add);
        }catch (Exception e){
            rtnPath.add(e.getMessage());
            e.printStackTrace();
        }
        return rtnPath;
    }

    @ShellMethod("Search File Name: searchFileName <queryString>")
    public List<String> searchFileName(String queryString) throws IOException {
        List<String> rtnPath = new ArrayList<>();
        try {
            List<Document> rtnDocument = folderSearchService.searchFiles("path", queryString);
            rtnDocument.stream().map(r -> r.getField("path").stringValue()).forEachOrdered(rtnPath::add);
        }catch (Exception e){
            rtnPath.add(e.getMessage());
            e.printStackTrace();
        }
        return rtnPath;
    }


}