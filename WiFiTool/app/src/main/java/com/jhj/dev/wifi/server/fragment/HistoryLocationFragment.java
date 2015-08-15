package com.jhj.dev.wifi.server.fragment;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.jhj.dev.wifi.server.R;
import com.jhj.dev.wifi.server.mydatabase.MyDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 韩吉
 */
@SuppressLint("InflateParams")
public class HistoryLocationFragment extends Fragment {

    private View view;
    private ListView listView;
    private SQLiteDatabase db;
    private SimpleAdapter adapter;
    private List<Map<String, String>> list;
    private Map<String, String> map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // TODO Auto-generated method stub

        setHasOptionsMenu(true);

        System.out.println("onCreateView");

        view = inflater.inflate(R.layout.layout_listview, null);
        listView = (ListView) view.findViewById(R.id.myListview);

        adapter = getAdapter();

        listView.setAdapter(adapter);

        setListener();

        return view;
    }

    /**
     * 设置lisiview的事件响应函数
     */
    private void setListener()
    {
        // TODO Auto-generated method stub
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                // TODO Auto-generated method stub

                getActivity().getIntent().putExtra("isFirstLocExtra", false); // 传递数据给地图，不是首次定位

                getActivity().getIntent().putExtra("latitude", list.get(arg2).get("Lat"));
                getActivity().getIntent().putExtra("longitude", list.get(arg2).get("Lng"));

                System.out.println("存储地址成功");

                getActivity().getSupportFragmentManager().popBackStack();

            }
        });
    }

    /**
     * 构造adapter
     *
     * @return 返回listview使用的adapter
     */
    private SimpleAdapter getAdapter()
    {

        list = new ArrayList<>();

        db = new MyDatabase(getActivity(), "Points").getWritableDatabase();

        Cursor cursor =
                db.query("HistoryPointsSql", new String[]{"address", "Lat", "Lng"}, null, null,
                         null, null, null);

        int i = 0;

        while (cursor.moveToNext()) {

            map = new HashMap<>();

            map.put("address", (++i) + "、" + cursor.getString(0));
            map.put("Lat", cursor.getString(1));
            map.put("Lng", cursor.getString(2));

            list.add(map);
        }

        SimpleAdapter adapter =
                new SimpleAdapter(getActivity(), list, android.R.layout.simple_list_item_1,
                                  new String[]{"address"}, new int[]{android.R.id.text1});

        return adapter;
    }

    @Override
    public void onResume()
    {
        // TODO Auto-generated method stub
        super.onResume();

        if (db == null) {

            db = new MyDatabase(getActivity(), "Points").getWritableDatabase();
        }

        adapter.notifyDataSetInvalidated(); // 更新adapter数据
    }

    @Override
    public void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();

        db.close();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.wifi_location_history_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {


        return super.onOptionsItemSelected(item);
    }

}
