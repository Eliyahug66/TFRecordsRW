import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Test {
    // A write-read usage example: generates context and sequence values for two training examples and serializes
    // them. Next, they are read from that file.
    public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException {
        String firstTfrFilePath = "tfr0001.gz";
        // Generates hypothetical examples and serializes them to disk
        new Test().generateAndWriteExamples(firstTfrFilePath);
        // Reads examples from disk one by one and convert to Java maps
        new Test().readExamples(firstTfrFilePath, false);
    }

    // Generates two hypothetical training examples, converts them into TensorFlow records (with padding - set to -1
    // if not required) and serializes them into a file.
    private void generateAndWriteExamples(String path) throws IOException, InstantiationException, IllegalAccessException {
        int paddingSize = 256;
        TFRwriter writer = new TFRwriter(path, paddingSize);
        List<MapDS<String, List<Object>>> maps = generateExamples();
        for(int i = 0; i < maps.size(); i += 2)
            writer.writeExample(maps.get(i), maps.get(i+1));
        writer.close();
    }

    // Reads training examples from file (a file may contain multiple examples) and converts them into Java hash maps
    private void readExamples(String path, boolean echo) throws IOException {
        int examplesNum = 0;
        TFRreader reader = new TFRreader(path);
        for(boolean found = true; found; examplesNum++) {
            MapDS<String, List<Object>> context = new MapDS<>();
            MapDS<String, List<Object>> sequences = new MapDS<>();
            found = reader.readExample(context, sequences);
            if(echo && found)
                printExample(context, sequences);
        }
        if(echo)
            System.out.printf("Read %d examples", examplesNum);
    }

    //Generates hypothetical two training examples into an ArrayList with two successive entries for each example:
    // the first is the map with the context values and the second contains the context values. So total size of the
    // list is 4 in this case
    private List<MapDS<String, List<Object>>> generateExamples(){
        MapDS<String, List<Object>> context1 = new MapDS<>(Arrays.asList("recordNum", "historyLength", "isRecurring"),
                List.of(List.of(1L), List.of(7L), List.of(0L)));
        context1.put("clientId", List.of("dfy6-sdfg-16fh-ghjq"));
        MapDS<String, List<Object>> sequences1 = new MapDS<>(Arrays.asList("eventsTypes", "returned", "timeDelta"),
                List.of(Arrays.asList(65L, 5L, 4780L, 149L ,150L, 288L, 1L), Arrays.asList(0, 0, 0, 0, 0, 1, 0),
                        Arrays.asList(0f, 0.109f, 0.334f, 0.82f, 1.275f, 1.275f, 3.756f)));

        MapDS<String, List<Object>> context2 = new MapDS<>(Arrays.asList("recordNum", "historyLength", "isRecurring",
                "timeDelta"), List.of(List.of(2L), List.of(4L), List.of(1L), Arrays.asList(0f, 0.2f, 0.44f,
                1.82f, 2.805f, 2.805f, 7.3012f)));
        context2.put("clientId", List.of("s140-lsp1-00dq-2395"));
        MapDS<String, List<Object>> sequences2 = new MapDS<>(Arrays.asList("eventsTypes", "returned"),
                List.of(Arrays.asList(66L, 5L, 60L, 1L), Arrays.asList(0, 0, 0, 0)));

        return new ArrayList<>(Arrays.asList(context1, sequences1, context2, sequences2));
    }
    private void printExample(MapDS<String, List<Object>> context, MapDS<String, List<Object>> sequences) {
        System.out.println("\nContext:");
        for(Map.Entry<String, List<Object>> entry: context.entrySet()){
            System.out.println(entry);
        }
        System.out.println("\nSequences:");
        for(Map.Entry<String, List<Object>> entry: sequences.entrySet()){
            System.out.println(entry);
        }
        System.out.println();
    }
}