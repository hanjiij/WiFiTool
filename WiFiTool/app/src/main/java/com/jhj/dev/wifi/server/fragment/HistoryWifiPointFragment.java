package com.jhj.dev.wifi.server.fragment;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HJ on 2015/8/15.
 */
public class HistoryWifiPointFragment
        extends Fragment {

    private View view;
    private ListView listView;
    private SQLiteDatabase db;
    private myBaseAdapter adapter;
    private List<Map<String, String>> list;
    private Map<String, String> map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        view = inflater.inflate(R.layout.layout_listview, null);
        listView = (ListView) view.findViewById(R.id.myListview);

        adapter = getAdapter();

        listView.setAdapter(adapter);

        return view;
    }

    /**
     * 构造adapter
     *
     * @return 返回listview使用的adapter
     */
    private myBaseAdapter getAdapter() {

        list = new ArrayList<>();

        db = new MyDatabase(getActivity(), "Points").getWritableDatabase();

        Cursor cursor = db.query("PointsSql", new String[]{"ssid", "mac",
                "Lat", "Lng", "address"}, null, null, null, null, "address ASC");

        int i = 0;

        while (cursor.moveToNext()) {

            map = new HashMap<>();

            map.put("ssid", cursor.getString(0));
            map.put("mac", cursor.getString(1));
            map.put("Lat", cursor.getString(2));

            map.put("Lng", cursor.getString(3));

            map.put("address", cursor.getString(4));

            list.add(map);
        }

//        SimpleAdapter adapter = new SimpleAdapter(getActivity(), list,
//                android.R.layout.simple_list_item_1,
//                new String[]{"ssid", "mac", "address"}, new int[]{android.R.id.text1});

        adapter = new myBaseAdapter();

        return adapter;
    }

    private class myBaseAdapter
            extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Map<String, String> getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = new TextView(getActivity());

            textView.setText(
                    position + 1 + "、\n" + "wifi名称 ：" + getItem(position).get("ssid") +
                            "\nMac地址：" +
                            getItem(position).get("mac") + "\n所在位置：" +
                            getItem(position).get("address"));
            return textView;
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (db == null) {

            db = new MyDatabase(getActivity(), "Points").getWritableDatabase();
        }

        adapter.notifyDataSetInvalidated(); // 更新adapter数据
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();

        db.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.wifi_location_history_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
