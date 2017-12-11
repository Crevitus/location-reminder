package com.crevitus.locationreminder.UI;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class PlaceAutocompleteAdapter
        extends ArrayAdapter<PlaceAutocompleteAdapter.PlaceAutocompleteItem> implements Filterable {

    private ArrayList<PlaceAutocompleteItem> _resultList;
    private GoogleApiClient _APIClient;
    private LatLngBounds _boundary;
    private AutocompleteFilter _placeFilter;

    public PlaceAutocompleteAdapter(Context context, int resource, LatLngBounds bounds,
            AutocompleteFilter filter) {
        super(context, resource);
        _boundary = bounds;
        _placeFilter = filter;
    }

    public void setAPIClient(GoogleApiClient googleApiClient) {
        if (googleApiClient == null || !googleApiClient.isConnected()) {
            _APIClient = null;
        } else {
            _APIClient = googleApiClient;
        }
    }

    @Override
    public int getCount() {
        return _resultList.size();
    }

    @Override
    public PlaceAutocompleteItem getItem(int position) {
        return _resultList.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                //dont query if constraint is null
                if (constraint != null) {
                    //search for results
                    _resultList = getAutocomplete(constraint);
                    if (_resultList != null) {
                        results.values = _resultList;
                        results.count = _resultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    notifyDataSetChanged();
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    private ArrayList<PlaceAutocompleteItem> getAutocomplete(CharSequence constraint) {
        if (_APIClient != null) {

            // Submit the query to the autocomplete API
            PendingResult<AutocompletePredictionBuffer> results =
                    Places.GeoDataApi
                            .getAutocompletePredictions(_APIClient, constraint.toString(),
                                    _boundary, _placeFilter);

            // await for a result from the API.
            AutocompletePredictionBuffer autocompletePredictions = results
                    .await(60, TimeUnit.SECONDS);

            // Confirm that the query completed successfully, otherwise return null
            final Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                autocompletePredictions.release();
                return null;
            }

            // Copy the results into helper class model
            Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
            ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
            while (iterator.hasNext()) {
                AutocompletePrediction prediction = iterator.next();
                resultList.add(new PlaceAutocompleteItem(prediction.getPlaceId(),
                        prediction.getDescription()));
            }

            // Release the buffer now that all data has been copied.
            autocompletePredictions.release();

            return resultList;
        }
        return null;
    }

     //Holder for Places Geo Data Autocomplete API results.
    class PlaceAutocompleteItem {

        private String _placeId, _description;

        PlaceAutocompleteItem(String placeId, String description) {
            _placeId = placeId;
            _description = description;
        }

        public String getPlaceId()
        {
            return _placeId;
        }

        @Override
        public String toString() {
            return _description;
        }
    }
}
