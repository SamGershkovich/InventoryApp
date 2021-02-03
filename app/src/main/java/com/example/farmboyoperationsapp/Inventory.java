/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLOutput;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class Inventory extends Fragment {

    ArrayList<Item> items = new ArrayList<>();

    //list of indexes of items that came from search
    ArrayList<Integer> searchItemIndexes = new ArrayList<>();

    //position in the list of indexes
    int searchItemIndex = 0;

    MainActivity main;
    EditItem editItemPage;
    PriceList priceList;
    PrintVerification printVerificationPage;

    TextView resultText;
    TextView nameText;
    TextView codeText;
    TextView weightText;
    TextView quantityText;
    TextView pricePoundText;
    TextView priceKiloText;
    TextView progress;
    TextView frontAmountText;
    TextView backAmountText;
    TextInputEditText inputText;

    ConstraintLayout contentContainer;

    String pdfText;
    String[]lines;

    String searchQuery;
    boolean queryFound;

    String inventoryLog;

    boolean verifying = false;

    public Inventory(MainActivity main, EditItem editItemPage, PrintVerification printVerificationPage) throws IOException {
        this.main = main;
        this.editItemPage = editItemPage;
        this.printVerificationPage = printVerificationPage;

        printVerificationPage.inventoryPage = this;

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App");
            if (!root.exists()) {
                root.mkdir();
            }

            File logRoot = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App/logs");
            File path;

            if (!root.exists()) {
                logRoot.mkdir();
            }

            path = new File(logRoot, "inventory_log.txt");

            if (!path.exists()) {
                FileWriter writer = new FileWriter(path);
                writer.flush();
                writer.close();
                //main.logWriter.append("inventory log created\n");

                //Toast.makeText(main, "Inventory Log Created", Toast.LENGTH_SHORT).show();
            } else {
                //main.logWriter.append("inventory log found\n");

                //Toast.makeText(main, "Inventory Log Found", Toast.LENGTH_SHORT).show();
            }

            inventoryLog = path.getAbsolutePath();

        } catch (IOException e) {
            e.printStackTrace();
            //main.logWriter.append(e.getStackTrace() + "\n");

        }
    }

    public void setPriceList(PriceList list){
        priceList = list;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inventory, container, false);
        return view;
    }

    public void GenerateInventoryDatabase(){
        setAllGUI();
        try {
            loadPDF();
        } catch (IOException e) {
            e.printStackTrace();
        }
        generateList();
        inputText.setText("");
        searchInventory("");
    }

    public void loadPDF() throws IOException {

        PdfReader reader = null;
        pdfText = "";
        items.clear();

        BufferedReader br = new BufferedReader(new FileReader("/storage/emulated/0/Farmboy Operations App/saved_locations/inventory_location.txt"));
        String location = br.readLine();
        br.close();
        if(location == ""){
            Toast.makeText(main, "Inventory pdf location invalid", Toast.LENGTH_LONG).show();
        }
        else {
            if (!new File(location).exists()) {
                Toast.makeText(main, "Inventory pdf does not exist!!", Toast.LENGTH_SHORT).show();
                contentContainer.setVisibility(View.INVISIBLE);
            } else {
                contentContainer.setVisibility(View.VISIBLE);

                reader = new PdfReader(location);

                int n = reader.getNumberOfPages();
                for (int i = 0; i < n; i++) {
                    pdfText += PdfTextExtractor.getTextFromPage(reader, i + 1).trim() + "\n";
                }
                reader.close();
                lines = pdfText.split("\n", -1);
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
            frontAmountText = getView().findViewById(R.id.frontAmountText);
            backAmountText = getView().findViewById(R.id.backAmountText);
            inputText = getView().findViewById(R.id.invInputText);
            contentContainer = getView().findViewById(R.id.invContentContainer);
        }
    }

    public void generateList(){
        if (lines != null && lines.length > 0)
        {
            //loop through every line on list(each line = 1 item)
            for (int i = 0; i < lines.length; i++) {

                String category = "",
                        code = "",
                        name = "",
                        weight = "",
                        quantity = "",
                        pricePerPound = "",
                        pricePerKilo = "";

                if(lines[i].contains("Page") || lines[i].contains("Description") || lines[i].contains("Printable")){
                    continue;
                }

                int count = 0;

                String[] words = lines[i].split(" ");

                if(lines[i].contains("$")) {
                    for (int k = 0; k < lines[i].length(); k++) {
                        if (lines[i].charAt(k) == '$')
                            count++;
                    }
                }
                if(words.length > 2) {
                    int lastWordIndex = words.length - 1;

                    weight = words[lastWordIndex];

                    lastWordIndex--;

                    pricePerKilo = words[lastWordIndex];

                    lastWordIndex--;

                    if (count == 2) {
                        pricePerPound = words[lastWordIndex];
                        lastWordIndex--;
                    }
                    if ((words[lastWordIndex] == "oz"
                            || words[lastWordIndex] == "g"
                            || words[lastWordIndex] == "lb"
                            || words[lastWordIndex] == "pint"
                            || words[lastWordIndex] == "kg"
                            || words[lastWordIndex] == "Lb"
                            || words[lastWordIndex].contains("lbs")
                            || words[lastWordIndex].contains("-"))) {
                        lastWordIndex--;
                        try {
                            Double.parseDouble(words[lastWordIndex]);
                            lastWordIndex--;
                        } catch (Exception e) {
                        }
                    } 

                    for (int j = lastWordIndex; j >= 0; j--) {
                        if (!words[j].contains("*")) {
                            if (name == "") {
                                name += words[j];
                            } else {
                                name = words[j] + " " + name;
                            }
                        }
                    }
                }
                if(name.contains("Shredded")){
                    pricePerPound = pricePerKilo;
                    pricePerKilo = "";
                }
                Item item = new Item(category, code, name, weight, quantity, pricePerPound, pricePerKilo);
                if(item.getWeight() != "") {
                    items.add(item);
                }
            }

            try {
                loadInventoryData();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            /*
            FileWriter writer = null;
            try {
                writer = new FileWriter("/storage/emulated/0/Farmboy Operations App/data/parsed inv pdf.txt", true);
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
                    searchInventory(searchQuery);
                }
                break;
        }
    }

    public void loadInventoryData() throws IOException, ClassNotFoundException {
        File path = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/inventory_log.txt");

        FileInputStream fis = new FileInputStream(path);
        ObjectInputStream ois = new ObjectInputStream(fis);
        items = (ArrayList<Item>) ois.readObject();
        ois.close();

        /*
        ArrayList<String>logLines = new ArrayList<>();

        if(path.exists()){
            FileReader reader = new FileReader(path);
            BufferedReader br = new BufferedReader(reader);
            String currLine;
            while((currLine = br.readLine()) != null){
                logLines.add(currLine);
            }
            reader.close();
            br.close();

            for(String line: logLines) {
                String[] itemInfo = line.split("_");

                for(Item item: items){
                   if(itemInfo[0].contains(item.getName()) && itemInfo[0].length() == item.getName().length()){

                       if(itemInfo[1].contains("P")){
                           item.setPerpetualFront(true);

                       }else {
                           item.setPerpetualFront(false);
                           item.setFrontAmount(Double.parseDouble(itemInfo[1]));
                       }

                       if(itemInfo[2].contains("P")){
                           item.setPerpetualBack(true);
                       }else {
                           item.setPerpetualBack(false);
                           item.setBackAmount(Double.parseDouble(itemInfo[2]));
                       }
                   }
                }
            }
        }*/
    }

    @SuppressLint("SetTextI18n")
    public void searchInventory(String query) {
        CheckBox organicButton = getView().findViewById(R.id.organic);

        if(query == ""){
            inputText = getView().findViewById(R.id.invInputText);
            if(inputText.getText().toString() != "") {
                query = inputText.getText().toString();
            }
            else{
                Toast.makeText(main, "Nothing to search", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if(organicButton.isChecked()){
            query = "org "+ query;
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

        TextView searchResult = getView().findViewById(R.id.searchResult);

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
            searchResult.setText(searchItemIndexes.size() + " result(s) found");
            progress.setText("1/" +searchItemIndexes.size());
        }
        searchItemIndex = 0;
        populateItemInfo();
    }

    public void editItem(){
        if (editItemPage.inventory == null) {
            editItemPage.setInventory(this);
        }
        editItemPage.setItem(items.get(searchItemIndexes.get(searchItemIndex)));
    }

    public void setItemAmount(double front, double back, Item editedItem) throws IOException {
        Item item = editedItem;

        item.setFrontAmount(front);
        item.setBackAmount(back);
        populateItemInfo();

        updateInventoryLog();

        /*
        FileWriter writer = new FileWriter(inventoryLog, true);

        ArrayList<String>lines = new ArrayList<>();

        File path = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/inventory_log.txt");

        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
        String currLine;
        while((currLine = br.readLine()) != null){
            lines.add(currLine);
        }

        reader.close();
        br.close();

        if(lines.size() > 0) {
            String linesToWrite = "";

            for (String logLine : lines) {

                if (!logLine.contains(item.getName())) {
                    linesToWrite += logLine + "\n";
                }
            }
            path = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/inventory_log.txt");
            writer = new FileWriter(path);
            writer.write(linesToWrite);
            writer.flush();
        }

        String saveAmountFront;
        String saveAmountBack;
        if(item.isPerpetualFront()){
            saveAmountFront = "P";
        }
        else{
            saveAmountFront = String.valueOf(item.getFrontAmount());
        }
        if(item.isPerpetualBack()){
            saveAmountBack = "P";
        }else{
            saveAmountBack = String.valueOf(item.getBackAmount());
        }

        writer.append(item.getName() + "_" + saveAmountFront + "_" + saveAmountBack + "\n");
        writer.flush();
        writer.close();*/
    }

    public void updateInventoryLog() throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/inventory_log.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(items);
        oos.close();
    }

    public void verify(){
        printVerificationPage.findUncounted(items);
    }

    public void printInventory() throws IOException {

        verifying = false;

        File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App");

        if (!root.exists()) {
            root.mkdir();
        }


        File path = new File(root, "inventory_" + getDate() + ".txt");
        if(path.exists()){
            path.delete();
        }

        FileWriter writer = new FileWriter(path.getAbsolutePath(), true);

        String separator = "";
        for(int i = 0; i < 67; i++){
            separator+="-";
        }

        writer.append("Inventory for: " + getDate() +"\tCounted by: "+ main.employeeName + "\n\n\n" + separator + "\n" +
                "|                    Item Name                    | Front |  Back |\n" + separator + "\n");

        for(Item item: items) {

            int textLength;
            int spaceRemainder;

            textLength = item.getName().length();
            spaceRemainder = 50 - (5 + textLength);

            writer.append("|    " + item.getName());
            for (int i = 0; i < spaceRemainder; i++) {
                writer.append(" ");
            }


            if(item.isPerpetualFront()){
                textLength = 2;
                writer.append("|   " + "P");
            }
            else {
                if(main.countBy){
                    double amount = item.getFrontAmount();

                    if(main.roundTo) {
                        amount = (double)((int)(amount / 0.25 + 0.5)) * 0.25;
                        textLength = String.valueOf(amount).length();
                        writer.append("|  " + amount);
                    }else{
                        if(amount > 0 && amount < 0.25){
                            amount = 0.25;
                        }
                        else{
                            amount = (double)((int)(amount / 0.25 + 0.5)) * 0.25;
                        }
                        textLength = String.valueOf(amount).length();
                        writer.append("|  " + amount);
                    }
                }else{
                    textLength = String.valueOf(item.getFrontAmount()).length();
                    writer.append("|  " + item.getFrontAmount());
                }
            }

            spaceRemainder = 8 - (3 + textLength);

            for (int i = 0; i < spaceRemainder; i++) {
                writer.append(" ");
            }

            if(item.isPerpetualBack()){
                textLength = 2;
                writer.append("|   " + "P");
            }
            else {
                if(main.countBy){
                    double amount = item.getBackAmount();

                    if(main.roundTo) {
                        amount = (double)((int)(amount / 0.25 + 0.5)) * 0.25;
                        textLength = String.valueOf(amount).length();
                        writer.append("|  " + amount);
                    }else{
                        if(amount > 0 && amount < 0.25){
                            amount = 0.25;
                        }
                        else{
                            amount = (double)((int)(amount / 0.25 + 0.5)) * 0.25;
                        }
                        textLength = String.valueOf(amount).length();
                        writer.append("|  " + amount);
                    }
                }else{
                    textLength = String.valueOf(item.getBackAmount()).length();
                    writer.append("|  " + item.getBackAmount());
                }
            }

            spaceRemainder = 8 - (3 + textLength);

            for (int i = 0; i < spaceRemainder; i++) {
                writer.append(" ");
            }

            writer.append("|\n" + separator + "\n");
        }
        writer.flush();
        writer.close();
        Toast.makeText(main, "Inventory Printed", Toast.LENGTH_SHORT).show();
    }

    public void finalizeInventory(){
        try {
            File invLog = new File(inventoryLog);
            invLog.renameTo(new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/invLOG_" + getDate() + ".txt"));
            Toast.makeText(main, "Finalized inventroy", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(main, "Finalized failed", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
    }

    public String getDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat nameDateFormat = new SimpleDateFormat("EEE MMMM dd, yyyy");
        return nameDateFormat.format(calendar.getTime());
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
                //System.out.println(item.isPerpetualFront());
                //System.out.println(item.isPerpetualBack());
                if(item.isPerpetualFront()){
                    frontAmountText.setText("Front: P");
                }
                else {
                    frontAmountText.setText("Front: " + item.getFrontAmount());
                }
                if(item.isPerpetualBack()){
                    backAmountText.setText("Back: P");
                }
                else {
                    backAmountText.setText("Back: " + item.getBackAmount());
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

