package de.derschimi.applibrary;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel on 21.09.2014.
 */
public class ActionAdapter extends ArrayAdapter<String> {
    protected List<String> list = new ArrayList<String>();

    protected FragmentActivity parentFragmentActivity;

    public ActionAdapter(Context context, FragmentActivity activity) {
        super(context, 0);
        createItemsAndAddToList(context);

        this.parentFragmentActivity = activity;
    }

    public void createItemsAndAddToList(Context context) {
        list.add("Fragment 1");
        list.add("Fragment 2");
    }

    @Override
    public String getItem(int position) {
        return list.get(position);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.slidingmenuentry, null);
        }

        TextView title = (TextView) convertView.findViewById(R.id.row_title);

        title.setText(getItem(position));

        return convertView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public List<Fragment> getFragments() {
        List<Fragment> frags = new ArrayList<Fragment>();
        frags.add(new AppFragment("Fragment 1"));
        frags.add(new AppFragment("Fragment 2"));
        return frags;
    }
}
