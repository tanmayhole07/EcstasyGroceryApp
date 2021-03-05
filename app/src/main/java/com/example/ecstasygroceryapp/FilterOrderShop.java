package com.example.ecstasygroceryapp;

import android.widget.Filter;


import com.example.ecstasygroceryapp.Models.ModelOrderShop;
import com.example.ecstasygroceryapp.Seller.Adapter.AdapterOrderShop;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults results = new FilterResults();
        if (charSequence!=null && charSequence.length()>0){
            charSequence = charSequence.toString().toUpperCase();

            ArrayList<ModelOrderShop> filterModels = new ArrayList<>();
            for (int i=0; i<filterList.size(); i++){

                if (filterList.get(i).getOrderStatus().toUpperCase().contains(charSequence)){
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

        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>)filterResults.values;

        adapter.notifyDataSetChanged();
    }
}
