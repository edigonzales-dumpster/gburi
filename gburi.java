///usr/bin/env jbang "$0" "$@" ; exit $?
//REPOS mavenCentral,ehi=http://jars.interlis.ch/
//DEPS org.xerial:sqlite-jdbc:3.36.0.3 org.jsoup:jsoup:1.14.3 org.apache.commons:commons-csv:1.9.0



import static java.lang.System.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class gburi {

    public static void main(String... args) throws IOException {
        String CSV_FILE = "/Users/stefan/Downloads/fubar.csv";
        String GPKG_URL = "jdbc:sqlite:/Users/stefan/Downloads/av_lv95/geopackage/av_lv95.gpkg";

        BufferedWriter writer = Files.newBufferedWriter(Paths.get(CSV_FILE));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("GBNr", "BfSNr", "Eigentuemer"));

        HttpClient client = HttpClient.newBuilder()
        .build();

        try(Connection con = DriverManager.getConnection(GPKG_URL);
        Statement stmt = con.createStatement(); ) {
            try (ResultSet rs = stmt.executeQuery("SELECT * FROM resf");) {
                int i=0;
                while(rs.next()) {
                    String bfsNr = rs.getString("nbident").substring(8); 
                    System.out.println(bfsNr);
                    String gbNr = rs.getString("nummer");
                    System.out.println(gbNr);

                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://egov.ur.ch/teraspur/wsclient/WSgbausMSXML2.asp?ws=GBAUS&gruid="+gbNr+"."+bfsNr+"&gb="+bfsNr+"&lang=D&asn=1&dar=&hist='"))
                        .header("Content-Type", "text/html")
                        .header("Cookie", "ASPSESSIONIDSABCQBQA=IIJGPGMAENOOHHCDNDOKIACA")
                        .GET()
                        .build();

                    //System.out.println(request.uri().toString());

                    HttpResponse<String> response;
                    try {
                        response = client.send(request, BodyHandlers.ofString());

                        Document doc = Jsoup.parse(response.body());

                        Element paragraph = doc.select("p.eigentum").first();
                        Element outerSpan = paragraph.select("span.statR").first();
                        Element innerSpan = paragraph.select("span.fett").first();
                        for (TextNode node : innerSpan.textNodes()) {
                            csvPrinter.printRecord(gbNr, bfsNr, node.text() + ", " + outerSpan.ownText());
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    } 
                    i++;
                    if (i>1000) {
                        break;
                    }
                }
                csvPrinter.flush();  

           } catch (SQLException e) {
                e.printStackTrace();
           }
        } catch (SQLException e) {
                e.printStackTrace();
        }
    }
}
