package de.derschimi.applibrary;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by daniel on 21.09.2014.
 */
public class AppFragment extends Fragment {
    private String name;

    // please note that fragment require empty constructor
    // it's because android framework will try to recreate your
    // fragment on configuration changes
    // if you want to pass argument to a fragment please use
    // a newInstance pattern just like google does:
    // http://developer.android.com/reference/android/app/Fragment.html
    public AppFragment(String name) {
        this.name = name;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.fragment, container, false);

        TextView tv = (TextView) v.findViewById(R.id.fragmentname);
        tv.setText(name);
        return v;
    }
}
