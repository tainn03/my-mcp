package com.mcp.service.impl;

import com.mcp.service.WebService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebServiceImpl implements WebService {
    @Value("${app.screenshots.api-key}")
    String SCREENSHOT_API_KEY;

    /**
     * Search the web for information and return relevant results.
     *
     * @param keyword The search keyword or query
     * @return The search results as a string
     */
    @Override
    public String search(String keyword) {
        try {
            String keySearch = URLEncoder.encode(keyword);
            String searchUrl = "https://www.google.com/search?q=" + keySearch;
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(searchUrl);
            return client.execute(request, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));
        } catch (IOException e) {
            return "Error searching the web: " + e.getMessage();
        }
    }

    /**
     * Fetch the content of a web page given its URL.
     *
     * @param url The URL of the web page to fetch
     * @return The content of the web page as a string
     */
    @Override
    public String fetchContent(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.body().text();
        } catch (IOException e) {
            return "Error fetching web content: " + e.getMessage();
        }
    }

    /**
     * Fetch the raw HTML content of a web page given its URL.
     *
     * @param url The URL of the web page to fetch
     * @return The raw HTML content of the web page as a string
     */
    @Override
    public String fetchHtml(String url) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet request = new HttpGet(url);
            return client.execute(request, httpResponse -> EntityUtils.toString(httpResponse.getEntity()));
        } catch (IOException e) {
            return "Error fetching web page: " + e.getMessage();
        }
    }

    /**
     * Analyze the structure of a web page and return its DOM tree.
     *
     * @param url The URL of the web page to analyze
     * @return The DOM tree of the web page as a string
     */
    @Override
    public String fetchHtmlDom(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.html();
        } catch (IOException e) {
            return "Error analyzing web page structure: " + e.getMessage();
        }
    }

    /**
     * Scrape specific data from a web page using a CSS selector.
     *
     * @param url         The URL of the web page to scrape
     * @param cssSelector The CSS selector to identify the data to scrape
     * @return The scraped data as a string
     */
    @Override
    public String scrape(String url, String cssSelector) {
        try {
            Document doc = Jsoup.connect(url).get();
            return doc.select(cssSelector).toString();
        } catch (IOException e) {
            return "Error scraping web page data: " + e.getMessage();
        }
    }

    /**
     * Generate a screenshot of a web page and save it as an image file.
     *
     * @param url      The URL of the web page to screenshot
     * @param fileType The desired image file type, either "png" or "jpeg"
     * @return The name of the saved image file or an error message
     */
    @Override
    public String screenshot(String url, String fileType) {
        String apiKey = SCREENSHOT_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) {
            return "Let's set up the screenshot API key first.";
        }
        final String targetUrl = URLEncoder.encode(url);
        try {
            InputStream inputStream = getInputStream(apiKey, fileType, targetUrl);
            String imageName = "screenshot" + (fileType.equalsIgnoreCase("jpeg") ? ".jpeg" : ".png");
            OutputStream outputStream = new FileOutputStream("./" + imageName);
            inputStream.transferTo(outputStream);
            return imageName;
        } catch (IOException e) {
            return "Error generating web page screenshot: " + e.getMessage();
        }
    }


    /**
     * Get InputStream from ScreenshotAPI
     *
     * @param fileType  String specifying the output type of the image, 'png' or 'jpeg'
     * @param targetUrl Encoded URI string container the URI you're targeting
     * @return InputStream from ScreenshotAPI
     * @throws IOException if an I/O error occurs
     */
    private InputStream getInputStream(final String apiKey, final String fileType, final String targetUrl) throws IOException {
        final String targetFileType = "jpeg".equalsIgnoreCase(fileType) ? "jpeg" : "png";
        final String query = String.format(
                "https://shot.screenshotapi.net/screenshot?token=%s&url=%s&output=image&file_type=%s",
                apiKey, targetUrl, targetFileType
        );
        final URL apiUrl = new URL(query);
        return apiUrl.openStream();
    }
}
