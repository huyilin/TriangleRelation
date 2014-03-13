package org.myorg;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
//import org.apache.hadoop.mapred.jobcontrol.*;

public class hw2 {
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {
//		private final static IntWritable one = new IntWritable(1);
		private Text user;
		private Text uservalue;
		public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
			String line = value.toString();
			String [] linearray=line.split(",");
			for(int i=1;i<linearray.length;i++){
				String userstr=linearray[i];
				user=new Text(userstr);
				for(int j=1;j<linearray.length;j++){
						if(j!=i){
							String uservalstr=linearray[j]+","+linearray[0];
							uservalue=new Text(uservalstr);
							output.collect(user,uservalue);
						}
				}
			}
		}
	}

	public static class Map1 extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text TriangleKey;
		public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			TriangleKey=new Text("Triangle");
			output.collect(TriangleKey,one);
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
				if (myList.contains(inverse)){
					relationvalue=new Text(s);
					output.collect(key,relationvalue);
				}
			}
		}
	}

	public static class Reduce1 extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {
		private Text outputsymbol;
		public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += values.next().get();
			}
			sum=sum/6;
			output.collect(key,new IntWritable(sum));
		}
	}
	

	public static void main(String[] args) throws Exception {

		//Specify the path to store the result of first reduce function
		String [] pathtemparray=args[1].split("/"); 
		StringBuilder pathtempstr=new StringBuilder();		
		for(int i=0;i<pathtemparray.length-1;i++){
			 pathtempstr.append(pathtemparray[i]+"/");
		}
		pathtempstr.append("temp");
		String pathtemp=pathtempstr.toString(); 
		//end   

		JobConf conf = new JobConf(hw2.class);
		conf.setJobName("TriangleFindPhase1");
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(Text.class);

		conf.setMapperClass(Map.class);
//		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);
 	
		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);
 	
		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(pathtemp));
		JobClient.runJob(conf);

////////////////////////	The second mapreduce
		JobConf confnext=new JobConf(hw2.class);
		confnext.setJobName("TriangleFindPhase2");
		confnext.setOutputKeyClass(Text.class);
		confnext.setOutputValueClass(IntWritable.class);

		confnext.setMapperClass(Map1.class);
//		confnext.setCombinerClass(Reduce1.class);
		confnext.setReducerClass(Reduce1.class);
 	
		confnext.setInputFormat(TextInputFormat.class);
		confnext.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(confnext, new Path(pathtemp,"part-00000")); //read data from the temp(Output file of the first mapreduce)
		FileOutputFormat.setOutputPath(confnext, new Path(args[1]));
		JobClient.runJob(confnext);

/*		Job job1=new Job(conf);
		Job job2=new Job(confnext);
		
        JobControl jbctrl = new JobControl("TriangelCount");
        jbctrl.addJob(job1);
        jbctrl.addJob(job2);
        job2.addDependingJob(job1);

        Thread thread = new Thread(jbctrl);
        thread.start();
        while(!jbctrl.allFinished()){
                thread.sleep(1000);
        }
        jbctrl.stop(); */
	}
}