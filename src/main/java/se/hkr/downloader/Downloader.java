package se.hkr.downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class Downloader {
    private static final Set<String> processedUrls = new HashSet<>();
    private static final int MAX_DEPTH = 10;

    public static void main(String[] args) throws IOException, URISyntaxException {
        String sourceUrl = args[0];
        String targetFolder = args[1];
        downloadPage(sourceUrl, new URI(sourceUrl), targetFolder, MAX_DEPTH);
    }

    private static void downloadPage(String sourceUrl, URI baseUri, String targetFolder, int depth) throws IOException, URISyntaxException {
        if (depth <= 0 || processedUrls.contains(sourceUrl)) {
            return;
        }
        processedUrls.add(sourceUrl);
        System.out.printf("Downloading %s%n", sourceUrl);
        Document htmlDoc = Jsoup.connect(sourceUrl).get();
        savePage(htmlDoc.outerHtml(), sourceUrl, targetFolder);
        HtmlContent pageContent = parsePage(htmlDoc);
        saveResources(pageContent.resources(), targetFolder, baseUri);
        followLinks(pageContent.links(), targetFolder, baseUri, depth - 1);
    }

    private static void savePage(String html, String sourceUrl, String targetFolder) throws IOException, URISyntaxException {
        Path targetPath = getTargetPath(sourceUrl, targetFolder);
        Files.createDirectories(targetPath.getParent());
        try (FileWriter writer = new FileWriter(targetPath.toFile())) {
            writer.write(html);
        }
    }

    private static Path getTargetPath(String sourceUrl, String targetFolder) throws URISyntaxException {
        URI uri = new URI(sourceUrl);
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }

        Path targetPath = Paths.get(targetFolder, path).normalize();
        if (Files.isDirectory(targetPath)) {
            targetPath = targetPath.resolve("index.html");
        }
        return targetPath;
    }

    private static void followLinks(Elements links, String targetFolder, URI baseUri, int depth) {
        for (Element link : links) {
            String href = link.attr("abs:href");
            if (href.isEmpty()) continue;
            try {
                downloadPage(href, baseUri, targetFolder, depth);
            } catch (IOException | URISyntaxException e) {
                System.err.printf("Failed to follow link: %s. Cause: %s%n", href, e);
            }
        }
    }

    private static HtmlContent parsePage(Document htmlDoc) {
        Elements links = htmlDoc.select("a[href]");
        Elements images = htmlDoc.select("img[src]");
        Elements scripts = htmlDoc.select("script[src]");
        Elements styles = htmlDoc.select("link[rel=stylesheet]");
        return new HtmlContent(links, images, scripts, styles);
    }

    private static void saveResources(Elements resources, String targetFolder, URI baseUri) {
        for (Element resource : resources) {
            String resourceUrl = resource.attr("abs:src").isEmpty() ?
                    resource.attr("abs:href") :
                    resource.attr("abs:src");
            if (resourceUrl.isEmpty()) continue;
            if (processedUrls.contains(resourceUrl)) continue;
            processedUrls.add(resourceUrl);
            try {
                URI uri = new URI(resourceUrl);
                if (!baseUri.getHost().equals(uri.getHost())) continue;
                Path targetPath = getTargetPath(resourceUrl, targetFolder);
                if (Files.exists(targetPath)) continue;
                System.out.printf("Downloading resource: %s%n", uri);
                Files.createDirectories(targetPath.getParent());
                try (InputStream in = uri.toURL().openStream()) {
                    Files.copy(in, targetPath);
                }
            } catch (Exception e) {
                System.err.printf("Failed to save resource: %s. Cause: %s%n", resourceUrl, e);
            }
        }
    }
}
