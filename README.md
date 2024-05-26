# Downloader

A Java-based web page downloader using Jsoup. This tool downloads web pages, including their resources (images, scripts, stylesheets), and saves them locally, preserving the directory structure.

## Features

- Downloads web pages and their associated resources.
- Preserves the directory structure.
- Avoids downloading resources from other domains.
- Limits recursion depth to avoid infinite loops.

## Limitations

- Won't download fonts (or other files) specified in css files.
- Waits for one hour before threads and application are terminated. Should probably be replaced by use of a CountDownLatch.

## Prerequisites

- Java 21 or higher (uses virtual threads)
- Maven

## Installation

1. **Clone the repository**

    ```sh
    git clone https://github.com/ppoluha/SiteDownloader.git
    cd SiteDownloader
    ```

2. **Build the project using Maven**

    ```sh
    mvn clean package
    ```

## Usage

1. **Run the downloader**

    ```sh
    java -jar target/site-downloader-1.0-SNAPSHOT.jar se.hkr.downloader.Downloader <sourceUrl> <targetFolder>
    ```

    - `<sourceUrl>`: The URL of the web page to download.
    - `<targetFolder>`: The local directory where the downloaded files will be saved.

    For example:

    ```sh
    java -jar target/site-downloader-1.0-SNAPSHOT.jar se.hkr.downloader.Downloader https://example.com /path/to/target/folder
    ```

## Project Structure

- `src/main/java`: Java source files.
- `pom.xml`: Maven configuration file.
