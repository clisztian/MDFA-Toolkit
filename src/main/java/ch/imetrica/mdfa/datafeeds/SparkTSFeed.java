package ch.imetrica.mdfa.datafeeds;

import java.sql.Timestamp;
import java.util.Comparator;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;


import com.cloudera.sparkts.BusinessDayFrequency;
import com.cloudera.sparkts.DateTimeIndex;
import com.cloudera.sparkts.api.java.DateTimeIndexFactory;
import com.cloudera.sparkts.api.java.JavaTimeSeriesRDD;
import com.cloudera.sparkts.api.java.JavaTimeSeriesRDDFactory;
import com.cloudera.sparkts.stats.TimeSeriesStatisticalTests;

import scala.Tuple2;

public class SparkTSFeed {

	//"Index","Open","High","Low","Close","Volume","Adjusted"
	
	public static Dataset<Row> loadObservations(JavaSparkContext sparkContext, 
			  SQLContext sqlContext,
		      String path, String regex) {
		
	    JavaRDD<Row> rowRdd = sparkContext.textFile(path).map((String line) -> {
	    	
	        String[] tokens = line.split(regex);	        
	        String[] date = tokens[0].split("[-]+");
	        
	        ZonedDateTime dt = ZonedDateTime.of(Integer.parseInt(date[0]),
	            Integer.parseInt(date[1]), Integer.parseInt(date[2]), 0, 0, 0, 0,
	            ZoneId.systemDefault());
	        
	        String[] symbol = path.split("[.]+");
	        
	        double open = Double.parseDouble(tokens[1]);
	        double high = Double.parseDouble(tokens[2]);
	        double low = Double.parseDouble(tokens[3]);
	        double close = Double.parseDouble(tokens[4]);
	        double volume = Double.parseDouble(tokens[5]);

	        double[] bar = {open, high, low, close, volume};
	        
	        return RowFactory.create(Timestamp.from(dt.toInstant()), symbol, open);
	    });
	    List<StructField> fields = new ArrayList();
	    fields.add(DataTypes.createStructField("timestamp", DataTypes.TimestampType, true));
	    fields.add(DataTypes.createStructField("symbol", DataTypes.StringType, true));
	    //fields.add(DataTypes.createStructField("bar", DataTypes.createArrayType(DataTypes.DoubleType), true));
	    fields.add(DataTypes.createStructField("price", DataTypes.DoubleType, true));
	    
	    StructType schema = DataTypes.createStructType(fields);
	    return sqlContext.createDataFrame(rowRdd, schema);
		
			
	}
	
	
	public static void main(String[] args) {
		
	    SparkConf conf = new SparkConf().setAppName("Spark-TS Ticker Example").setMaster("local");
	    conf.set("spark.io.compression.codec", "org.apache.spark.io.LZ4CompressionCodec");
	    JavaSparkContext context = new JavaSparkContext(conf);
	    SQLContext sqlContext = new SQLContext(context);

	    Dataset<Row> tickerObs = loadObservations(context, sqlContext, "data/sparkTest/AAPL.daily.csv", "[,]+");

	    // Create an daily DateTimeIndex over August and September 2015
	    ZoneId zone = ZoneId.systemDefault();
	    DateTimeIndex dtIndex = DateTimeIndexFactory.uniformFromInterval(
	        ZonedDateTime.of(LocalDateTime.parse("2015-08-03T00:00:00"), zone),
	        ZonedDateTime.of(LocalDateTime.parse("2015-09-22T00:00:00"), zone),
	        new BusinessDayFrequency(1, 0));

	    // Align the ticker data on the DateTimeIndex to create a TimeSeriesRDD
	    JavaTimeSeriesRDD tickerTsrdd = JavaTimeSeriesRDDFactory.timeSeriesRDDFromObservations(
	        dtIndex, tickerObs, "timestamp", "symbol", "price");

	    // Cache it in memory
	    tickerTsrdd.cache();

	    // Count the number of series (number of symbols)
	    System.out.println(tickerTsrdd.count());

	    // Impute missing values using linear interpolation
	    JavaTimeSeriesRDD<String> filled = tickerTsrdd.fill("linear");

	    // Compute return rates
	    JavaTimeSeriesRDD<String> returnRates = filled.returnRates();

	    // Compute Durbin-Watson stats for each series
	    JavaPairRDD<String, Double> dwStats = returnRates.mapValues(
	        (Vector x) -> TimeSeriesStatisticalTests.dwtest(x)
	    );

	    class StatsComparator implements Comparator<Tuple2<String,Double>>, java.io.Serializable {
	        public int compare(Tuple2<String, Double> a, Tuple2<String, Double> b) {
	            return a._2.compareTo(b._2);
	        }
	    }

	    System.out.println(dwStats.min(new StatsComparator()));
	    System.out.println(dwStats.max(new StatsComparator()));
	  }
	
	
	
}
