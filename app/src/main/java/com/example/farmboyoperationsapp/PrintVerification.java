/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PrintVerification extends Fragment {

    MainActivity main;
    Inventory inventoryPage;
    EditItem editItemPage;

    TextView progress;
    TextView searchResult;
    TextView nameText;
    TextView frontAmountText;
    TextView backAmountText;
    Button confirmButton;
    Button confirmEditButton;

    ArrayList<Item> inventoryItems = new ArrayList<>();
    ArrayList<Integer> zeroedItemIndexes = new ArrayList<>();

    int zeroedItemIndex = 0;

    public PrintVerification(MainActivity main, EditItem editItemPage) {
        this.main = main;
        this.editItemPage = editItemPage;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_print_verification, container, false);
    }

    public void findUncounted(ArrayList<Item> items){
        if(searchResult == null){
            progress = getView().findViewById(R.id.confirmProgress);
            searchResult = getView().findViewById(R.id.confirmSearchResult);
            nameText = getView().findViewById(R.id.confirmItemName);
            frontAmountText = getView().findViewById(R.id.confirmFrontAmountText);
            backAmountText = getView().findViewById(R.id.confirmBackAmountText);
            confirmButton = getView().findViewById(R.id.confirmButton);
            confirmEditButton = getView().findViewById(R.id.confirmEditButton);
        }
        inventoryItems = items;
        zeroedItemIndexes.clear();

        for(Item item: inventoryItems){
            if (!item.isConfirmed()) {
                zeroedItemIndexes.add(inventoryItems.indexOf(item));
            }
        }
        if (zeroedItemIndexes.size() == 0) {
            confirmButton.setVisibility(View.INVISIBLE);
            confirmEditButton.setVisibility(View.INVISIBLE);
            searchResult.setText("");
            progress.setText("");
            frontAmountText.setText("");
            backAmountText.setText("");
            nameText.setText("All items counted!");
        }
        else{
            confirmButton.setVisibility(View.VISIBLE);
            confirmEditButton.setVisibility(View.VISIBLE);
            if(zeroedItemIndex > zeroedItemIndexes.size() - 1) {
                zeroedItemIndex = 0;
            }
            searchResult.setText("Item found - " + zeroedItemIndexes.size() + " result(s) found");
            progress.setText((zeroedItemIndex + 1) + "/" + zeroedItemIndexes.size());
            populateItemInfo();

        }

    }

    public void editItem(){
        if(editItemPage.inventory == null){
            editItemPage.inventory = this.inventoryPage;
        }
        editItemPage.setItem(inventoryItems.get(zeroedItemIndexes.get(zeroedItemIndex)));
    }

    public void confirmItem(){
        inventoryItems.get(zeroedItemIndexes.get(zeroedItemIndex)).setConfirmed(true);

        Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 100 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        findUncounted(inventoryItems);
    }

    @SuppressLint("SetTextI18n")
    public void populateItemInfo(){
        try {
            if (zeroedItemIndexes.size() > 0) {
                Item item = inventoryItems.get(zeroedItemIndexes.get(zeroedItemIndex));
                if(item.getName().length() > 20){
                    nameText.setTextSize(18);
                }
                else{
                    nameText.setTextSize(30);
                }
                nameText.setText(item.getName());
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
        if(zeroedItemIndexes.size() > 0) {
            if (zeroedItemIndex < zeroedItemIndexes.size() - 1) {
                zeroedItemIndex++;
                progress.setText((zeroedItemIndex + 1) + "/" + zeroedItemIndexes.size());
            } else {
                zeroedItemIndex = 0;
                progress.setText((zeroedItemIndex + 1) + "/" + zeroedItemIndexes.size());
            }
            populateItemInfo();
        }
    }

    public void PrevItem(){
        if(zeroedItemIndexes.size() > 0) {
            if (zeroedItemIndex > 0) {
                zeroedItemIndex--;
                progress.setText((zeroedItemIndex + 1) + "/" + zeroedItemIndexes.size());
            } else {
                zeroedItemIndex = zeroedItemIndexes.size() - 1;
                progress.setText((zeroedItemIndex + 1) + "/" + zeroedItemIndexes.size());
            }
            populateItemInfo();
        }
    }
}