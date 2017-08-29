import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.io.IOException;

/**
 * Created by Rui Lebre (ruilebre@ua.pt) on 8/24/17.
 */
public class YoutubeAPI {
    private YouTube youtube;
    private HttpTransport HTTP_TRANSPORT;
    private JsonFactory JSON_FACTORY;

    public YoutubeAPI(YoutubeAPIBuilder builder) {
        this.youtube = builder.youtube;
        this.HTTP_TRANSPORT = builder.HTTP_TRANSPORT;
        this.JSON_FACTORY = builder.JSON_FACTORY;
    }

    public YouTube.Search.List searchYoutubeList(String attributes) throws IOException {
        return youtube.search().list(attributes);
    }

    public static class YoutubeAPIBuilder {
        private YouTube youtube;
        private HttpTransport HTTP_TRANSPORT;
        private JsonFactory JSON_FACTORY;

        public YoutubeAPIBuilder(String applicationName) {
            HTTP_TRANSPORT = new NetHttpTransport();
            JSON_FACTORY = new JacksonFactory();

            youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpRequestInitializer() {
                public void initialize(HttpRequest request) throws IOException {
                }
            }).setApplicationName(applicationName).build();

        }

        public YoutubeAPIBuilder setYoutube(YouTube youtube) {
            this.youtube = youtube;
            return this;
        }

        public YoutubeAPIBuilder setHTTP_TRANSPORT(HttpTransport HTTP_TRANSPORT) {
            this.HTTP_TRANSPORT = HTTP_TRANSPORT;
            return this;
        }

        public YoutubeAPIBuilder setJSON_FACTORY(JsonFactory JSON_FACTORY) {
            this.JSON_FACTORY = JSON_FACTORY;
            return this;
        }

        public YoutubeAPI build() {
            return new YoutubeAPI(this);
        }
    }
}
