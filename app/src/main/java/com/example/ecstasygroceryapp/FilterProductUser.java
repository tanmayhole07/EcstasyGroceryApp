package com.example.ecstasygroceryapp;

import android.widget.Filter;


import com.example.ecstasygroceryapp.Models.ModelProduct;
import com.example.ecstasygroceryapp.User.Adapter.AdapterProductUser;

import java.util.ArrayList;

public class FilterProductUser extends Filter {

    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence!=null && charSequence.length()>0){
            charSequence = charSequence.toString().toUpperCase();

            ArrayList<ModelProduct> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){

                if (filterList.get(i).getProductTitle().toUpperCase().contains(charSequence) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(charSequence)){
                    filterModels.add(filterList.get(i));
                }
            }

            results.count = filterModels.size();
            results.values = filterModels;

        }else {
            results.count = filterList.size();
            results.values = filterList;
        }

        return results;

    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapter.productsList = (ArrayList<ModelProduct>)filterResults.values;

        adapter.notifyDataSetChanged();
    }
}
