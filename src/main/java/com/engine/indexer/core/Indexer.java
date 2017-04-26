package com.engine.indexer.core;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Indexer {
    private String html = "<html><head><title>First parse</title></head>"
            + "<body><p>Parsed HTML into a doc.</p></body></html>";

    private Document doc = Jsoup.parse(html);

    public void printIt() {
        System.out.println(doc.body().text());

    }

}

