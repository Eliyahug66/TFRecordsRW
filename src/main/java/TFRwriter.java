import com.google.protobuf.ByteString;
import org.apache.hadoop.io.BytesWritable;
import org.tensorflow.example.*;
import org.tensorflow.hadoop.util.TFRecordWriter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

public class TFRwriter {
    private final FileOutputStream file;
    private final ByteArrayOutputStream byteOutStream;
    private final TFRecordWriter writer;
    private final OutputStream outStream;
    private final int sequencesSize;

    // Opens a TensorFlow records file, that may contain multiple training examples. The file is opened as a compressed
    // or uncompressed, according to its extension. Padding is optional (set to -1 if not required)
    public TFRwriter(String path, int padding) throws IOException {
        sequencesSize = padding;
        file = new FileOutputStream(path);
        byteOutStream = new ByteArrayOutputStream();
        outStream = path.endsWith("gz") || path.endsWith("zip")? new GZIPOutputStream(file): byteOutStream;
        writer = new TFRecordWriter(new DataOutputStream(outStream));
    }
    // Converts the generated maps into TensorFlow records bytes streams and serializes them to disk
    public void writeExample(MapDS<String, List<Object>> context, MapDS<String, List<Object>> sequences) throws IOException, InstantiationException, IllegalAccessException {
        //Set context
        Features.Builder contextFeatures = Features.newBuilder();
        for (Map.Entry<String, List<Object>> entry : context.entrySet()) {
            contextFeatures.putFeature(entry.getKey(), contextToTf(entry.getValue())).build();
        }
        byte[] contextBytes = Example.newBuilder().setFeatures(contextFeatures).build().toByteArray();

        //Set sequences
        FeatureLists.Builder sequenceFeatures = FeatureLists.newBuilder();
        for (Map.Entry<String, List<Object>> entry : sequences.entrySet()) {
            sequenceFeatures.putFeatureList(entry.getKey(), sequenceToTf(entry.getValue()));
        }
        byte[] sequenceBytes = SequenceExample.newBuilder().setFeatureLists(sequenceFeatures).build().toByteArray();

        //Combine context & sequences into a single example
        byte[] combinedBytes = new byte[contextBytes.length + sequenceBytes.length];
        System.arraycopy(contextBytes, 0, combinedBytes, 0, contextBytes.length);
        System.arraycopy(sequenceBytes, 0, combinedBytes, contextBytes.length, sequenceBytes.length);
        writer.write(new BytesWritable(combinedBytes).getBytes());
        byteOutStream.writeTo(file);
        byteOutStream.reset();
    }
    public void close() throws IOException {
        outStream.close();
    }

    //Converts Java arrays into TensorFlow bytes streams for serialization
    protected FeatureList sequenceToTf(Object values) throws ExceptionInInitializerError {
        List list = (List) values;
        switch (list.get(0).getClass().getSimpleName()){
            case "Integer": //upcast
                list = (List) list.stream().map(d->((Integer) d).longValue()).collect(Collectors.toList());
            case "Long":
                if(list.size() < sequencesSize){
                    list = new ArrayList(list);
                    list.addAll(Collections.nCopies(sequencesSize - list.size(), 0L));
                }
                return FeatureList.newBuilder().addFeature(Feature.newBuilder().setInt64List(Int64List.newBuilder().addAllValue(list))).build();
            case "Double": //downcast
                list = (List) list.stream().map(d->((Double) d).floatValue()).collect(Collectors.toList());
            case "Float":
                if(list.size() < sequencesSize){
                    list = new ArrayList(list);
                    list.addAll(Collections.nCopies(sequencesSize - list.size(), 0f));
                }
                return FeatureList.newBuilder().addFeature(Feature.newBuilder().setFloatList(FloatList.newBuilder().addAllValue(list))).build();
            case "String":
                if(list.size() < sequencesSize){
                    list = new ArrayList(list);
                    list.addAll(Collections.nCopies(sequencesSize - list.size(), ""));
                }
                return FeatureList.newBuilder().addFeature(Feature.newBuilder().setBytesList(BytesList.newBuilder().addAllValue(list))).build();
        }
        throw new ExceptionInInitializerError("unknown type in in toTfFeature!");
    }

    //Converts Java arrays into TensorFlow bytes streams for serialization
    private Feature contextToTf(Object value) throws ExceptionInInitializerError {
        value = ((List) value).get(0);
        switch (value.getClass().getSimpleName()){
            case "Integer": //upcast
                value = ((Integer) value).longValue();
            case "Long":
                return Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue((Long) value).build()).build();
            case "Float":
            case "Double": //downcast!!!
                return Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue((Float) value).build()).build();
            case "String":
                return Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8((String) value)).build()).build();
        }
        throw new ExceptionInInitializerError("unknown type in in toTfFeature!");
    }
}