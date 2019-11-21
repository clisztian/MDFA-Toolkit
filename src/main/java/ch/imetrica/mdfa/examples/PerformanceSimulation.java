package ch.imetrica.mdfa.examples;

import java.text.DecimalFormat;
import java.util.Random;

import ch.imetrica.mdfa.market.SimulateStrategy;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
 

public class PerformanceSimulation extends Application {
     
	double[] kpi;
    int resolution = 100;
    int group[] = new int[resolution];
    int n_simulations = 2000; 
    double max,min;
	private double[] buckets;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
         
        prepareData();
        groupData();
         
        DecimalFormat df = new DecimalFormat("#.##");
        Label labelInfo = new Label();
        labelInfo.setText(
            "KPI: Sharpe Ratio");
         
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> barChart = 
            new BarChart<>(xAxis,yAxis);
        barChart.setCategoryGap(0);
        barChart.setBarGap(0);
         
        xAxis.setLabel("Range");       
        yAxis.setLabel("Population");
         
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("KPI Sharpe Ratio Distribution");
        
        for(int k = 0; k < resolution; k++) {     
        	
        	System.out.println(df.format(buckets[k]) + " " + group[k]);
        	series1.getData().add(new XYChart.Data(df.format(buckets[k]), group[k]));	
        }
                 
        barChart.getData().addAll(series1);
         
        VBox vBox = new VBox();
        vBox.getChildren().addAll(labelInfo, barChart);
         
        StackPane root = new StackPane();
        root.getChildren().add(vBox);
         
        Scene scene = new Scene(root, 800, 450);
        scene.getStylesheets().add("css/WhiteOnBlack.css");
        primaryStage.setTitle("BO Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
     
    //generate dummy random data
    private void prepareData() throws Exception{
 
    	min = Double.MAX_VALUE;
    	max = Double.MIN_VALUE;
	
		kpi = new double[n_simulations];
		
		for(int i = 0; i < n_simulations; i++) {
			
			SimulateStrategy simulator = new SimulateStrategy("data/CEVA.SW.daily.csv", "Bobo", 3, .5f, false);
			
			float[] strategy = new float[] {.5f, .3f, .1f};
			simulator.simulate(strategy);	
			
			kpi[i] = simulator.getKPI().getSharpeRatio();
			
			if(kpi[i] < min) {
				min = kpi[i];
			}
			else if(kpi[i] > max) {
				max = kpi[i];
			}
			
			simulator.closeMarket();
		}
    }
     
    //count data population in groups
    private void groupData() {
    	
    	group = new int[resolution];
        
    	buckets = new double[resolution];
    	double delta = (max - min)/(double)resolution;
    	
    	
    	buckets[0] = min;
    	for(int i = 1; i < resolution; i++) {
    		buckets[i] = buckets[i-1] + delta;
    	}
    	
        for(int i=0; i<n_simulations; i++) {	
          for(int k = 1; k < resolution; k++) {
        	  
        	  if(kpi[i] <= buckets[k]) {
        		  group[k-1]++;
        		  break;
        	  }  
          }
        }        
    }
     
}