package org.myorg;

import java.io.IOException;
import java.util.*;
	
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
 	
public class hw2 {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, Text> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		public void map(LongWritable key, Text value, OutputCollector<IntWritable, Text> output, Reporter reporter) throws IOException {
			String line = value.toString();
			String [] immediatekey= new String[2];
			String[] s=line.split(",");
			for (int i=1;i<s.length,i++){
				for(int j=1,j<s&&j!=i,j++){
					key=s[i];
					immediatekey[0]=s[j];
					immediatekey[1]=s[0];
				//	ArrayList.add(immediatekey);
				//  connectlist.add(immediatekey);
					output.collect(key,immediatekey);
				}
			}
			//StringTokenizer tokenizer = new StringTokenizer(line);
/*			while (tokenizer.hasMoreTokens()) {
				word.set(tokenizer.nextToken());
				connectlist.add(new connection());
				connectlist.get(1).node1=5;
				output.collect(word, one);*/
			}
		}
	}
 	
public static class Reduce extends MapReduceBase implements Reducer<IntWritable, String[], Text, IntWritable> {
	public void reduce(IntWritable key, Iterator<String[]> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
		while (values.hasNext()){
			value=values.next().get();
			compare[0]=value[1];
			compare[1]=value[0];
			
			if values.contain(compare){
				Output.collect(compare,ture);
			}
		}
	}
}
 	
	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(HW2.class);
		conf.setJobName("HW2");
 	
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);
		
		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
 	
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
 	
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 	
		JobClient.runJob(conf);
	}
}	
