package se.hkr.downloader;

import org.jsoup.select.Elements;

public record HtmlContent(Elements links, Elements images, Elements scripts, Elements styles) {
    public Elements resources() {
        Elements resources = new Elements(scripts);
        resources.addAll(styles);
        resources.addAll(images);
        return resources;
    }
}
