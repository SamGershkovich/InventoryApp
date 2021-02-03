/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileLoad extends Fragment {

    public static final int PICKFILE_RESULT_CODE = 1;
    private Uri fileUri;
    private String filePath;

    boolean selectPricelist;
    boolean selectInventory;
    boolean selectInvLog;
    boolean selectShrinkLog;

    MainActivity main;
    PriceList priceList;
    Inventory inventory;

    public FileLoad(MainActivity main) {
        this.main = main;
        priceList = main.priceListPage;
        inventory = main.inventoryPage;
    }

    public void openFilePicker(View view) {
        if(view.getId() == R.id.loadPriceListButton) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Select price list pdf");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            selectPricelist = true;
        }
        else if(view.getId() == R.id.loadInventoryButton) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Select inventory pdf");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            selectInventory = true;
        }
        else if(view.getId() == R.id.loadInvLogButton) {
            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "Select inventory log");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
            selectInvLog = true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == -1) {

                    fileUri = data.getData();
                    filePath = fileUri.getPath();
                    filePath = filePath.substring(filePath.indexOf("/storage"));

                    if(selectPricelist || selectInventory) {
                        try {
                            File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App/saved_locations");
                            if (!root.exists()) {
                                root.mkdir();
                            }

                            if (selectPricelist) {
                                selectPricelist = false;
                                File path = new File(root, "pricelist_location.txt");
                                FileWriter writer = new FileWriter(path);

                                writer.write(filePath);
                                writer.flush();
                                writer.close();
                                Toast.makeText(main, "Price list location saved", Toast.LENGTH_SHORT).show();
                                priceList.GenerateItemDatabase();
                            } else if (selectInventory) {
                                selectInventory = false;
                                File path = new File(root, "inventory_location.txt");
                                FileWriter writer = new FileWriter(path);

                                writer.write(filePath);
                                writer.flush();
                                writer.close();
                                Toast.makeText(main, "Inventory location saved", Toast.LENGTH_SHORT).show();
                                inventory.GenerateInventoryDatabase();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(main, "Location save failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (selectInvLog) {
                        selectInvLog = false;
                        inventory.inventoryLog = filePath;
                        Toast.makeText(main, "Inventory log loaded", Toast.LENGTH_SHORT).show();
                        try {
                            inventory.loadInventoryData();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
    }
}