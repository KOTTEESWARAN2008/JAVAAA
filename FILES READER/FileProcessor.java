
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FileProcessor {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter file name: ");
        String filePath = sc.nextLine().trim();

        if (filePath.toLowerCase().endsWith(".txt")) {
            processTextFile(filePath);
        } else if (filePath.toLowerCase().endsWith(".json")) {
            processJsonFile(filePath);
        } else if (filePath.toLowerCase().endsWith(".xml")) {
            processXmlFile(filePath);
        } else {
            System.out.println("Unsupported file format.");
        }

        sc.close();
    }

    // 1. TXT FILE PROCESSING
    public static void processTextFile(String filePath) {
        try {
            String content = Files.readString(Paths.get(filePath));
            List<String> lines = Files.readAllLines(Paths.get(filePath));

            long lineCount = lines.size();

            long wordCount = content.trim().isEmpty() ? 0 :
                    Pattern.compile("\\S+")
                    .matcher(content).results().count();

            long letterCount = content.chars()
                    .filter(Character::isLetter)
                    .count();

            long characterCount = content
                    .replace("\r", "")
                    .replace("\n", "")
                    .length();

            System.out.println("\n--- TXT FILE ANALYSIS ---");
            System.out.println("Line Count      : " + lineCount);
            System.out.println("Word Count      : " + wordCount);
            System.out.println("Letter Count    : " + letterCount);
            System.out.println("Character Count : " + characterCount);

        } catch (IOException e) {
            System.out.println("Error reading TXT file: "
                    + e.getMessage());
        }
    }

    // 2. JSON FILE PROCESSING
    // Supports a JSON array containing simple objects.
    public static void processJsonFile(String filePath) {
        try {
            String json = Files.readString(Paths.get(filePath));

            System.out.println("\n--- JSON FILE CONTENT ---");

            Pattern objectPattern = Pattern.compile("\\{([^{}]*)\\}");
            Matcher objectMatcher = objectPattern.matcher(json);

            int personCount = 0;

            while (objectMatcher.find()) {
                String object = objectMatcher.group(1);
                personCount++;

                System.out.println("\nPerson " + personCount);

                printJsonField(object, "id", "ID");
                printJsonField(object, "name", "Name");
                printJsonField(object, "age", "Age");
                printJsonField(object, "course", "Course");
            }

            if (personCount == 0) {
                System.out.println("No person records found.");
            }

        } catch (IOException e) {
            System.out.println("Error reading JSON file: "
                    + e.getMessage());
        }
    }

    public static void printJsonField(
            String object, String field, String label) {

        Pattern fieldPattern = Pattern.compile(
                "\"" + Pattern.quote(field)
                + "\"\\s*:\\s*(?:\"([^\"]*)\"|(\\d+))",
                Pattern.CASE_INSENSITIVE);

        Matcher matcher = fieldPattern.matcher(object);

        if (matcher.find()) {
            String value = matcher.group(1) != null
                    ? matcher.group(1) : matcher.group(2);

            System.out.println(label + "    : " + value);
        }
    }

    // 3. XML FILE PROCESSING
    public static void processXmlFile(String filePath) {
        try {
            File xmlFile = new File(filePath);

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            // Prevent XML external entity processing.
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);

            DocumentBuilder builder =
                    factory.newDocumentBuilder();

            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            System.out.println("\n--- XML FILE CONTENT ---");

            Element root = doc.getDocumentElement();

            // Read each student element under the root.
            NodeList students = root.getElementsByTagName("student");

            for (int i = 0; i < students.getLength(); i++) {
                Element student = (Element) students.item(i);

                System.out.println("\nPerson " + (i + 1));

                printXmlField(student, "id", "ID");
                printXmlField(student, "name", "Name");
                printXmlField(student, "age", "Age");
                printXmlField(student, "course", "Course");
            }

            if (students.getLength() == 0) {
                System.out.println(
                        "No <student> records found in XML file.");
            }

        } catch (Exception e) {
            System.out.println("Error reading XML file: "
                    + e.getMessage());
        }
    }

    public static void printXmlField(
            Element parent, String tag, String label) {

        NodeList nodes = parent.getElementsByTagName(tag);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            // Print only fields belonging directly to this student.
            if (node.getParentNode() == parent) {
                String value = node.getTextContent().trim();
                System.out.println(label + "    : " + value);
                return;
            }
        }
    }
}
