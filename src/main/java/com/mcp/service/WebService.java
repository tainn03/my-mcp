package com.mcp.service;

public interface WebService {
    String search(String keyword);

    String fetchContent(String url);

    String fetchHtml(String url);

    String fetchHtmlDom(String url);
    
    String scrape(String url, String cssSelector);

    String screenshot(String url, String fileType);
}
