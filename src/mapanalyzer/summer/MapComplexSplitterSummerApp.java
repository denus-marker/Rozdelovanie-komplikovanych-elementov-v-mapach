package mapanalyzer.summer;

import java.io.File;
import java.util.Scanner;

public class MapComplexSplitterSummerApp {

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter .omap file path: ");
        String filePath = scanner.nextLine().trim();

        File inputFile = new File(filePath);
        if (!inputFile.exists() || !inputFile.isFile()) {
            System.err.println("File not found: " + filePath);
            return;
        }

        MapAreaAnalyzer.SplitStats stats = MapAreaAnalyzer.analyze(inputFile);

        System.out.println("Map file: " + inputFile.getName());
        System.out.println("Area objects (analyzed): " + stats.totalAreaObjects);
        System.out.println("Simple area objects: " + stats.simpleAreaObjects);
        System.out.println("Complex area objects: " + stats.complexAreaObjects);
        System.out.println("Produced simple parts: " + stats.producedSimpleParts);
    }
}