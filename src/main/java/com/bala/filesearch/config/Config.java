package com.bala.filesearch.config;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;

@Configuration
public class Config {

    @Bean
    public Directory getIndexDirectory() throws IOException {
        String indexPath = "index";
        return FSDirectory.open(Paths.get(indexPath));
    }

    @Bean
    public StandardAnalyzer getAnalyzer() {
        return new StandardAnalyzer();
    }
}
