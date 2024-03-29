package mju.hadoop.wordcount;

 

import java.io.IOException;

import java.util.StringTokenizer;

 

import org.apache.hadoop.conf.Configuration;

import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.LongWritable;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

 

public class WordCount {

    public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);

        private Text word = new Text();

        @Override

        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line,"\t\n\r\f@!%,.:;?![]' ");
            
            while (tokenizer.hasMoreTokens()) {

                word.set(tokenizer.nextToken().toLowerCase());

                context.write(word, one);

            }

        }

    }

 

    public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable value = new IntWritable(0);

        @Override

        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            Configuration conf =context.getConfiguration();
            int cnt=Integer.parseInt(conf.get("cnt"));

            int sum = 0;

            for (IntWritable value : values)

                sum += value.get();

            value.set(sum);
            if(sum>cnt)context.write(key, value);

        }

    }

 

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        conf.set("cnt", args[2]);
        
        Job job = new Job(conf, "wordcount");

        job.setJarByClass(WordCount.class);

        job.setOutputKeyClass(Text.class);

        job.setOutputValueClass(IntWritable.class);

        job.setMapperClass(Map.class);

        job.setReducerClass(Reduce.class);

        job.setInputFormatClass(TextInputFormat.class);

        job.setOutputFormatClass(TextOutputFormat.class);

        job.setNumReduceTasks(1);

 
        
        FileInputFormat .setInputPaths(job, new Path(args[0]));

        FileOutputFormat.setOutputPath(job, new Path(args[1]));

 

        boolean success = job.waitForCompletion(true);

        System.out.println(success);

    }

}