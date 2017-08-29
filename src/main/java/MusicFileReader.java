import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rui Lebre (ruilebre@ua.pt) on 8/23/17.
 */
public class MusicFileReader {
    public static List<Music> readMusicListFromCSV(File file) {
        List<Music> musicList = new LinkedList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Music musicInLine = parseLine(line);
                musicList.add(musicInLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return musicList;
    }

    private static Music parseLine(String line) {
        String[] musicMetadataElements = line.split(",");
        if (validLine(musicMetadataElements)) {
            return new Music(musicMetadataElements[0], musicMetadataElements[1], musicMetadataElements[2], musicMetadataElements[3]);
        } else {
            return null;
        }
    }

    private static boolean validLine(String[] line) {
        return line.length >= 4;
    }
}
