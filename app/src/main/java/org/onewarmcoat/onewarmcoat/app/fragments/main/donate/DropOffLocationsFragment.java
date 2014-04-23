package org.onewarmcoat.onewarmcoat.app.fragments.main.donate;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.onewarmcoat.onewarmcoat.app.R;
import org.onewarmcoat.onewarmcoat.app.fragments.main.MapHostingFragment;

import java.util.List;

import butterknife.ButterKnife;

public class DropOffLocationsFragment extends MapHostingFragment {

    public DropOffLocationsFragment() {
        // Required empty public constructor
    }

    public static DropOffLocationsFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        DropOffLocationsFragment f = new DropOffLocationsFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_drop_off, container, false);
        ButterKnife.inject(this, v);

        return v;
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        super.onMapReady(map);

        ParseQuery query = new ParseQuery("DropOffAgency");

        //TODO: Restrict query to a geobox so the phone doesnt explode
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List list, ParseException e) {
                for (Object item : list) {
                    ParseObject it = (ParseObject) item;
                    ParseGeoPoint geoPoint = it.getParseGeoPoint("agencyGeoLocation");
                    MarkerOptions marker = new MarkerOptions();
                    LatLng ll = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
                    marker.position(ll);
                    marker.title(it.getString("agencyName"));
                    marker.snippet(it.getString("agencyAddress"));
                    marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    map.addMarker(marker);
                }
            }
        });


    }
}
