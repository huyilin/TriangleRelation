package org.myorg;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class hw2 {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
//		private final static IntWritable one = new IntWritable(1);
		private Text localkey;
		private Text localvalue;
		private Text connectvalue=new Text("C"); //Representing these two points are directly connected
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			String line = value.toString();
			String [] linearray=line.split(",");
			localvalue=new Text(linearray[0]);
			for(int i=1;i<linearray.length;i++){
				localkey=new Text(linearray[0]+","+linearray[i]);
				output.collect(localkey,connectvalue); //key:pair of direct connected nodes,value:"C":connected
				for(int j=1;j<i;j++){					
					     	localkey=new Text(linearray[i]+","+linearray[j]);
							output.collect(localkey,localvalue);	//key:pair of nodes,value: peak node connected with these two nodes.
				}
			}
		}
	}
	
	public static class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {
		private Text relationvalue;
		public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
//			int sum = 0;
			ArrayList<String> valueList= new ArrayList<String>();
			while (values.hasNext()) {
				valueList.add(values.next().toString());
			}
			if(valueList.contains("C")&&(valueList.size()>1)){
				output.collect(key,new Text(Integer.toString(valueList.size()-1)));
			}
		}
	}

	public static class Map1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
		
		private Text localkey;
		private IntWritable localvalue;
		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			String line= value.toString();
			String[] linearray=line.split(",|\\s+");
			localkey=new Text("TriangleNumbers:");
			localvalue=new IntWritable(Integer.parseInt(linearray[2]));
			output.collect(localkey,localvalue);
		}
	}
	
	public static class Reduce1 extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
		private Text outputsymbol;
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			sum=sum/3;
			output.collect(key,new IntWritable(sum));
		}
	}
	
	public static void main(String[] args) throws Exception {
		String [] pathtemparray=args[1].split("/"); 
		StringBuilder pathtempstr=new StringBuilder();
		for(int i=0;i<pathtemparray.length-1;i++){
			 pathtempstr.append(pathtemparray[i]+"/");
		}
		
		pathtempstr.append("temp");
		String pathtemp=pathtempstr.toString();    //Specify the path to store the result of first reduce function
		
		JobConf conf = new JobConf(hw2.class);
		conf.setJobName("TriangleFindPhase1");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
		conf.setReducerClass(Reduce.class);
		conf.setNumMapTasks(80);
		conf.setNumReduceTasks(80);
//		TextInputFormat inputForm1= new TextInputFormat();
//		inputForm1.setMaxInputSplitSize(conf,5120000);
//		conf.setInputFormat(inputForm1);
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
 	
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(pathtemp));
		JobClient.runJob(conf);

//		The second mapreduce function
		JobConf confnext=new JobConf(hw2.class);
		confnext.setNumMapTasks(80);
		confnext.setNumReduceTasks(80);
		confnext.setJobName("TriangleFindPhase2");
		confnext.setOutputKeyClass(Text.class);
		confnext.setOutputValueClass(IntWritable.class);

		confnext.setMapperClass(Map1.class);
		confnext.setReducerClass(Reduce1.class);
		
//		TextInputFormat inputForm2= new TextInputFormat();
//		inputForm2.setMaxInputSplitSize(confnext,5120000);
//		confnext.setInputFormat(inputForm2.class);
		confnext.setInputFormat(TextInputFormat.class);
		confnext.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(confnext, new Path(pathtemp,"part-00000")); //read data from the temp(Output file of the first mapreduce)
		FileOutputFormat.setOutputPath(confnext, new Path(args[1]));
		JobClient.runJob(confnext);
	}	
}