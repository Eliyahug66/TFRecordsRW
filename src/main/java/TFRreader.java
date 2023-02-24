import com.google.protobuf.ByteString;
import org.tensorflow.example.Example;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FeatureList;
import org.tensorflow.example.SequenceExample;
import org.tensorflow.hadoop.util.TFRecordReader;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
public class TFRreader {
    private final TFRecordReader reader;
    private final InputStream stream;
    // Opens either compressed or uncompressed TFR file, according to file extension
    public TFRreader(String path) throws IOException {
        stream = path.endsWith("gz") || path.endsWith("zip") ? new GZIPInputStream(new FileInputStream(path)) : new FileInputStream(path);
        reader = new TFRecordReader(stream,false);
    }

    // Reads next single training example from the TFR file and converts it into two key-value maps: context and sequences
    public boolean readExample(MapDS<String, List<Object>> context, MapDS<String, List<Object>> sequences) throws IOException {
        byte[] bytes = reader.read();
        if(bytes == null){
            stream.close();
            return false;
        }
        // read 'context' values of the training example
        for(Map.Entry<String, Feature> entry: Example.parseFrom(bytes).getFeatures().getFeatureMap().entrySet()){
            context.put(entry.getKey(), fromTfFeature(entry.getValue()));
        }

        // read the 'sequences' values of the training example
        for(Map.Entry<String, FeatureList> entry: SequenceExample.parseFrom(bytes).getFeatureLists().getFeatureListMap().entrySet()){
            sequences.put(entry.getKey(), fromTfFeature(entry.getValue().getFeatureList().get(0)));
        }
        return true;
    }

    //Converts TensorFlow bytes stream into Java arrays
    private List<Object> fromTfFeature(Feature feature) throws UnsupportedEncodingException {
        switch (feature.getKindCase()) {
            case INT64_LIST -> {
                return Arrays.asList(feature.getInt64List().getValueList().toArray());
            }
            case FLOAT_LIST -> {
                return Arrays.asList(feature.getFloatList().getValueList().toArray());
            }
            case BYTES_LIST -> {
                List<Object> ret = new ArrayList<>();
                for (ByteString s : feature.getBytesList().getValueList()) {
                    ret.add(s.toString("UTF-8"));
                }
                return ret;
            }
        }
        throw new ExceptionInInitializerError("unknown type in in fromTfFeature!");
    }
}
