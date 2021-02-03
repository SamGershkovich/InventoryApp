/**
 * @author Sam Gershkovich
 **/

package com.example.farmboyoperationsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.sip.SipSession;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Layout;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    ArrayList<Item> items = new ArrayList<>();

    MainActivity main = this;
    EditItem editItemPage;
    PrintVerification printVerificationPage;
    Inventory inventoryPage;
    PriceList priceListPage;
    Shrink shrinkPage;
    FileLoad fileLoadPage;

    Dialog loginDialogue;
    Dialog settingsDialogue;
    Dialog shrinkDialogue;
    Dialog employeeEditDialogue;
    //Dialog inventoryDialogue;

    ArrayList<TableRow> employeesToRemove = new ArrayList<>();

    boolean addedEmployee = false;

    Fragment selectedFragment;
    int selectedFragmentId;

    BottomNavigationView bottomNav;

    boolean countBy = false;
    boolean roundTo = false;

    boolean loggedIn = false;
    String employeeName;
    String employeeRank;

    Map<Integer, String[]> employeeMap = new HashMap<Integer, String[]>();

    ArrayList<String> employees = new ArrayList<>();

    File employeeDataPath;

    //public FileWriter logWriter;

    public MainActivity() throws IOException {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Farmboy Operations App");
            if (!root.exists()) {
                root.mkdir();
            }

            File errorLog = new File(root, "status_log.txt");
            errorLog.createNewFile();
            //logWriter = new FileWriter(errorLog, true);

            File rootData = new File(root, "saved_locations");
            if(!rootData.exists()){
                rootData.mkdir();
            }
            //logWriter.append("saved locations folder created\n");

            File rootLogs = new File(root, "logs");
            if(!rootLogs.exists()){
                rootLogs.mkdir();
            }
            //logWriter.append("logs folder created\n");

            File rootEmployees = new File(root, "employee_data");
            if(!rootEmployees.exists()){
                rootEmployees.mkdir();
                File employees = new File(rootEmployees.getAbsoluteFile() + "employees.txt");
            }
            //logWriter.append("employee data folder created\n");

            editItemPage = new EditItem(main);
            //logWriter.append("edit item page created\n");

            printVerificationPage = new PrintVerification(main, editItemPage);
            //logWriter.append("print verification page created\n");

            inventoryPage = new Inventory(main, editItemPage, printVerificationPage);

            priceListPage = new PriceList(main, inventoryPage);
            //logWriter.append("pricelist page created\n");

            shrinkPage = new Shrink(main, inventoryPage);
            //logWriter.append("shrink page created\n");

            fileLoadPage = new FileLoad(main);
            //logWriter.append("file load page created\n");

            //logWriter.flush();
            //logWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Directory creation failed", Toast.LENGTH_SHORT).show();
            this.finishAffinity();
        }

        LoadEmployees();
    }

    @Override
    public void onBackPressed () {}

    public void LoadEmployees() throws IOException {
        employeeDataPath = new File(Environment.getExternalStorageDirectory() + "/Farmboy Operations App/employee_data/employees.txt");
        employees.clear();
        if(employeeDataPath.exists()){
            FileReader reader = new FileReader(employeeDataPath);
            BufferedReader br = new BufferedReader(reader);
            String currLine;
            while((currLine = br.readLine()) != null){
                employees.add(currLine);
            }
            reader.close();
            br.close();

            for(String employee: employees) {
                String[] employeeInfo = employee.split(",");
                if(employeeInfo[0].length() == 5 && employeeInfo[1].length() > 1) {
                    employeeMap.put(Integer.parseInt(employeeInfo[0]), employeeInfo[1].split("/"));
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, priceListPage, "PRICE_LIST").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, shrinkPage, "SHRINK").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, inventoryPage, "INVENTORY").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, editItemPage, "EDIT_ITEM").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, printVerificationPage, "PRINT_VERIFY").commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fileLoadPage, "LOAD").commit();

        selectedFragment = priceListPage;
        selectedFragmentId = R.id.nav_price_list;

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        StartLogin();

        //createPermissions();
    }

    /**Fragment Switcher*/
    private BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            {
                if(menuItem.getItemId() == R.id.nav_settings){
                    settingsDialogue = new Dialog(main);
                    settingsDialogue.setContentView(R.layout.fragment_settings);
                    settingsDialogue.show();

                    Button editEmployeesButton = settingsDialogue.findViewById(R.id.editEmployeesButton);
                    if(employeeRank.contains("admin")){
                        editEmployeesButton.setVisibility(View.VISIBLE);
                    }
                    else{
                        editEmployeesButton.setVisibility(View.INVISIBLE);
                    }

                    settingsDialogue.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                          bottomNav.setSelectedItemId(selectedFragmentId);
                        }
                    });

                    TextView employeeInfo = settingsDialogue.findViewById(R.id.employeeInfo);
                    employeeInfo.setText("Employee: \n" + employeeName);
                }
                else if (loggedIn) {

                    //set selected fragment based on id of fragment item clicked
                    switch (menuItem.getItemId()) {
                        case R.id.nav_price_list:
                            main.setTitle("Price list");

                            switch (selectedFragmentId){
                                case R.id.nav_shrink:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_exit_far, R.anim.slide_to_right_exit_far)
                                            .hide(shrinkPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_exit_mid, R.anim.slide_to_right_exit_mid)
                                            .show(inventoryPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_enter_far, R.anim.slide_to_right_enter_far)
                                            .show(priceListPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .hide(inventoryPage).commit();
                                    break;
                                case R.id.nav_inventory:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_enter, R.anim.slide_to_right_enter)
                                            .show(priceListPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_exit, R.anim.slide_to_right_exit)
                                            .hide(inventoryPage).commit();
                                    break;
                            }

                            selectedFragment = priceListPage;
                            selectedFragmentId =R.id.nav_price_list;
                            break;

                        case R.id.nav_inventory:
                            main.setTitle("Inventory");

                            switch (selectedFragmentId){
                                case R.id.nav_price_list:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_enter, R.anim.slide_to_left_enter)
                                            .show(inventoryPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_exit, R.anim.slide_to_left_exit)
                                            .hide(priceListPage).commit();
                                    break;
                                case R.id.nav_shrink:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_enter, R.anim.slide_to_right_enter)
                                            .show(inventoryPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_right_exit, R.anim.slide_to_right_exit)
                                            .hide(shrinkPage).commit();
                                    break;
                            }

                            if(selectedFragmentId == R.id.nav_price_list) {

                            }

                            selectedFragment = inventoryPage;
                            selectedFragmentId =R.id.nav_inventory;
                            break;

                        case R.id.nav_shrink:
                            main.setTitle("Shrink");

                            switch (selectedFragmentId){
                                case R.id.nav_price_list:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_exit_far, R.anim.slide_to_left_exit_far)
                                            .hide(priceListPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_exit_mid, R.anim.slide_to_left_exit_mid)
                                            .show(inventoryPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_enter_far, R.anim.slide_to_left_enter_far)
                                            .show(shrinkPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .hide(inventoryPage).commit();
                                    break;
                                case R.id.nav_inventory:
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_enter, R.anim.slide_to_left_enter)
                                            .show(shrinkPage).commit();
                                    getSupportFragmentManager().beginTransaction()
                                            .setCustomAnimations(R.anim.slide_to_left_exit, R.anim.slide_to_left_exit)
                                            .hide(inventoryPage).commit();
                                    break;
                            }

                            selectedFragment = shrinkPage;
                            selectedFragmentId =R.id.nav_shrink;
                            break;
                    }
                }
                return true;
            } }};

    public void StartLogin(){
        main.setTitle("Farmboy Operations App");

        //hide all fragments
        getSupportFragmentManager().beginTransaction().hide(priceListPage).commit();
        getSupportFragmentManager().beginTransaction().hide(inventoryPage).commit();
        getSupportFragmentManager().beginTransaction().hide(shrinkPage).commit();
        getSupportFragmentManager().beginTransaction().hide(editItemPage).commit();
        getSupportFragmentManager().beginTransaction().hide(printVerificationPage).commit();
        getSupportFragmentManager().beginTransaction().hide(fileLoadPage).commit();

        //set price list as selected fragment so when user logs in it will be showing
        selectedFragment = priceListPage;
        selectedFragmentId = R.id.nav_price_list;

        //show the login dialogue
        loginDialogue = new Dialog(this);
        loginDialogue.setContentView(R.layout.fragment_login);
        loginDialogue.show();
        loginDialogue.setCancelable(false);
        bottomNav.setVisibility(View.INVISIBLE);
    }

    public void VerifyLogin(View view){

        //get the users input
        EditText input = view.getRootView().findViewById(R.id.employeeNumber);

        //hide keyboard
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        //try to parse emplyee number; if successfull log in
        try {
            int id = Integer.parseInt(String.valueOf(input.getText()));

            if (employeeMap.containsKey(id) || id == 00000) {
                if(id == 00000){
                    employeeName = "ADMIN";
                    employeeRank = "admin";

                }else {
                    employeeName = employeeMap.get(id)[0];
                    employeeRank = employeeMap.get(id)[1];
                }
                Toast.makeText(this, "Logged in as " + employeeName, Toast.LENGTH_LONG).show();
                loggedIn = true;
                LoadApp();
            } else {
                Toast.makeText(this, "Employee not found", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void Logout(View view){
        loggedIn = false;
        settingsDialogue.cancel();
        employeeName = "";
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up_exit, R.anim.slide_up_exit)
                .hide(selectedFragment).commit();
        StartLogin();
    }

    public void LoadApp(){
        //close the log in dialogue, show the menu bar, and show the price list page
        loginDialogue.cancel();
        bottomNav.setVisibility(View.VISIBLE);
        if(selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_enter, R.anim.slide_up_enter)
                    .show(selectedFragment).commit();
        }

        priceListPage.GenerateItemDatabase();
        inventoryPage.GenerateInventoryDatabase();
        shrinkPage.InitiateShrinkPage();
    }

    public void EditEmployees(View view){
        employeeEditDialogue = new Dialog(main);
        employeeEditDialogue.setContentView(R.layout.fragment_employees);
        final TableLayout employeeTable = employeeEditDialogue.findViewById(R.id.employeeTable);

        employeeEditDialogue.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                    try {
                        FileWriter writer = new FileWriter(employeeDataPath);
                        writer.write("");
                        writer = new FileWriter(employeeDataPath, true);

                        for (int i = 0; i < employeeTable.getChildCount(); i++) {

                            TableRow currRow = (TableRow) employeeTable.getChildAt(i);
                            String currEmp = "" + ((EditText) currRow.getChildAt(1)).getText();
                            if(currEmp.matches("")){
                                continue;
                            }
                            if(("" + ((EditText) currRow.getChildAt(2)).getText()).length() < 1)
                            {
                                continue;
                            }
                            currEmp += "," + ((EditText) currRow.getChildAt(2)).getText();

                            for (int j = 1; j < 3; j++) {

                                EditText currCol = (EditText) currRow.getChildAt(j);
                                System.out.println(currCol.getText());

                                if (currCol.getText().toString().matches("")) {
                                    employeeTable.removeView(currRow);
                                    employees.remove(currEmp);
                                    break;
                                }
                            }
                            writer.append(((EditText) currRow.getChildAt(1)).getText() + ",");
                            writer.append(((EditText) currRow.getChildAt(2)).getText()+"\n");
                        }
                        writer.flush();
                        writer.close();

                        LoadEmployees();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        });

        for(String employee: employees) {
            TableRow row = new TableRow(main);

            if(employee.length() > 8) {
                String[] employeeInfo = employee.split(",");

                EditText employeeNum = new EditText(main);

                //set the input to be digit only
                employeeNum.setInputType(InputType.TYPE_CLASS_NUMBER);

                //set max length to 5 digits
                InputFilter[] filters = new InputFilter[1];
                filters[0] = new InputFilter.LengthFilter(5);
                employeeNum.setFilters(filters);

                employeeNum.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }
                });
                employeeNum.setGravity(Gravity.CENTER);
                employeeNum.setTextSize(16);
                employeeNum.setWidth(300);
                employeeNum.setText(employeeInfo[0]);
                employeeNum.setHint("Employee Num.");

                EditText nameCol = new EditText(main);
                nameCol.setTextSize(16);
                nameCol.setText(employeeInfo[1]);
                employeeNum.setHint("Employee Name");

                final CheckBox selectBox = new CheckBox(main);

                selectBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        TableRow row = (TableRow)selectBox.getParent();

                        if(isChecked){
                            employeesToRemove.add(row);
                        }
                        else {
                            if(employeesToRemove.contains(row)){
                                employeesToRemove.remove(row);
                            }
                        }

                        if(employeesToRemove.size() >=1){
                            employeeEditDialogue.findViewById(R.id.removeEmployeeButton).setEnabled(true);
                        }
                        else{
                            employeeEditDialogue.findViewById(R.id.removeEmployeeButton).setEnabled(false);
                        }
                    }
                });

                row.addView(selectBox);
                row.addView(employeeNum);
                row.addView(nameCol);

                employeeTable.addView(row);
            }
        }
        employeeEditDialogue.show();
    }

    public void AddEmployee(View view){
        addedEmployee = true;
        ScrollView scrollView = employeeEditDialogue.findViewById(R.id.employeeScroller);
        TableLayout table = employeeEditDialogue.findViewById(R.id.employeeTable);
        scrollView.fling(2000 * table.getChildCount());
        TableRow row = new TableRow(main);

        EditText employeeNum = new EditText(main);

        //set the input to be digit only
        employeeNum.setInputType(InputType.TYPE_CLASS_NUMBER);

        //set max length to 5 digits
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(5);
        employeeNum.setFilters(filters);

        employeeNum.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
        employeeNum.setGravity(Gravity.CENTER);
        employeeNum.setTextSize(16);
        employeeNum.setWidth(300);
        employeeNum.setHint("Emp. Num");

        EditText nameCol = new EditText(main);
        nameCol.setTextSize(16);
        nameCol.setHint("Emp. Name");

        CheckBox selectBox = new CheckBox(main);

        row.addView(selectBox);
        row.addView(employeeNum);
        row.addView(nameCol);

        table.addView(row);
    }

    public void RemoveEmployee(View view){
        for(TableRow currRow: employeesToRemove){
            TableLayout table = employeeEditDialogue.findViewById(R.id.employeeTable);
            String currEmp = "" + ((EditText) currRow.getChildAt(1)).getText();
            currEmp += "," + ((EditText) currRow.getChildAt(2)).getText();

            employees.remove(currEmp);
            table.removeView(currRow);
        }
    }

    public void OpenFilePicker(View view){
        fileLoadPage.openFilePicker(view);
    }

    public void GetSpeechInput(View view) {
       if((getSupportFragmentManager().findFragmentByTag("PRICE_LIST").isVisible())){
           priceListPage.getSpeechInput();
       }

       if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible())){
           inventoryPage.getSpeechInput();
       }

       if((getSupportFragmentManager().findFragmentByTag("SHRINK").isVisible())){
           shrinkPage.getSpeechInput();
       }
    }

    public void NextItem(View view){

        Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
        }
        if((getSupportFragmentManager().findFragmentByTag("PRICE_LIST").isVisible())){
            priceListPage.NextItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible())){
            inventoryPage.NextItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("SHRINK").isVisible())){
            shrinkPage.NextItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("PRINT_VERIFY").isVisible())){
            printVerificationPage.NextItem();
        }
    }

    public void PrevItem(View view){
        Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        if((getSupportFragmentManager().findFragmentByTag("PRICE_LIST").isVisible())){
            priceListPage.PrevItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible())){
            inventoryPage.PrevItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("SHRINK").isVisible())){
            shrinkPage.PrevItem();
        }

        if((getSupportFragmentManager().findFragmentByTag("PRINT_VERIFY").isVisible())){
            printVerificationPage.PrevItem();
        }

    }

    public void EditItem(View view){
        main.setTitle("Edit item");
        if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible())){
            if(inventoryPage.searchItemIndexes.size() > 0) {
                inventoryPage.editItem();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                        .show(editItemPage).commit();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                        .hide(inventoryPage).commit();
            }
        }

        if((getSupportFragmentManager().findFragmentByTag("PRINT_VERIFY").isVisible())){
            if(printVerificationPage.zeroedItemIndexes.size() > 0) {
                printVerificationPage.editItem();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                        .show(editItemPage).commit();
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                        .hide(printVerificationPage).commit();
            }
        }
    }

    public void SetFrontPerpetual(View view){
        CheckBox checkBox = (CheckBox) view;
        editItemPage.setFrontPerpetual(checkBox.isChecked());
    }

    public void SetBackPerpetual(View view){
        CheckBox checkBox = (CheckBox) view;
        editItemPage.setBackPerpetual(checkBox.isChecked());
    }

    public void SaveEdit(View view){
        editItemPage.Save();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                .hide(editItemPage).commit();
        if(inventoryPage.verifying){
            main.setTitle("Verify inventory");

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(printVerificationPage).commit();
            printVerificationPage.findUncounted(inventoryPage.items);
        }
        else {
            main.setTitle("Inventory");

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(inventoryPage).commit();
        }
    }

    public void CancelEdit(View view){
        editItemPage.cancel();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                .hide(editItemPage).commit();
        if(inventoryPage.verifying){
            main.setTitle("Verify inventory");

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(printVerificationPage).commit();
        }
        else {
            main.setTitle("Inventory");

            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(inventoryPage).commit();
        }
    }

    public void PrintInventory(View view) throws IOException {
        if((getSupportFragmentManager().findFragmentByTag("PRINT_VERIFY").isVisible())){
            main.setTitle("Inventory");
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                    .hide(printVerificationPage).commit();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(inventoryPage).commit();
            bottomNav.setVisibility(View.VISIBLE);
            inventoryPage.printInventory();
        }

        if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible() && inventoryPage.items.size() > 0)){
            main.setTitle("Verify inventory");
            inventoryPage.verify();
            inventoryPage.verifying = true;
            bottomNav.setVisibility(View.INVISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                    .hide(inventoryPage).commit();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                    .show(printVerificationPage).commit();
        }
    }

    public void FinalizeInventory(View view){
       inventoryPage.finalizeInventory();
    }

    public void LoadInvLog(View view){

    }

    public void SetCountBy(View view){
        CheckBox check =  view.findViewById(R.id.countBy);
        countBy = check.isChecked();
    }
    public void SetRoundTo(View view){
       CheckBox check =  view.findViewById(R.id.roundTo);
       roundTo = check.isChecked();
    }

    public void ConfirmItem(View view){
        printVerificationPage.confirmItem();
    }

    public void CancelVerify(View view){
        main.setTitle("Inventory");
        bottomNav.setVisibility(View.VISIBLE);
        inventoryPage.verifying = false;
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_close_exit, R.anim.fragment_close_exit)
                .hide(printVerificationPage).commit();
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fragment_open_enter, R.anim.fragment_open_enter)
                .show(inventoryPage).commit();


    }

    public void SearchDatabase(View view){

        Vibrator v = (Vibrator) main.getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        CloseKeyboard();
        if((getSupportFragmentManager().findFragmentByTag("PRICE_LIST").isVisible())){
            priceListPage.searchPriceList("");
        }

        if((getSupportFragmentManager().findFragmentByTag("INVENTORY").isVisible())){
            inventoryPage.searchInventory("");
        }

        if((getSupportFragmentManager().findFragmentByTag("SHRINK").isVisible())){
            shrinkPage.searchInventory("");
        }
    }

    public void ViewShrink(View view){
        shrinkDialogue = new Dialog(main);
        shrinkDialogue.setContentView(R.layout.fragment_shrink_list_viewer);

        TableLayout shrinkTable = shrinkDialogue.findViewById(R.id.shrinkTable);

        int counter = 0;
        for(Item shrinkItem: shrinkPage.shrinkItems) {
            String shrinkAmount = String.valueOf(shrinkItem.getShrinkAmount());
            TableRow row = new TableRow(main);

            final EditText amountCol = new EditText(main);

            //set the input to be no decimal
            amountCol.setInputType(InputType.TYPE_CLASS_NUMBER);

            amountCol.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    String shrinkAmount = "";
                    shrinkAmount = String.valueOf(s).replace("\t", "");
                    if (shrinkAmount.contains("g")) {
                        shrinkAmount = shrinkAmount.replace("g", "");
                    }
                    if (shrinkAmount.contains(",")) {
                        shrinkAmount = shrinkAmount.replace(",", "");
                    }
                    if(shrinkAmount.length() > 5){
                        shrinkAmount = shrinkAmount.substring(0, 5);
                    }

                    try {
                        shrinkPage.editItemAmount((TableRow) amountCol.getParent(), shrinkAmount);
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
            amountCol.setGravity(Gravity.CENTER);
            amountCol.setTextSize(16);
            amountCol.setWidth(300);

           if(shrinkAmount.length() > 3){
               shrinkAmount = shrinkAmount.substring(0,shrinkAmount.length() - 3) + "," + shrinkAmount.substring(shrinkAmount.length()-3);
           }
            if(shrinkItem.isWeightedItem()){
                shrinkAmount += "g";
            }
            amountCol.setText(String.valueOf(shrinkAmount));


            TextView nameCol = new TextView(main);
            //nameCol.setWidth(700);
            //nameCol.setMaxWidth(700);
            nameCol.setTextSize(16);
            nameCol.setText(shrinkItem.getName());



            row.addView(amountCol);
            row.addView(nameCol);

            shrinkTable.addView(row);

            if (counter % 2 == 0) {
                row.setBackgroundColor(Color.WHITE);
            } else {
                row.setBackgroundColor(Color.LTGRAY);
            }
            counter++;
        }
        shrinkDialogue.show();
        shrinkDialogue.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                shrinkPage.populateItemInfo();
            }
        });
    }
    /*public void ViewInventory(View view){
        inventoryDialogue = new Dialog(main);
        inventoryDialogue.setContentView(R.layout.fragment_inventory_list_viewer);

        TableLayout shrinkTable = inventoryDialogue.findViewById(R.id.invTable);

        int counter = 0;
        for(Item shrinkItem: shrinkPage.shrinkItems) {
            String shrinkAmount = String.valueOf(shrinkItem.getShrinkAmount());
            TableRow row = new TableRow(main);

            final EditText amountCol = new EditText(main);

            //set the input to be no decimal
            amountCol.setInputType(InputType.TYPE_CLASS_NUMBER);

            amountCol.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    String shrinkAmount = "";
                    shrinkAmount = String.valueOf(s).replace("\t", "");
                    if (shrinkAmount.contains("g")) {
                        shrinkAmount = shrinkAmount.replace("g", "");
                    }
                    if (shrinkAmount.contains(",")) {
                        shrinkAmount = shrinkAmount.replace(",", "");
                    }
                    if(shrinkAmount.length() > 5){
                        shrinkAmount = shrinkAmount.substring(0, 5);
                    }

                    shrinkPage.editItemAmount((TableRow) amountCol.getParent(), shrinkAmount);
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });
            amountCol.setGravity(Gravity.CENTER);
            amountCol.setTextSize(16);
            amountCol.setWidth(300);

           if(shrinkAmount.length() > 3){
               shrinkAmount = shrinkAmount.substring(0,shrinkAmount.length() - 3) + "," + shrinkAmount.substring(shrinkAmount.length()-3);
           }
            if(shrinkItem.isWeightedItem()){
                shrinkAmount += "g";
            }
            amountCol.setText(String.valueOf(shrinkAmount));


            TextView nameCol = new TextView(main);
            //nameCol.setWidth(700);
            //nameCol.setMaxWidth(700);
            nameCol.setTextSize(16);
            nameCol.setText(shrinkItem.getName());



            row.addView(amountCol);
            row.addView(nameCol);

            shrinkTable.addView(row);

            if (counter % 2 == 0) {
                row.setBackgroundColor(Color.WHITE);
            } else {
                row.setBackgroundColor(Color.LTGRAY);
            }
            counter++;
        }
        shrinkDialogue.show();
        shrinkDialogue.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                shrinkPage.populateItemInfo();
            }
        });
    }*/

    public void AddToShrink(View view){
        shrinkPage.addToShrink();
        ((EditText)view.getRootView().findViewById(R.id.modifyShrinkAmount)).setText("");
    }

    public void SubtractFromShrink(View view){
        shrinkPage.subtractFromShrink();
        ((EditText)view.getRootView().findViewById(R.id.modifyShrinkAmount)).setText("");

    }

    public void PrintShrink(View view) throws IOException {
        shrinkPage.printShrink();
    }

    public void CloseKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void SelectText(View view) {
       TextInputEditText inputText = (TextInputEditText)view;
       inputText.selectAll();
    }


    /*public void createPermissions(){
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
   }*///create permission
/*
    public void searchInventory(String query) {
        queryFound = false;
        TableLayout table = fragmentView.findViewById(R.id.table);
        TextView searchResult = fragmentView.findViewById(R.id.searchResult);
        ScrollView scrollView = fragmentView.findViewById(R.id.scrollView);
        HorizontalScrollView horizScroll = fragmentView.findViewById(R.id.horizScroll);

        for (int i = 0; i < table.getChildCount(); i++) {
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
                    scrollView.scrollTo(0, 120*i - 240);
                    horizScroll.scrollTo(340*j - 240, 0);
                    cell.setBackgroundColor(Color.YELLOW);
                    queryFound = true;
                    break;
                }
            }
        }
        if (!queryFound) {
            searchResult.setText("Query not found");
        }
    }
/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case 10:
                if(resultCode == RESULT_OK && data != null){
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    TextView resultText = fragmentView.findViewById(R.id.txvResult);
                    if(resultText != null) {
                        resultText.setText("Searching for: " + result.get(0));
                    }
                    searchQuery = result.get(0);
                    searchInventory(searchQuery);
                }
                break;
        }
    }*///search inventory
}
