/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.sqoop.mapreduce;

import org.apache.avro.Schema;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import parquet.avro.AvroParquetOutputFormat;
import parquet.avro.AvroWriteSupport;
import parquet.hadoop.ParquetOutputFormat;
import parquet.hadoop.metadata.CompressionCodecName;

import java.io.IOException;

/**
 * Helper class for setting up a native Parquet MapReduce job using parquet-avro and parquet-hadoop.
 */
public final class ParquetJob {

  public static final Log LOG = LogFactory.getLog(ParquetJob.class.getName());

  public static final String CONF_AVRO_SCHEMA = "parquet.avro.schema";
  static final String CONF_OUTPUT_CODEC = "parquetjob.output.codec";

  public enum WriteMode {
    DEFAULT, APPEND, OVERWRITE
  }

  private ParquetJob() {
  }

  public static Schema getAvroSchema(Configuration conf) {
    String schemaString = conf.get(CONF_AVRO_SCHEMA);
    if (schemaString == null) {
      schemaString = conf.get("parquetjob.avro.schema");
    }
    return new Schema.Parser().parse(schemaString);
  }

  public static CompressionCodecName getCompressionCodec(Configuration conf) {
    String codec = conf.get(CONF_OUTPUT_CODEC, "snappy");
    try {
      return CompressionCodecName.fromConf(codec);
    } catch (IllegalArgumentException ex) {
      LOG.warn(String.format(
          "Unsupported compression codec '%s'. Fallback to 'snappy'.", codec));
      return CompressionCodecName.SNAPPY;
    }
  }

  /**
   * Configure the import job with AvroParquetOutputFormat and CompressionCodecName.
   */
  public static void configureImportJob(Job job, Schema schema) throws IOException {
    Configuration conf = job.getConfiguration();
    conf.set(CONF_AVRO_SCHEMA, schema.toString());
    conf.set("parquetjob.avro.schema", schema.toString());
    AvroWriteSupport.setSchema(conf, schema);
    AvroParquetOutputFormat.setSchema(job, schema);
    ParquetOutputFormat.setCompression(job, getCompressionCodec(conf));
  }

  /**
   * Compatibility overload for configureImportJob.
   */
  public static void configureImportJob(JobConf conf, Schema schema,
      String uri, WriteMode writeMode) throws IOException {
    conf.set(CONF_AVRO_SCHEMA, schema.toString());
    conf.set("parquetjob.avro.schema", schema.toString());
    AvroWriteSupport.setSchema(conf, schema);
    conf.set(ParquetOutputFormat.COMPRESSION, getCompressionCodec(conf).name());
  }

}
