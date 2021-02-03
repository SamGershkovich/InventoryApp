/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class EditItem extends Fragment {

    MainActivity main;
    Inventory inventory;
    Item item;
    TextView itemNameText;

    int frontCaseValue;
    double frontPartialValue;
    int backCaseValue;
    double backPartialValue;

    EditText frontCaseText;
    EditText frontPartialText;
    EditText backCaseText;
    EditText backPartialText;

    CheckBox frontPCheckBox;
    CheckBox backPCheckBox;

    boolean isFrontPerpetual;
    boolean isBackPerpetual;

    boolean frontCheckSwitched = false;
    boolean backCheckSwitched = false;

    public void checkForNulls(){
       try{
           Double.parseDouble(String.valueOf(frontCaseText.getText()));
       }
       catch (Exception e){
           frontCaseText.setText(String.valueOf(frontCaseValue));
       }

       try{
           Double.parseDouble(String.valueOf(frontPartialText.getText()));
       }
       catch (Exception e){
           frontPartialText.setText(String.valueOf(frontPartialValue));
       }

       try{
           Double.parseDouble(String.valueOf(backCaseText.getText()));
       }
       catch (Exception e){
           backCaseText.setText(String.valueOf(backCaseValue));
       }

       try{
           Double.parseDouble(String.valueOf(backPartialText.getText()));
       }
       catch (Exception e){
           backPartialText.setText(String.valueOf(backPartialValue));
       }

    }

    public EditItem(MainActivity main) {
        this.main = main;
    }

    public void setInventory(Inventory inv){
        inventory = inv;
    }

    public void setItem(Item item) {
        this.item = item;

        if (itemNameText == null) {
            itemNameText = getView().findViewById(R.id.itemName);
            frontCaseText = getView().findViewById(R.id.frontCaseText);
            frontPartialText = getView().findViewById(R.id.frontPartialText);
            backCaseText = getView().findViewById(R.id.backCaseText);
            backPartialText = getView().findViewById(R.id.backPartialText);
            frontPCheckBox = getView().findViewById(R.id.frontPerpetual);
            backPCheckBox = getView().findViewById(R.id.backPerpetual);
        }

        isFrontPerpetual = item.isPerpetualFront();
        isBackPerpetual = item.isPerpetualBack();

        if(!frontCheckSwitched){
            frontPCheckBox.setChecked(isFrontPerpetual);

            frontCaseText.setEnabled(!isFrontPerpetual);
            frontPartialText.setEnabled(!isFrontPerpetual);
        }
        if(!backCheckSwitched){
            backPCheckBox.setChecked(isBackPerpetual);

            backCaseText.setEnabled(!isBackPerpetual);
            backPartialText.setEnabled(!isBackPerpetual);
        }

        itemNameText.setText(item.getName());

            //Item is counted by quantity
            if (!item.isWeightedItem()) {

                frontCaseValue = (int) (item.getFrontAmount());//get only whole amount of cases @ front
                frontPartialValue = Math.round((item.getFrontAmount() - frontCaseValue) * Double.parseDouble(item.getWeight()));//get only decimal amount and convert to quantity @ front
                frontPartialValue = (double) Math.round(frontPartialValue * 100) / 100;

                backCaseValue = (int) (item.getBackAmount());//get only whole amount of cases @ back
                backPartialValue = Math.round((item.getBackAmount() - backCaseValue) * Double.parseDouble(item.getWeight()));//get only decimal amount and convert to quantity @ back
                backPartialValue = (double) Math.round(backPartialValue * 100) / 100;

                frontCaseText.setText(String.valueOf(frontCaseValue));
                frontPartialText.setText(String.valueOf((int)frontPartialValue));
                backCaseText.setText(String.valueOf(backCaseValue));
                backPartialText.setText(String.valueOf((int)backPartialValue));
            }
            //item is counted by weight
            else {
                frontCaseValue = (int) (item.getFrontAmount());//get only whole amount of cases @ front
                frontPartialValue = item.getFrontAmount() - frontCaseValue;//get only decimal amount @ front
                frontPartialValue = (double) Math.round(frontPartialValue * 100) / 100;

                backCaseValue = (int) (item.getBackAmount());//get only whole amount of cases @ back
                backPartialValue = item.getBackAmount() - backCaseValue;//get only decimal amount @ back
                backPartialValue = (double) Math.round(backPartialValue * 100) / 100;

                frontCaseText.setText(String.valueOf(frontCaseValue));
                frontPartialText.setText(String.valueOf(frontPartialValue));
                backCaseText.setText(String.valueOf(backCaseValue));
                backPartialText.setText(String.valueOf(backPartialValue));
            }
        }

    public void setFrontPerpetual(boolean perpetual){
        frontCheckSwitched = true;
        isFrontPerpetual = perpetual;
        if(perpetual) {
            frontCaseText.setEnabled(false);
            frontPartialText.setEnabled(false);
        }
        else{
            frontCaseText.setEnabled(true);
            frontPartialText.setEnabled(true);
        }
    }

    public void setBackPerpetual(boolean perpetual){
        backCheckSwitched = true;
        isBackPerpetual = perpetual;
        if(perpetual) {
            backCaseText.setEnabled(false);
            backPartialText.setEnabled(false);
        }
        else{
            backCaseText.setEnabled(true);
            backPartialText.setEnabled(true);
        }
    }

    public void cancel(){
        checkForNulls();

        frontCheckSwitched = false;
        backCheckSwitched = false;

        CheckBox frontPerpetualCheckBox = getView().findViewById(R.id.frontPerpetual);
        CheckBox backPerpetualCheckBox = getView().findViewById(R.id.backPerpetual);

        frontPerpetualCheckBox.setChecked(item.isPerpetualFront());
        backPerpetualCheckBox.setChecked(item.isPerpetualBack());

        frontCaseText.setEnabled(!item.isPerpetualFront());
        frontPartialText.setEnabled(!item.isPerpetualFront());
        backCaseText.setEnabled(!item.isPerpetualBack());
        backPartialText.setEnabled(!item.isPerpetualBack());
    }

    public void Save(){
        checkForNulls();

        frontCheckSwitched = false;
        backCheckSwitched = false;

        double frontAmount;
        double backAmount;

        item.setPerpetualFront(isFrontPerpetual);
        item.setPerpetualBack(isBackPerpetual);

        //Item is counted by weight
        if(item.isWeightedItem()) {

            if(!String.valueOf(backPartialText.getText()).contains(".") || !String.valueOf(frontPartialText.getText()).contains(".")){
                Toast.makeText(main, "Partial value invalid", Toast.LENGTH_SHORT).show();
                return;
            }

            frontAmount = Double.parseDouble(String.valueOf(frontCaseText.getText())) + Double.parseDouble(String.valueOf(frontPartialText.getText()));//add whole and decimal amounts @ front
            backAmount = Double.parseDouble(String.valueOf(backCaseText.getText())) + Double.parseDouble(String.valueOf(backPartialText.getText()));//add whole and decimal amounts @ back
        }
        //Item is counted by quantity
        else{
            double frontPartialConvert;
            double backPartialConvert;

            if(String.valueOf(frontPartialText.getText()).contains(".")){
                frontPartialConvert =  Double.parseDouble(String.valueOf(frontPartialText.getText()));
            }else{
                frontPartialConvert = Double.parseDouble(String.valueOf(frontPartialText.getText())) / Double.parseDouble(item.getWeight());//convert quantity amount to decimal amount
            }

            if(String.valueOf(backPartialText.getText()).contains(".")){
                backPartialConvert = Double.parseDouble(String.valueOf(backPartialText.getText()));
            }else{
                backPartialConvert = Double.parseDouble(String.valueOf(backPartialText.getText())) / Double.parseDouble(item.getWeight());//convert quantity amount to decimal amount
            }

            frontAmount = Double.parseDouble(String.valueOf(frontCaseText.getText())) + frontPartialConvert;//add whole and decimal amounts @ front
            backAmount = Double.parseDouble(String.valueOf(backCaseText.getText()))  + backPartialConvert;//add whole and decimal amounts @ back
        }

        try {
            inventory.setItemAmount(frontAmount, backAmount, item);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_edit_item, container, false);
        return fragment;
    }
}