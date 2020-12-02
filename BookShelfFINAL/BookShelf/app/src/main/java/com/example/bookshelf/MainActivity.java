package com.example.bookshelf;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // declarations
    ListView lv_pdf;
    public static ArrayList<File> fileList = new ArrayList<>();
    PDFAdapter obj_adapter;
    boolean bolean_permission;
    File dir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //set view to main upon startup

        lv_pdf = findViewById(R.id.listView_pdf);

        dir = new File(Environment.getExternalStorageDirectory().toString()); //make new file with list of files from storage
        permission_fn();

        Button buttonSort = findViewById(R.id.button_sort); //set the sort button with appropriate id
        buttonSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sortArrayList();
            }
        });

        lv_pdf.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int itemPosition, long l) { //function for a long click on an item in the pdf list

                Log.d("MyTag", fileList.get(itemPosition).getAbsolutePath());

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // Create List Adapter
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>( //get layout for menu and add dialogue options for it
                        MainActivity.this,
                        android.R.layout.select_dialog_item);
                adapter.add("Share Book");
                adapter.add("Change Book Name");
                adapter.add("Delete Book");

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { //set cancel option
                        dialogInterface.dismiss();
                    }
                });

                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (i == 0) { //if user selects the share book option
                            File file = new File(fileList.get(itemPosition).getAbsolutePath());

                            Uri uri = FileProvider.getUriForFile(getApplicationContext(), "com.example.bookshelf.fileprovider", file);

                            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.setType("application/*");

                            Intent chooser = Intent.createChooser(intent, "Share Book"); //select share book intent and start the activity
                            startActivity(chooser);
                        }
                        else if (i == 1) { //if user selects change name option
                            final EditText et = new EditText(MainActivity.this);
                            final AlertDialog.Builder fnBuilder = new AlertDialog.Builder(MainActivity.this);

                            et.setText(fileList.get(itemPosition).getName());

                            fnBuilder.setTitle("Change Book Name.") //diplay option and text box settings
                                    .setMessage("Enter new book name.")
                                    .setCancelable(false)
                                    .setView(et)
                                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                                        public void onClick(DialogInterface dialog, int id) {
                                            String value = et.getText().toString();

                                            File file = new File(fileList.get(itemPosition).getAbsolutePath());
                                            File newFile = new File(file.getParent() + "/" + value + ".pdf");

                                            if(file.renameTo(newFile)) { //change file name to the one made by user
                                                fileList.clear();
                                                getfile(dir);
                                                obj_adapter.notifyDataSetChanged();
                                                Toast.makeText(MainActivity.this, "Book has been renamed.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, "Renaming has failed, try again", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            AlertDialog alert = fnBuilder.create();
                            alert.show();
                        }
                        else { //if user picks delete option
                            AlertDialog.Builder fdBuilder = new AlertDialog.Builder(MainActivity.this);

                            fdBuilder.setMessage("Would you like to delete the book?"); //display message
                            fdBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            fdBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    File file = new File(fileList.get(itemPosition).getAbsolutePath()); //search list and select file

                                    try { //delete file, and reset the list
                                        file.delete();
                                        fileList.clear();
                                        getfile(dir);
                                        obj_adapter.notifyDataSetChanged();
                                        Toast.makeText(MainActivity.this, "Book has been deleted", Toast.LENGTH_SHORT).show();
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(MainActivity.this, "Deletion failed, try again", Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });
                            AlertDialog dialog = fdBuilder.create();
                            dialog.show();
                        }
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        lv_pdf.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //if user short clicks an item on file list

                Intent intent = new Intent(getApplicationContext(), ViewPDFFiles.class); //continue to PDF display class
                intent.putExtra("position", position);
                startActivity(intent);

            }
        });
    }

    private void sortArrayList() { //the sort function for the pdf list
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) { //compare names of files lexicographically
                return o1.getName().compareTo(o2.getName());
            }
        });

        obj_adapter.notifyDataSetChanged();
    }

    private String[] permissions = { //needed permissions to obtain
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int MULTIPLE_PERMISSIONS = 101;

    private boolean permission_fn() {
        int result;
        List<String> permissionList = new ArrayList<>(); //make permission list to store what permissions we need

        for (String pm : permissions) { //add permissions needed to the list
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED)
                permissionList.add(pm);
        }
        if (!permissionList.isEmpty()) { //request permission here
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else { //if everything is ready, create adapter and get file list
            bolean_permission = true;
            getfile(dir);
            obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
            lv_pdf.setAdapter(obj_adapter);
        }
        return true;


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { //upon installation, display what is needed
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    boolean permissionAllow = true;
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) { //after sorting through permissions list, if found then ask user to display permission
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                permissionAllow = false;
                                Toast.makeText(this, "Please Allow the Permission", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    if(permissionAllow) { //if they allow permission, start to get all data and the adapter
                        bolean_permission = true;
                        getfile(dir);
                        obj_adapter = new PDFAdapter(getApplicationContext(), fileList);
                        lv_pdf.setAdapter(obj_adapter);
                    }
                } else {
                    Toast.makeText(this, "Please Allow the Permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public ArrayList<File> getfile(File dir) { //function to get files from storage
        File listFile[] = dir.listFiles();

        if (listFile != null && listFile.length > 0) {

            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) { //if file is directory, then pass directory and search it
                    getfile(listFile[i]);
                } else {
                    boolean booleanpdf = false;
                    if (listFile[i].getName().endsWith(".pdf")) { //get only pdf files

                        for (int j = 0; j < fileList.size(); j++) {

                            if (fileList.get(j).getName().equals(listFile[i].getName())) { //check for duplicates in arrays
                                booleanpdf = true;
                            }
                        }

                        if (booleanpdf) {
                            booleanpdf = false;
                        } else { //if no duplicates, add to array of files
                            fileList.add(listFile[i]);
                        }
                    }
                }
            }
        }
        return fileList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { //create menu for search option
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search); //get search option and its respective view
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { //if text is changing, get results
                obj_adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
    /* Unfinished bookmark code by Julz Y
    private void init() {
        arrayList = new ArrayList<BookmarkModel>();
        listView = (ListView) findViewById(R.id.bookmark_list);

        // Cursor to get Bookmar information
        Cursor cursor = getContentResolver().query(
                Browser.BOOKMARKS_URI,
                new String[] { Browser.BookmarkColumns.TITLE,
                        Browser.BookmarkColumns.URL }, null, null, null);

        // Note : " Browser.BookmarkColumns.BOOKMARK " - this will return 0 or
        // 1. '1' indicates a bookmark and '0' indicates history item.

        try {
            // Now loop to all items using cursor
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    // Add Bookmark title and Bookmark Url
                    arrayList.add(new BookmarkModel(cursor.getString(0), cursor
                            .getString(1)));

                } while (cursor.moveToNext()); // Move to next
            }
        } finally {

            // Close the cursor after use
            cursor.close();
        }

        Bookmark_Adapter adapter = new Bookmark_Adapter(MainActivity.this,
                arrayList);
        listView.setAdapter(adapter);// Set adapter
    }
    }*/

}
