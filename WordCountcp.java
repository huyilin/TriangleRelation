package org.myorg;

import java.io.IOException;
import java.util.*;
	
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
 	
public class TriangleFind {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
//		private final static IntWritable one = new IntWritable(1);
		private Text user;
		private Text uservalue;
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			String line = value.toString();
			String [] linearray=line.split(",");
			for(int i=1;i<linearray.length;i++){
				for(int j=1;j<linearray.length&&j!=i;j++){
					String userstr=linearray[1];
					user=Text(userstr);
					String uservalstr=linearray[i]+","+linearray[j];
					uservalue=Text(uservalstr);
					output.collect(user,uservalue);
				}
			}
		}
	}
 	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		private Text relationvalue;
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
//			int sum = 0;
			ArrayList<String> myList= new ArrayList<String>();
			while (values.hasNext()) {
				myList.add(values.next().toString());
			}
			for(String s: myList){
				String [] sarray=s.split(",");
				String inverse=sarray[1]+","+sarray[0];
				relationvalue=Text(inverse);
				if (myList.contains(inverse)){
					output.collect(key,relationvalue);
				}
			}
		}
	}
 	
	public static void main(String[] args) throws Exception {
		JobConf conf = new JobConf(TriangleFind.class);
		//		Job job1=new job(jobconf1);
//		JobClient.run(job1);
//		Job job2=new job(jobconf2);
//		JobClient.run(job2);
		
		job1.setJobName("TriangleFind");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);
 	
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
