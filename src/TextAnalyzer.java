//Imports
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.stream.Collectors.*;
import java.util.LinkedHashMap;
//JavaFX Imports
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
//Imports from External Libraries
import org.jsoup.Jsoup;

public class TextAnalyzer extends Application {

	public static void main(String[] args) {
		launch(args);
	}//END MAIN
	
	//Put all the entries in unsorted map into LinkedMap of Object in descending value order
	public Map<String, Integer> SortedWordCount (Map<String, Integer> Unsorted) {
		//Create a Sorted Map
		Map<String, Integer> Sorted = Unsorted
				.entrySet()
				.stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.collect(
						toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
								LinkedHashMap::new));
		return Sorted; 
	}//end SortedWordCount
	
	//Start Application
	@Override
	public void start(Stage primaryStage) throws Exception {
		//Stage setup
		primaryStage.setTitle("Word Occurances GUI");
		
		//Create objects for UI
		Button submitB = new Button();
		Button saveB = new Button();
		Button loadB = new Button();
		TextField submitT = new TextField();
		TextField saveT = new TextField();
		TextArea outputTA = new TextArea();
		Label tipsL = new Label();
		ListView<String> fileList = new ListView<String>();
		HBox top = new HBox();
		VBox left = new VBox();
		VBox right = new VBox();
		VBox center = new VBox();
		BorderPane borderPane = new BorderPane();
		//Create object for Word Occurances
		WebpageWordCount WWC = new WebpageWordCount();
		
		//Text and Options for objects
		submitB.setText("Submit");
		saveB.setText("Save");
		loadB.setText("Load");
		submitT.setPromptText("URL");
		saveT.setPromptText("File Name");
		outputTA.setEditable(false);
		tipsL.setText("Submit: Submit URL to analyze word occurances"
				+ "\n" + "Save: Save results to text file" +
				"\n" + "Load: Load text file");
		tipsL.setWrapText(true);
		
		//Object sizes
		submitB.setPrefWidth(75);
		saveB.setPrefWidth(75);
		loadB.setPrefWidth(75);
		submitT.setPrefWidth(250);
		saveT.setPrefWidth(200);
		outputTA.setPrefSize(250,500);
		tipsL.setMinSize(150, 250);
		fileList.setPrefSize(275,500);
		
		//Object alignments
		top.getChildren().addAll(submitT, submitB, saveT, saveB);
		top.setAlignment(Pos.BASELINE_CENTER);
		center.getChildren().addAll(loadB, tipsL);
		center.setAlignment(Pos.CENTER);
		left.getChildren().add(outputTA);
		right.getChildren().add(fileList);
		borderPane.setTop(top);
		borderPane.setCenter(center);
		borderPane.setLeft(left);
		borderPane.setRight(right);
		
		//Object Padding and spacing
		borderPane.setPadding(new Insets(10, 10, 10, 10));
		left.setPadding(new Insets(10));
		right.setPadding(new Insets(10));
		top.setPadding(new Insets(10));
		top.setSpacing(30);
		center.setPadding(new Insets(10));
		center.setSpacing(10);
		
		
		//Update empty file list
		UpdateFileList(fileList);
		
		//Action Handlers for buttons
		submitB.setOnAction(e -> { 
			if (isValidURL(submitT.getText())) {
				outputTA.clear();
				WWC.setURL(submitT.getText());
				WWC.setWordCount(URLToWordCount(WWC.getURL()));
				outputTA.setText(WordOccurancesIterator(WWC.getWordCount()));
			}//end if
			else
				AlertBox.display("URL Error", "The URL Entered is invalid");
		});
		saveB.setOnAction(e -> {
			TextWriter(WWC.getWordCount(), saveT.getText());
			UpdateFileList(fileList);
			});
		loadB.setOnAction(e -> {
			TextReader(outputTA, fileList.getSelectionModel().getSelectedItem());
			});
		
		Scene scene = new Scene(borderPane, 750, 600);
		primaryStage.setScene(scene);
		primaryStage.show();
	
	}//end start
	
	//Checks URL is valid
	public boolean isValidURL(String url) 
    { 
        /* Try creating a valid URL */
        try { 
            new URL(url).toURI(); 
            return true; 
        } 
          
        // If there was an Exception 
        // while creating URL object 
        catch (Exception e) { 
            return false; 
        } 
    }//end isValidURL
	

	//Read selected file name as a directory for text file and output to text area
	public void TextReader(TextArea output, String filename) {
		File directory = new File(new File("").getAbsolutePath()+"//Word Occurances//");
		if (directory.exists()) {
			try {
				File file = new File(directory + "//" + filename);
				BufferedReader br = new BufferedReader(new FileReader(file)); 
				String st; 
				output.setText("");
				int i = 1;
					while ((st = br.readLine()) != null) {
						output.appendText(i + ". " + st + "\n");
						i++;
					}//end while
				br.close();
				output.selectPositionCaret(output.getLength());
				output.deselect();
			}//end try
			catch (IOException e) {
				e.printStackTrace();
			}//end catch
		}//end if
	}//end TextReader
	
	//Creates text file with file name from text field, if invalid name alert box tells user
	public void TextWriter (Map<String, Integer> Map, String filename) {
		//Initialize Filewriter and Buffer
		try {
			FileWriter fstream = new FileWriter(new File(".").getAbsolutePath()+"//Word Occurances//"+filename+".txt",true);
			BufferedWriter out = new BufferedWriter(fstream);
			//Create Iterator object to iterate over each pair of map in a loop for writing
			Iterator<Entry<String, Integer>> It = Map.entrySet().iterator();
		
			while (It.hasNext()) {
				Map.Entry<String, Integer> Pairs = It.next();
				out.write(Pairs.getKey() + ", " + Pairs.getValue() + "\n");
			}//end while loop
			out.close();
			AlertBox.display("Success", filename + ".txt was saved");
		}//end try
		catch (IOException e) {
			AlertBox.display("File Error", "File name entered is invalid");
		}//end catch
	}//end TextWriter	

	//Check if file list folder exists, if not create, and show files in list
	public void UpdateFileList(ListView<String> fileList) {
		fileList.getItems().clear();
		File directory = new File(new File(".").getAbsolutePath()+"//Word Occurances//");
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				fileList.getItems().add(file.getName());
			}//end for
		}//end if		
	}//end UpdateFileList
	
	//Map the word count to unsorted map
	public static void UnsortedWordCount (String[] SplitText, Map<String, Integer> UnsortedMap) {
		//Check if word exists in map and increase count, if not add word to unsorted map
		int count = 0;
		for (String word:SplitText) { 
			if (!UnsortedMap.containsKey(word))
				UnsortedMap.put(word, 1);
			else {
				count = UnsortedMap.get(word);
				UnsortedMap.put(word, count+1);
				}//end else statement
		}//end for loop
	}//end UnsortedWordCount

	//Takes the WebpageWordCount URL to make an unsorted map of word count
	public Map<String, Integer> URLToWordCount(String URL) {
		
		//Call function and put parsed HTML into string
		String Text = WebpageToString(URL);
		
		//Take the string text remove punctuation and capitalization, and split the string into a String array of words
		String[] SplitText = Text.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+"); //Make regex pattern if used multiple times
		
		//Call function and create unsorted word count map
		Map<String, Integer> tempMap = new HashMap<String, Integer>();
		UnsortedWordCount(SplitText, tempMap);
		
		//Call function and sort the word count map
		return SortedWordCount(tempMap);
		
	}//end URLToWordCount
	
	//Gets HTML from URL, parses tags from HTML with Jsoup and returns result in string
	public String WebpageToString(String page) {
        String HTML = "";
        String inputLine = "";
		
		try {
			//Initialize variables
			URL url = new URL(page);
            URLConnection conn = url.openConnection();
            // open the stream and put it into BufferedReader
            BufferedReader br = new BufferedReader(
                               new InputStreamReader(conn.getInputStream()));
            
            //Read in each line of webpage into String
            while ((inputLine = br.readLine()) != null) {
            	HTML = HTML.concat(inputLine);
            }//end while loop
            br.close();
            //Parse HTML tags
            HTML = Jsoup.parse(HTML).text();
        }//end try
		catch (MalformedURLException e) {
            e.printStackTrace();
        }//end catch 
		catch (IOException e) {
            e.printStackTrace();
        }//end catch

		return HTML;
	}//end WebpageToString

	//Iteratates through all words in Map and returns string of the Map
	public String WordOccurancesIterator(Map<String, Integer> SortedMap) {
		Iterator<Map.Entry<String, Integer>> entries = SortedMap.entrySet().iterator();
		int i = 0;
		String result = "";
		
		while (entries.hasNext() && i < SortedMap.size()) {
		    Map.Entry<String, Integer> entry = entries.next();
		    result = result.concat((i+1 + ". " + entry.getKey() + ", " + entry.getValue() + "\n"));
		    i++;
		}//end while loop		
		return result;
	}//end WordOccurancesIterator



}//end TextAnalyzer
