package com.example.bookshelf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PDFAdapter extends ArrayAdapter<File> implements Filterable {

    // declarations
    Context context;
    ViewHolder viewHolder;
    ArrayList<File> al_pdf;
    ArrayList<File> al_pdfFull;

    public PDFAdapter(Context context, ArrayList<File> al_pdf) { //constructor passing list of files
        super(context, R.layout.adapter_pdf, al_pdf);
        this.context = context;
        this.al_pdf = al_pdf;
        al_pdfFull = new ArrayList<>(al_pdf);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    } //get position of array

    @Override
    public int getViewTypeCount() { //get size of array
        if(al_pdf.size()>0){
            return al_pdf.size();
        }
        else return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) { //get view of selected pdf file

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_pdf,parent, false); //get view from adapter xml file
            viewHolder = new ViewHolder();

            viewHolder.tv_filename = convertView.findViewById(R.id.tv_name); // set textview name
            convertView.setTag(viewHolder);

        }
        else{
            viewHolder = (ViewHolder)convertView.getTag();
        }

        viewHolder.tv_filename.setText(al_pdf.get(position).getName()); //get position of file being read and set the text in holder
        return convertView;
    }

    public class ViewHolder{
        TextView tv_filename;
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    } //filter class for search function

    private final Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { //results after searching
            ArrayList<File> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) { //add array if filtered list is empty
                filteredList.addAll(al_pdfFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (File item : al_pdfFull) { //if file name has user inputted text, then add it to the filter list
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList; //results after filtering

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) { //add results to file list
            al_pdf.clear();
            al_pdf.addAll((List)results.values);
            notifyDataSetChanged();
        }
    };
}
