import java.io.*;
import java.util.*;


import java.io.IOException;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Output {

    private List<ProcessedReview> processedReviews;

    public Output() {
        processedReviews = new LinkedList();
    }

    public void writeOutputToJSONFile(String path) {
        JSONObject obj = new JSONObject();
        JSONArray procReviews = new JSONArray();
        Iterator<ProcessedReview> it = processedReviews.iterator();
        for(int i=0; i<processedReviews.size(); i++) {
            JSONObject entry = new JSONObject();
            ProcessedReview pr = it.next();
            entry.put("link",pr.getLink());
            entry.put("color",pr.getColor().name());
            JSONArray allEntities = new JSONArray();
            allEntities.addAll(pr.getNamedEntities());
            entry.put("namedEntities", allEntities);
            entry.put("isSarcastic",pr.isSarcastic());
            procReviews.add(entry);
        }
        obj.put("processedEntries",procReviews);

        try {
            FileWriter file = new FileWriter(path);
            file.write(obj.toJSONString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //This function recieves list of file paths with JSON content,
    //and it assembles all of them to the destination file, separated with "\n" between two JSONs.
    public static void assembleFiles(String[] paths, String destinationFile) {
        try (FileWriter output_file = new FileWriter(destinationFile)) {
            for (String file_path : paths) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file_path))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output_file.write(line);
                    }
                    //Split of json files
                    output_file.write("\n");
                } catch (IOException e) {
                    System.err.println("Error reading file: " + file_path);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to destination file: " + destinationFile);
            e.printStackTrace();
        }
    }


    public static void writeOutputToHTMLFile(String fileName) throws ParseException, IOException {
        // read json file
        JSONParser parser = new JSONParser();
        Reader reader = new FileReader(fileName);
        BufferedReader br = new BufferedReader(reader);
        List<String> json_list = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            json_list.add(line);
        }

        // Write HTML file
        try(FileWriter fileWriter = new FileWriter(fileName)) {
            fileWriter.write("<html>\n<head>\n</head>\n<body>\n");

            for ( String json :json_list ) {
                JSONObject jsonObject = (JSONObject) parser.parse(json);

                // Get the array of processed reviews
                JSONArray processedEntries = (JSONArray) jsonObject.get("processedEntries");
                // Iterate over processed reviews
                for (Object entry : processedEntries) {
                    JSONObject processedReview = (JSONObject) entry;
                    String link = (String) processedReview.get("link");
                    String color = ((String) processedReview.get("color")).toLowerCase().replace("_", "");
                    JSONArray namedEntities = (JSONArray) processedReview.get("namedEntities");
                    boolean isSarcastic = (boolean) processedReview.get("isSarcastic");

                    fileWriter.write("<div style=\"color: " + color + ";\">\n");
                    fileWriter.write("<p>Link: <a href=\"" + link + "\">" + link + "</a></p>\n");
                    fileWriter.write("<p>Named Entities: [" + String.join(", ", namedEntities) + "]</p>\n");
                    fileWriter.write("<p>Sarcasm Detection: " + (isSarcastic ? "Sarcastic" : "Not Sarcastic") + "</p>\n");
                    fileWriter.write("</div>\n");
                }
            }
            fileWriter.write("</body>\n</html>");
        }
        catch (IOException e) {
            System.err.println("Error writing to destination file: " + fileName);
            e.printStackTrace();
        }
    }

    public void appendProcessedReview(ProcessedReview pr) {
        processedReviews.add(pr);
    }

    public void appendProcessedReviews(List<ProcessedReview> pr) {
        processedReviews.addAll(pr);
    }

}