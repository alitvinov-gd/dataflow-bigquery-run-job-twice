package org.litvinov.andrei.beam.bquery.example;

import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;

public class BigqueryPipeline {
    public interface BigqueryPipelineOptions extends PipelineOptions {
        String getInputTable();
        void setInputTable(String value);

        String getOutputFile();
        void setOutputFile(String value);
    }

    public static void main(String[] args) {
        BigqueryPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation()
                .as(BigqueryPipelineOptions.class);
        Pipeline pipeline = Pipeline.create(options);
        String query = "select * from " + options.getInputTable();
        pipeline.apply(BigQueryIO.readTableRows()
                .fromQuery(query)
                .usingStandardSql())
                .apply(MapElements.into(TypeDescriptors.strings())
                .via((TableRow row) -> (String) row.get("id")))
                .apply(TextIO.write().to(options.getOutputFile()));
        pipeline.run();
    }

}
