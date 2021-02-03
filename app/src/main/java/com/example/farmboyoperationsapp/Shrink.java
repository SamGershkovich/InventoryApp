/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.itextpdf.text.pdf.PdfReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class Shrink extends Fragment {

    MainActivity main;
    Inventory inventory;

    ScrollView scrollView;
    TextView resultText;
    TextView nameText;
    TextView progress;
    TextView frontAmountText;
    TextView backAmountText;
    TextView gramsText;
    EditText shrinkInput;
    EditText modifyInput;
    TextInputEditText inputText;
    ConstraintLayout contentContainer;

    ArrayList<Item> inventoryItems = new ArrayList<>();

    //list of indexes of items that came from search
    ArrayList<Integer> searchItemIndexes = new ArrayList<>();

    //position in the list of indexes
    int searchItemIndex = 0;

    boolean queryFound;

    ArrayList<Item> shrinkItems = new ArrayList<>();

    public Shrink(MainActivity main, Inventory inventory) {
        this.main = main;
        this.inventory = inventory;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_shrink, container, false);
        return fragment;
    }

    public void InitiateShrinkPage(){
        setAllGUI();
        contentContainer.setVisibility(View.INVISIBLE);
        inventoryItems = inventory.items;
        try {
            loadShrinkData();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

       // main.ViewShrink(null);
    }

    public void setAllGUI(){
        if(nameText == null) {
            resultText = getView().findViewById(R.id.txvResult);
            nameText = getView().findViewById(R.id.itemName);
            progress = getView().findViewById(R.id.progress);
            frontAmountText = getView().findViewById(R.id.frontAmountText);
            backAmountText = getView().findViewById(R.id.backAmountText);
            inputText = getView().findViewById(R.id.inputText);
            contentContainer = getView().findViewById(R.id.shrinkContentContainer);
            shrinkInput = getView().findViewById(R.id.shrinkAmountInput);
            modifyInput = getView().findViewById(R.id.modifyShrinkAmount);
            gramsText = getView().findViewById(R.id.gramsText);
            shrinkInput.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        editItemAmount(null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
        }
    }

    public void editItemAmount(TableRow row, String amount) throws IOException {
        if(searchItemIndexes.size() > 0) {
            Item item = inventoryItems.get(searchItemIndexes.get(searchItemIndex));
            if (row == null) {
                try {
                    item.setShrinkAmount(Integer.parseInt(String.valueOf(shrinkInput.getText())));
                    //logShrinkAmount();
                    if (!shrinkItems.contains(item)) {
                        shrinkItems.add(item);
                    }
                } catch (NumberFormatException e) {
                    item.setShrinkAmount(0);
                   /* try {
                        logShrinkAmount();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }*/
                    if (shrinkItems.contains(item)) {
                        shrinkItems.remove(item);
                    }
                    e.printStackTrace();
                }
            } else {
                try {
                    for (Item shrinkItem : shrinkItems) {
                        if (shrinkItem.getName() == ((TextView) row.getChildAt(row.getChildCount() - 1)).getText()) {
                           /* String shrinkAmount = "";
                            shrinkAmount = String.valueOf(amount).replace("\t", "");
                            if (String.valueOf(amount).contains("g")) {
                                shrinkAmount = shrinkAmount.replace("g", "");
                            }
                            if (String.valueOf(amount).contains(",")) {
                                shrinkAmount = shrinkAmount.replace(",", "");
                            }*/
                            shrinkItem.setShrinkAmount(Integer.parseInt(amount));
                        }
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

            }

            inventory.updateInventoryLog();
        }
    }

    public void loadShrinkData() throws IOException, ClassNotFoundException {
        for(Item item: inventoryItems){
            if(item.getShrinkAmount() > 0){
                shrinkItems.add(item);
            }
        }

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
                String[] itemInfo = line.split("/");

                for(Item item: inventoryItems){
                    if((itemInfo[0]).contains(item.getName()) && itemInfo[0].length() == item.getName().length()){
                        item.setShrinkAmount(Integer.parseInt(itemInfo[1]));
                        shrinkItems.add(item);
                    }
                }
            }
        }
        else{
            Toast.makeText(main, "Shrink log not found", Toast.LENGTH_SHORT).show();
        }*/
    }

  /*  public void logShrinkAmount() throws IOException {
        FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/shrink_log.txt");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(shrinkItems);
        oos.close();

        Item item = inventoryItems.get(searchItemIndexes.get(searchItemIndex));
        FileWriter writer = new FileWriter(shrinkLog, true);

        ArrayList<String> lines = new ArrayList<>();

        File path = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/shrink_log.txt");

        FileReader reader = new FileReader(path);
        BufferedReader br = new BufferedReader(reader);
        String currLine;
        while ((currLine = br.readLine()) != null) {
            lines.add(currLine);
        }

        reader.close();
        br.close();

        if (lines.size() > 0) {
            String linesToWrite = "";

            for (String logLine : lines) {

                if (!logLine.contains(item.getName())) {
                    linesToWrite += logLine + "\n";
                }
            }
            path = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/logs/shrink_log.txt");
            writer = new FileWriter(path);
            writer.write(linesToWrite);
            writer.flush();
        }
        if(item.getShrinkAmount() > 0) {
            writer.append(item.getName() + "/" + item.getShrinkAmount() + "\n");
        }
        writer.flush();
        writer.close();

    }*/

    public void getSpeechInput() {
        if(scrollView == null){
            scrollView = getView().findViewById(R.id.shrinkScroller);
        }

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


        switch(requestCode){
            case 10:
                if(resultCode == main.RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    searchInventory(result.get(0));
                }
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    public void searchInventory(String query) {
        contentContainer.setVisibility(View.VISIBLE);

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

        TextView searchResult = getView().findViewById(R.id.searchResult);

        for(Item item: inventoryItems){
            String name = item.getName();
            boolean fullQueryFound = true;
            String[] queryWords = query.split(" ");

            for(int k = 0; k < queryWords.length; k++){
                if (!Pattern.compile(Pattern.quote(queryWords[k]), Pattern.CASE_INSENSITIVE).matcher(name).find()) {
                    fullQueryFound = false;
                }
            }
            if(fullQueryFound){
                searchItemIndexes.add(inventoryItems.indexOf(item));
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

    public void addToShrink(){
        Item item = inventoryItems.get(searchItemIndexes.get(searchItemIndex));
        System.out.println(item);
        try {
            item.setShrinkAmount(item.getShrinkAmount() + Integer.parseInt(String.valueOf(modifyInput.getText())));

            Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 100 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        populateItemInfo();
    }

    public void subtractFromShrink(){
        Item item = inventoryItems.get(searchItemIndexes.get(searchItemIndex));
        try{
            item.setShrinkAmount(item.getShrinkAmount() - Integer.parseInt(String.valueOf(modifyInput.getText())));
            if(item.getShrinkAmount() < 0){
                item.setShrinkAmount(0);
           }
            Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 100 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
            }
        }   catch (Exception e){
           e.printStackTrace();
        }
        populateItemInfo();
    }

    public void printShrink() throws IOException {

        File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App");

        if (!root.exists()) {
            root.mkdir();
        }


        File path = new File(root, "shrink_" + getDate() + ".txt");
        if(path.exists()){
            path.delete();
        }

        FileWriter writer = new FileWriter(path.getAbsolutePath(), true);

        String separator = "";
        for(int i = 0; i < 63; i++){
            separator+="-";
        }

        writer.append("Shrink for: " + getDate() +"\tDone by: "+ main.employeeName + "\n\n\n" + separator + "\n" +
                "|  Amount   |                    Item Name                    |\n" + separator + "\n");

        for(Item item: shrinkItems) {

            int textLength;
            int spaceRemainder;

            String shrinkAmount = String.valueOf(item.getShrinkAmount());
            textLength = String.valueOf(item.getShrinkAmount()).length();

            writer.append("|");

            if(shrinkAmount.length() > 3){
                shrinkAmount = shrinkAmount.substring(0,shrinkAmount.length() - 3) + "," + shrinkAmount.substring(shrinkAmount.length()-3);
                textLength++;
            }
            if(item.isWeightedItem()){
                shrinkAmount +="g";
                textLength++;
            }
            for(int i = 5 - textLength / 2; i > 0; i--){
                writer.append(" ");
                textLength++;
            }
            writer.append(shrinkAmount);

            spaceRemainder = 11 - (textLength);

            for (int i = 0; i < spaceRemainder; i++) {
                writer.append(" ");
            }

            textLength = item.getName().length();
            spaceRemainder = 50 - (5 + textLength);

            writer.append("|    " + item.getName());
            for (int i = 0; i < spaceRemainder; i++) {
                writer.append(" ");
            }



            writer.append("|\n" + separator + "\n");
        }
        writer.flush();
        writer.close();
        Toast.makeText(main, "Shrink Printed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    public void populateItemInfo(){
        try {
            if (searchItemIndexes.size() > 0) {
                Item item = inventoryItems.get(searchItemIndexes.get(searchItemIndex));
                if(item.getName().length() > 20){
                    nameText.setTextSize(18);
                }
                else{
                    nameText.setTextSize(30);
                }

                if(item.isWeightedItem()){
                    shrinkInput.setHint("Enter shrink WEIGHT");
                    gramsText.setText("(grams)");
                }
                else{
                    shrinkInput.setHint("Enter shrink AMOUNT");
                    if(item.getShrinkAmount() == 1){
                        gramsText.setText("(item)");
                    }
                    else {
                        gramsText.setText("(item)");
                    }

                }

                nameText.setText(item.getName());

                if(item.getShrinkAmount() != 0) {
                    shrinkInput.setText(String.valueOf(item.getShrinkAmount()));
                }
                else{
                    shrinkInput.setText("");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
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

    public String getDate(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat nameDateFormat = new SimpleDateFormat("EEE MMMM dd, yyyy");
        return nameDateFormat.format(calendar.getTime());
    }
    /*
    public void AddShrinkItem(String input){
        String[] words = input.split(" ");
        double amount = 0;
        int amountIndex;
        for(int i = 0; i < words.length; i++){
            try{
                amount = Double.parseDouble(words[i]);
                words[i] = "";
            }
            catch (Exception e){
            }
        }

        if(amount != 0){
            TableRow row = new TableRow(main);
            TextView rowContent = new TextView(main);
            rowContent.setTextSize(16);
            for(int i = 0; i < words.length; i++){
                rowContent.setText(rowContent.getText() + words[i] + " ");
            }

            rowContent.setText(rowContent.getText() + "\t\t----------\t\t" + amount);
            row.addView(rowContent);
            table.addView(row);
            shrinkItems.add((String) rowContent.getText());
        }
        else{
            Toast.makeText(main, "No amount input detected", Toast.LENGTH_SHORT).show();
        }
    }

    public void SendShrink(){
        if(shrinkItems.size() > 0) {
            createFile();
        }
        else{
            Toast.makeText(main, "Shrink is empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void createFile(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM d, yyyy - HH:mm aaa" );
        String date = dateFormat.format(calendar.getTime());

        SimpleDateFormat nameDateFormat = new SimpleDateFormat("MM_dd_yyyy");
        String nameDate = nameDateFormat.format(calendar.getTime());

        try{
            File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App");
            if(!root.exists()){
                root.mkdir();
            }

            File filepath = new File(root, "shrink " + nameDate + ".txt");
            FileWriter writer = new FileWriter(filepath);

            writer.append("Shrink for " + date + "\n\n\n\t\t\tShrink Items:\n\n");

            for(String item : shrinkItems){
                writer.append(item + "\n");
            }

            writer.flush();
            writer.close();

            Toast.makeText(main, "Shrink Saved", Toast.LENGTH_LONG).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public void printShrink(MainActivity main){


        ActivityCompat.requestPermissions(main,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    String content = "Shrink test\napple ... 34\norange ... .67";
                    String fileName = "Shrink.txt";
                    FileOutputStream fos = null;

                    File path = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    }
                    else{
                        Toast.makeText(main, "Android version error", Toast.LENGTH_SHORT).show();
                    }
                    File file = new File(path, fileName);

                    try {
                        // Make sure the Pictures directory exists.
                        path.mkdirs();

                        fos = new FileOutputStream(file);
                        byte[] data = content.getBytes();
                        fos.write(data);
                        fos.close();
                        Toast.makeText(main, "File saved to: " + path + "/" + fileName, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        // Unable to create file, likely because external storage is
                        // not currently mounted.
                       Toast.makeText(main, "Error writing " + file, Toast.LENGTH_SHORT);
                    }
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(main, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

*/
}
