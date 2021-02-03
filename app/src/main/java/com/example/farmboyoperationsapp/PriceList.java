/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class PriceList extends Fragment {

    ArrayList<Item> items = new ArrayList<>();
    ArrayList<Integer> searchItemIndexes = new ArrayList<>();

    int searchItemIndex = 0;

    MainActivity main;
    Inventory inventory;

    String searchQuery;
    boolean queryFound;
    int lineCount;
    String[] lines;
    String pdfText = "";

    ConstraintLayout contentContainer;

    TextView resultText;
    TextView nameText;
    TextView codeText;
    TextView weightText;
    TextView quantityText;
    TextView pricePoundText;
    TextView priceKiloText;
    TextView progress;
    TextInputEditText inputText;

    public PriceList(MainActivity main, Inventory inv) throws IOException {
        this.main = main;
        inventory = inv;
        inventory.setPriceList(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_price_list, container, false);

        return view;
    }

    public void GenerateItemDatabase(){
        items.clear();
        main.items.clear();
        pdfText = "";

        setAllGUI();

        try {
            loadPDF();
        } catch (IOException e) {
            e.printStackTrace();
        }

        generateTable();

        inputText.setText("");

        searchPriceList("");
    }

    public void loadPDF() throws IOException {

        PdfReader reader = null;

        BufferedReader br = new BufferedReader(new FileReader("/storage/emulated/0/Farmboy Operations App/saved_locations/pricelist_location.txt"));
        String location = br.readLine();
        br.close();
        if(location == ""){
            Toast.makeText(main, "Pricelist pdf location invalid", Toast.LENGTH_LONG).show();
        }
        else {
            if (!new File(location).exists()) {
                Toast.makeText(main, "Price list pdf does not exist!!", Toast.LENGTH_SHORT).show();
                contentContainer.setVisibility(View.INVISIBLE);
            } else {
                contentContainer.setVisibility(View.VISIBLE);

                reader = new PdfReader(location);

                int n = reader.getNumberOfPages();
                for (int i = 0; i < n; i++) {
                    String pageText = PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n";
                    String[] pageLines = pageText.split("\n");
                    pageText = "";
                    for (int j = 0; j < pageLines.length; j++) {
                        try {
                            Integer.parseInt(pageLines[j].substring(0, pageLines[j].indexOf(' ')));
                            pageText += pageLines[j] + "\n";
                        } catch (Exception e) {
                        }
                    }
                    pdfText += pageText;
                }
                reader.close();
                lines = pdfText.split("\n", -1);
                lineCount = lines.length - 1;
            }
        }
    }

    public void setAllGUI(){
        if(nameText == null) {
            resultText = getView().findViewById(R.id.txvResult);
            nameText = getView().findViewById(R.id.itemName);
            codeText = getView().findViewById(R.id.itemCode);
            weightText = getView().findViewById(R.id.itemWeight);
            quantityText = getView().findViewById(R.id.itemQuantity);
            pricePoundText = getView().findViewById(R.id.itemPricePound);
            priceKiloText = getView().findViewById(R.id.itemPriceKilo);
            progress = getView().findViewById(R.id.progress);
            inputText = getView().findViewById(R.id.inputText);
            contentContainer = getView().findViewById(R.id.priceListContentContainer);

        }
    }

    public void generateTable() {

       /*if (lineCount > 0) {
            TableLayout table = rootView.findViewById(R.id.table);
            for (int i = 0; i < lineCount; i++) {
                TableRow newRow = new TableRow(main);
                table.addView(newRow);
                String[] words = lines[i].split(" ");

                boolean numeric = false;

                for (int j = 0; j < words.length; j++) {
                    try {
                        Double.parseDouble(words[j]);
                        if (!numeric)
                            switched = true;
                        else {
                            switched = false;
                        }
                        numeric = true;

                    } catch (Exception e) {
                        if (numeric)
                            switched = true;
                        else {
                            switched = false;
                        }
                        numeric = false;
                    }

                    if (newRow.getChildCount() == 5 && numeric) {
                        TextView spacer = new TextView(main);
                        spacer.setTextSize(14);
                        spacer.setBackgroundResource(R.drawable.textlines);
                        spacer.setText("        ");
                        spacer.setPadding(20, 20, 20, 20);
                        newRow.addView(spacer);

                        TextView column = new TextView(main);
                        column.setTextSize(14);
                        column.setBackgroundResource(R.drawable.textlines);
                        column.setText(words[j]);
                        column.setPadding(20, 20, 20, 20);
                        newRow.addView(column);
                    }
                    else if (numeric || words[j].contains("$") || switched) {
                        int count = 0;
                        for (int k = 0; k < lines[i].length(); k++) {
                            if (lines[i].charAt(k) == '$')
                                count++;
                        }
                        if (j < words.length - 1 && j > 1 && words[j + 1].contains("oz") && numeric) {
                            TextView column = (TextView) newRow.getChildAt(newRow.getChildCount() - 1);
                            column.setText(column.getText() + " " + words[j] + "oz");
                            j++;
                        }
                        else {
                            if (words[j].contains("$") && count == 1) {
                                TextView column = new TextView(main);
                                column.setTextSize(14);
                                column.setBackgroundResource(R.drawable.textlines);
                                column.setText(" ");
                                column.setPadding(20, 20, 20, 20);
                                newRow.addView(column);
                            }
                            TextView column = new TextView(main);
                            column.setTextSize(14);
                            column.setBackgroundResource(R.drawable.textlines);
                            column.setText(words[j]);
                            column.setPadding(20, 20, 20, 20);
                            newRow.addView(column);
                        }
                    }
                    else {
                        if (newRow.getChildCount() == 0) {
                            TextView column = new TextView(main);
                            column.setTextSize(14);
                            column.setBackgroundResource(R.drawable.textlines);
                            column.setText(words[j]);
                            column.setPadding(20, 20, 20, 20);
                            newRow.addView(column);
                        }

                        TextView column = (TextView) newRow.getChildAt(newRow.getChildCount() - 1);
                        if (column.getText() == null) {
                            column.setText(words[j]);
                        } else {
                            column.setText(column.getText() + " " + words[j]);
                        }
                    }
                }

                if (i % 2 == 0) {
                    newRow.setBackgroundColor(Color.WHITE);
                } else {
                    newRow.setBackgroundColor(Color.LTGRAY);
                }
            }
        }*///Table Generation

        String truncatedName = "";

        //Item array list generation
        if (lineCount > 0)
        {
           //loop through every line on list(each line = 1 item)
           for (int i = 0; i < lineCount; i++) {

               int columnNum = 1;//the current column of the price list we are at

               String category = "",
                       code = "",
                       name = "",
                       weight = "",
                       quantity = "",
                       pricePerPound = "",
                       pricePerKilo = "";
            /*
               if(lines[i].contains("Case Wt") || lines[i].contains("Sold by Price") || lines[i].contains("Qty KG") || lines[i].contains("Price List") || lines[i].contains("Harvard Square") || lines[i].contains("Department")){
                   continue;
               }
               try{
                   Integer.parseInt(lines[i].substring(0, 1));

               }catch (Exception e){
                   truncatedName = lines[i];
                   continue;
               }
*/

               int count = 0;

               String[] words = lines[i].split(" ");

               boolean numeric = false;
               boolean switched = false;

               if(lines[i].contains("$")) {
                   for (int k = 0; k < lines[i].length(); k++) {
                       if (lines[i].charAt(k) == '$')
                           count++;
                   }
               }

               //loop through every word in the line
               for (int j = 1; j < words.length; j++) {
                   try {
                        Double.parseDouble(words[j]);
                        if (!numeric)
                            switched = true;
                        else {
                            switched = false;
                        }
                        numeric = true;

                    } catch (Exception e) {
                        if (numeric)
                            switched = true;
                        else {
                            switched = false;
                        }
                        numeric = false;
                    }

                    if(numeric || words[j].contains("$") || switched){

                       // if (!(j < words.length - 1 && j > 1 && words[j + 1].contains("oz") && numeric)) {
                            columnNum++;
                       // }
                    }
                    if(words[j] == "qty"){
                        columnNum++;
                    }
                    try{
                        switch (columnNum){
                            case 1:
                                if(category ==  "") {
                                    category += words[j];
                                }
                                else{
                                    category += " " + words[j];
                                }
                                break;
                            case 2:
                                code += words[j];
                                break;
                            case 3:
                              /*  if(truncatedName != ""){
                                    if(!numeric){
                                        items.get(items.size()-1).setName(items.get(items.size()-1).getName() + " " + truncatedName);
                                    }
                                    else {
                                        name = truncatedName;
                                    }
                                    truncatedName = "";
                                    columnNum++;
                                    weight = words[j];
                                }*/
                               // else {
                                    if(words[j].contains("berries")){
                                        words[j] = words[j].substring(0, words[j].indexOf("ies")) + "y";
                                    }

                                    if (name == "") {
                                        name += words[j];
                                    } else {
                                        name += " " + words[j];
                                    }
                                //}
                                break;
                            case 4:
                                weight = words[j];
                                break;
                            case 5:
                                if(numeric){
                                    quantity = words[j];
                                }
                                else{
                                    quantity = words[j + 1];
                                    j++;
                                }
                                break;
                            case 6:
                                if(count == 1){
                                    pricePerPound = "";
                                    pricePerKilo = words[j];
                                }
                                else if(count == 2){
                                    pricePerPound = words[j];
                                    j++;
                                    pricePerKilo = words[j];
                                }
                                break;
                        }
                    }catch(Exception e){
                        System.out.println("FAILED TO CREATE ITEM FROM LINE: " + i);
                    }
                }
               Item item = new Item(category, code, name, weight, quantity, pricePerPound, pricePerKilo);
               items.add(item);
            }
           main.items = items;
           /*
            FileWriter writer = null;
            try {
                writer = new FileWriter("/storage/emulated/0/Farmboy Operations App/data/parsed pdf.txt", true);
                for(Item item: items){
                    writer.write(item + "\n");
                }
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*///write item list to file
        }
    }

    public void getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent. LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if(intent.resolveActivity(main.getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else{
            Toast.makeText(main, "Your Device Does Not Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        resultText = getView().findViewById(R.id.txvResult);

        switch(requestCode){
            case 10:
                if(resultCode == main.RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    resultText.setText("Searching for: " + result.get(0));
                    searchQuery = result.get(0);
                    searchPriceList(searchQuery);
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void searchPriceList(String query) {
        if(query == ""){
            if(inputText.getText().toString() != "") {
                query = inputText.getText().toString();
            }
            else{
                Toast.makeText(main, "Nothing to search", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        resultText.setText("Searching for: " + query);
        queryFound = false;
        searchItemIndexes.clear();
        if(query.contains("berries")){
            query = query.substring(0, query.indexOf("ies")) + "y";
        }
        if(query.endsWith("es")){
            query = query.substring(0, query.length()-2);
        }
        else if(query.endsWith("s")){
            query = query.substring(0, query.length()-1);
        }

        //TableLayout table = getView().findViewById(R.id.table);
        //ScrollView scrollView = getView().findViewById(R.id.scrollView);
        //HorizontalScrollView horizScroll = getView().findViewById(R.id.horizScroll);

        TextView searchResult = getView().findViewById(R.id.searchResult);

        /*for (int i = 0; i < table.getChildCount(); i++) {
            if(queryFound){
                break;
            }
            TableRow tempRow = (TableRow) table.getChildAt(i);
            for (int j = 0; j < tempRow.getChildCount(); j++) {

                TextView cell = (TextView) tempRow.getChildAt(j);
                String cellText = cell.getText().toString();

                boolean fullQueryFound = true;

                String[] queryWords = query.split(" ");
                for(int k = 0; k < queryWords.length; k++){
                    if (Pattern.compile(Pattern.quote(queryWords[k]), Pattern.CASE_INSENSITIVE).matcher(cellText).find() == false) {
                        fullQueryFound = false;
                    }
                }
                if (fullQueryFound) {
                    searchResult.setText("Found query in: Row " + i + " | Cell: " + j);
                    scrollView.scrollTo(0, 90*i - 240);
                    horizScroll.scrollTo(340*j - 240, 0);
                    cell.setBackgroundColor(Color.YELLOW);
                    queryFound = true;
                    break;
                }
            }
        }*///Table Search

        for(Item item: items){
            String name = item.getName();
            boolean fullQueryFound = true;
            String[] queryWords = query.split(" ");

            for(int k = 0; k < queryWords.length; k++){
                if (!Pattern.compile(Pattern.quote(queryWords[k]), Pattern.CASE_INSENSITIVE).matcher(name).find()) {
                    fullQueryFound = false;
                }
            }
            if(fullQueryFound){
                searchItemIndexes.add(items.indexOf(item));
            }
        }
        if (searchItemIndexes.size() == 0) {
            searchResult.setText("Item not found");
        }
        else{
            searchResult.setText("Item found - " + searchItemIndexes.size() + " result(s) found");
            progress.setText("1/" +searchItemIndexes.size());
        }
        searchItemIndex = 0;
        populateItemInfo();
    }

    @SuppressLint("SetTextI18n")
    public void populateItemInfo(){
        try {
            if (searchItemIndexes.size() > 0) {
                Item item = items.get(searchItemIndexes.get(searchItemIndex));
                if(item.getName().length() > 20){
                    nameText.setTextSize(18);
                }
                else{
                    nameText.setTextSize(30);
                }
                nameText.setText(item.getName());
                codeText.setText(item.getCode());
                weightText.setText(item.getWeight());
                quantityText.setText("Quantity: " + item.getQuantity());

                if (item.getPricePerPound() == "") {
                    pricePoundText.setText("");
                    priceKiloText.setText(item.getPricePerKilo() + "ea");
                } else {
                    pricePoundText.setText(item.getPricePerPound() + "/lb");
                    priceKiloText.setText(item.getPricePerKilo() + "/kg");
                }
            }
        }catch(Exception e){
            Toast.makeText(main, "Unkown error occured", Toast.LENGTH_SHORT).show();
        }
    }

    public void NextItem(){
        if(searchItemIndexes.size() > 0) {
            if (searchItemIndex < searchItemIndexes.size() - 1) {
                searchItemIndex++;
                progress.setText((searchItemIndex + 1) + "/" + searchItemIndexes.size());
            } else {
                searchItemIndex = 0;
                progress.setText((searchItemIndex + 1) + "/" + searchItemIndexes.size());
            }
            populateItemInfo();
        }
    }

    public void PrevItem(){
        if(searchItemIndexes.size() > 0) {
            if (searchItemIndex > 0) {
                searchItemIndex--;
                progress.setText((searchItemIndex + 1) + "/" + searchItemIndexes.size());
            } else {
                searchItemIndex = searchItemIndexes.size() - 1;
                progress.setText((searchItemIndex + 1) + "/" + searchItemIndexes.size());
            }
            populateItemInfo();
        }
    }
}
