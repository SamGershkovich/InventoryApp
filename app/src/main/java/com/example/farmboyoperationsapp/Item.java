/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import java.io.Serializable;

public class Item implements Serializable {

    private String category, code, name, weight, quantity, pricePerPound, pricePerKilo;
    private boolean weightedItem = true, perpetualFront = false, perpetualBack = false, confirmed = false;
    private double frontAmount, backAmount;
    private int shrinkAmount;




    public Item(String category, String code, String name, String weight, String quantity, String pricePerPound, String pricePerKilo){
        this.category = category;
        this.code = code;
        this.name = name;
        this.weight = weight;
        this.quantity = quantity;
        this.pricePerPound = pricePerPound;
        this.pricePerKilo = pricePerKilo;

        if(pricePerPound == ""){
            weightedItem = false;
        }

    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getWeight() {
        return weight;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPricePerPound() {
        return pricePerPound;
    }

    public String getPricePerKilo() {
        return pricePerKilo;
    }

    public double getFrontAmount() {
        return frontAmount;
    }

    public double getBackAmount() {
        return backAmount;
    }

    public void setFrontAmount(double amount){
        frontAmount =(double) Math.round(amount * 100) / 100;
        if(frontAmount != 0.0){
            confirmed = true;
        }
    }

    public void setBackAmount(double amount){
        backAmount = (double) Math.round(amount * 100) / 100;
    }

    public boolean isWeightedItem() {
        return weightedItem;
    }

    public void setPerpetualFront(boolean state){
        perpetualFront = state;
        confirmed = state;
    }

    public boolean isPerpetualFront() {
        return perpetualFront;
    }
    public void setPerpetualBack(boolean state){
        perpetualBack = state;
    }

    public boolean isPerpetualBack() {
        return perpetualBack;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getShrinkAmount() {
        return shrinkAmount;
    }

    public void setShrinkAmount(int shrinkAmount) {
        this.shrinkAmount = shrinkAmount;
    }

    public String toString(){
        return String.format(" Code: %s | Name: %s | Wt: %s | Qty: %s | /lb: %s | /kg: %s", code, name, weight, quantity, pricePerPound, pricePerKilo);
    }
}
