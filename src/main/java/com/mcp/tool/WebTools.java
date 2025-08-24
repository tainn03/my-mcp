package com.mcp.tool;

import com.mcp.service.WebService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebTools {
    WebService webService;

    /**
     * Search the web for information and return relevant results.
     *
     * @param keyword The search keyword or query
     * @return The search results as a string
     */
    @Tool(name = "w01_search_web", description = "Search the web for information and return relevant results.")
    public String searchWeb(@ToolParam String keyword) {
        return webService.search(keyword);
    }

    /**
     * Fetch the content of a web page given its URL.
     *
     * @param url The URL of the web page to fetch
     * @return The content of the web page as a string
     */
    @Tool(name = "w02_fetch_web_content", description = "Fetch the content of a web page given its URL.")
    public String fetchWebContent(@ToolParam String url) {
        return webService.fetchContent(url);
    }

    /**
     * Fetch the raw HTML content of a web page given its URL.
     *
     * @param url The URL of the web page to fetch
     * @return The raw HTML content of the web page as a string
     */
    @Tool(name = "w03_fetch_web_html", description = "Fetch the raw HTML content of a web page given its URL.")
    public String fetchWebHtmlPage(@ToolParam String url) {
        return webService.fetchHtml(url);
    }

    /**
     * Analyze the structure of a web page and return its DOM tree.
     *
     * @param url The URL of the web page to analyze
     * @return The DOM tree of the web page as a string
     */
    @Tool(name = "w04_fetch_web_DOM_tree", description = "Analyze the structure of a web page and return its DOM tree.")
    public String fetchWebDOMTree(@ToolParam String url) {
        return webService.fetchHtmlDom(url);
    }

    /**
     * Scrape specific data from a web page based on given criteria.
     *
     * @param url         The URL of the web page to scrape
     * @param cssSelector The CSS selector to identify the data to scrape
     * @return The scraped data as a string
     */
    @Tool(name = "w05_scrape_web_page_data", description = "Scrape specific data from a web page based on given criteria.")
    public String scrapeWebPageData(@ToolParam String url, @ToolParam String cssSelector) {
        return webService.scrape(url, cssSelector);
    }

    /**
     * Generate a screenshot of a web page given its URL.
     *
     * @param url      Encoded URI string container the URI you're targeting
     * @param fileType String specifying the output type of the image, 'png' or 'jpeg'
     * @return A path to the saved screenshot file or an error message
     */
    @Tool(name = "w06_generate_web_page_screenshot", description = "Generate a screenshot of a web page given its URL.")
    public String generateWebPageScreenshot(@ToolParam final String url, @ToolParam final String fileType) {
        return webService.screenshot(url, fileType);
    }
}
