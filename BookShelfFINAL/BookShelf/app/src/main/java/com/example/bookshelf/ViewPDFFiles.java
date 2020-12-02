package com.example.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;

public class ViewPDFFiles extends AppCompatActivity { //class to view pdfs

    PDFView pdfView;
    int position = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pdffiles); //assign xml layout

        pdfView = findViewById(R.id.pdfView); //call the PDFView function
        position = getIntent().getIntExtra("position", -1);

        displayPDF(); //perform the display

    }

    private void displayPDF() {

        pdfView.fromFile(MainActivity.fileList.get(position))
                .enableSwipe(true)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();

    }
}
