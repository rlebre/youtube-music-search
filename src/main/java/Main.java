/**
 * Created by Rui Lebre (ruilebre@ua.pt) on 8/19/17.
 */
/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ResourceId;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import org.apache.commons.cli.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Prints a list of videos based on a search term.
 *
 * @author Jeremy Walker
 */
public class Main {
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final long NUMBER_OF_VIDEOS_RETURNED = 1;
    private static YoutubeAPI youtubeapi;
    private static String API_KEY = "AIzaSyAhiCIqK92OgziRDvPSXeuQw3EQrtlP_ek";


    public static void main(String[] args) {

        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output folder");
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");

        List<Music> musicList = MusicFileReader.readMusicListFromCSV(new File(inputFilePath));

        youtubeapi = new YoutubeAPI.YoutubeAPIBuilder("youtube-cmdline-search-sample").build();

        try {
            List<String> videoIdsList = youtubeSearch(musicList);
            int videoDownloaderCount = 1;
            File file = new File("output.txt");
            Files.write(Paths.get(file.getPath()), videoIdsList, Charset.forName("UTF-8"));

            for (String videoLink : videoIdsList) {
                System.out.printf("Converting File %d of %d: %.2f%%", videoDownloaderCount, videoIdsList.size(), videoDownloaderCount * 100.0 / musicList.size());
                downloadVideo(videoLink, outputFilePath);
                videoDownloaderCount++;
            }
        } catch (IOException t) {
            t.printStackTrace();
        }
    }

    private static void downloadVideo(String link, String outputPath) {
        String command = "youtube-dl --extract-audio --audio-format mp3 --audio-quality 0 ";
        command += link;
        System.out.println(executeCommand(command));
    }

    private static String executeCommand(String command) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return output.toString();

    }

    private static List<String> youtubeSearch(List<Music> musicList) throws IOException {
        YouTube.Search.List search = youtubeapi.searchYoutubeList("id,snippet");
        setSearchMainParameters(search);
        List<String> youtubeLinksList = new LinkedList<>();

        int searchingItemCounter = 1;
        for (Music music : musicList) {
            if (music != null) {
                String queryTerm = music.getYoutubeQuery();
                search.setQ(queryTerm);
                SearchListResponse searchResponse = search.execute();

                List<SearchResult> searchResultList = searchResponse.getItems();
                youtubeLinksList.add("https://www.youtube.com/watch?v=" + searchResultList.get(0).getId().getVideoId());
                //System.out.println(YoutubeMp3Downloader.getLink(searchResultList.get(0).getId().getVideoId()));
                System.out.printf("Searching %d of %d: %.2f%%\n", searchingItemCounter, musicList.size(), searchingItemCounter * 100.0 / musicList.size());
                searchingItemCounter++;
            }
        }


        return youtubeLinksList;
    }


    private static void setSearchMainParameters(YouTube.Search.List search) {
        String apiKey = API_KEY;
        search.setKey(apiKey);
        search.setType("video");
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
    }

    private static String getInputQuery() throws IOException {
        String inputQuery = "";

        System.out.print("Please enter a search term: ");
        BufferedReader bReader = new BufferedReader(new InputStreamReader(System.in));
        inputQuery = bReader.readLine();

        if (inputQuery.length() < 1) {
            // If nothing is entered, defaults to "YouTube Developers Live."
            inputQuery = "YouTube Developers Live";
        }
        return inputQuery;
    }

    /*
     * Prints out all SearchResults in the Iterator. Each printed line includes title, id, and
     * thumbnail.
     *
     * @param iteratorSearchResults Iterator of SearchResults to print
     *
     * @param query Search query (String)
     */
    private static void prettyPrint(Iterator<SearchResult> iteratorSearchResults, String query) {

        System.out.println("\n=============================================================");
        System.out.println(
                "   First " + NUMBER_OF_VIDEOS_RETURNED + " videos for search on \"" + query + "\".");
        System.out.println("=============================================================\n");

        if (!iteratorSearchResults.hasNext()) {
            System.out.println(" There aren't any results for your query.");
        }

        while (iteratorSearchResults.hasNext()) {

            SearchResult singleVideo = iteratorSearchResults.next();
            ResourceId rId = singleVideo.getId();

            // Double checks the kind is video.
            if (rId.getKind().equals("youtube#video")) {
                Thumbnail thumbnail = singleVideo.getSnippet().getThumbnails().get("default");

                System.out.println(" Video Id" + rId.getVideoId());
                System.out.println(" Title: " + singleVideo.getSnippet().getTitle());
                System.out.println(" Thumbnail: " + thumbnail.getUrl());
                System.out.println("\n-------------------------------------------------------------\n");
            }
        }
    }
}