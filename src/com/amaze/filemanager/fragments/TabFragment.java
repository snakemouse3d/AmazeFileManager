package com.amaze.filemanager.fragments;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amaze.filemanager.R;
import com.amaze.filemanager.activities.MainActivity;
import com.amaze.filemanager.adapters.TabSpinnerAdapter;
import com.amaze.filemanager.database.Tab;
import com.amaze.filemanager.database.TabHandler;
import com.amaze.filemanager.services.asynctasks.ZipHelperTask;
import com.amaze.filemanager.utils.Futils;
import com.amaze.filemanager.utils.Shortcuts;
import com.amaze.filemanager.utils.ZipObj;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Arpit on 15-12-2014.
 */
public class TabFragment extends android.support.v4.app.Fragment {

    public  List<Fragment> fragments = new ArrayList<Fragment>();
    public ScreenSlidePagerAdapter mSectionsPagerAdapter;
    Futils utils = new Futils();
    public ViewPager mViewPager;
    SharedPreferences Sp;
    String path;
    int currenttab;
    MainActivity mainActivity;
    TabSpinnerAdapter tabSpinnerAdapter;
    public ArrayList<String> tabs=new ArrayList<String>();
    public int theme1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.tabfragment,
                container, false);
        Sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int theme=Integer.parseInt(Sp.getString("theme","0"));
        theme1 = theme;
        if (theme == 2) {
            if(hour<=6 || hour>=18) {
                theme1 = 1;
            } else
                theme1 = 0;
        }
        mViewPager = (ViewPager) rootView.findViewById(R.id.pager);
        if (getArguments() != null){
            path = getArguments().getString("path");
        }
        mainActivity = ((MainActivity)getActivity());
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            public void onPageScrolled(int p1, float p2, int p3) {
                try {
                    String name=fragments.get(mViewPager.getCurrentItem()).getClass().getName();
                    if(name.contains("Main")) {
                        Main ma = ((Main) fragments.get(mViewPager.getCurrentItem()));
                        if(ma.mActionMode!=null) {
                            ma.mActionMode.finish();
                            ma.mActionMode=null;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void onPageSelected(int p1) { // TODO: Implement this								// method
                currenttab=p1;
                Sp.edit().putInt("currenttab",currenttab).commit();
                mainActivity.supportInvalidateOptionsMenu();
                try {
                    updateSpinner();
                } catch (Exception e) {
                   // e.printStackTrace();
                }
                String name=fragments.get(p1).getClass().getName();
                if(name.contains("Main")){
                    Main ma = ((Main) fragments.get(p1));
                    if (ma.current != null) {
                        try {
                            mainActivity.updateDrawer(ma.current);

                        } catch (Exception e) {
                            //       e.printStackTrace();5
                        }
                    }
                }

            }

            public void onPageScrollStateChanged(int p1) {
                // TODO: Implement this method
            }
        });
        if (savedInstanceState == null) {
            mSectionsPagerAdapter = new ScreenSlidePagerAdapter(
                    getActivity().getSupportFragmentManager());
            TabHandler tabHandler=new TabHandler(getActivity(),null,null,1);
            List<Tab> tabs1=tabHandler.getAllTabs();
            int i=tabs1.size();
            if(i==0) {
                addTab(new Tab(1,"",mainActivity.list.get(0),mainActivity.list.get(0)),1,"");
                addTab(new Tab(2,"","/","/"),2,"");
            }
            else{
                if(path!=null && path.length()!=0){
                    Tab tab=tabHandler.findTab(1);
                    tab.setPath(path);
                    addTab(tab,1,"");
                }
                else
                addTab(tabHandler.findTab(1),1,"");
                addTab(tabHandler.findTab(2),2,"");
            }



            mViewPager.setAdapter(mSectionsPagerAdapter);

                int l=Sp.getInt("currenttab",0);
                try {
                    mViewPager.setCurrentItem(l,true);
                } catch (Exception e) {
                    e.printStackTrace();
                }


        } else {
            fragments.clear();
          tabs= savedInstanceState.getStringArrayList("tabs");
            for(int i=0;i<tabs.size();i++){
                try {
                    fragments.add(i, getActivity().getSupportFragmentManager().getFragment(savedInstanceState, "tab"+i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mSectionsPagerAdapter = new ScreenSlidePagerAdapter(
                    getActivity().getSupportFragmentManager());

            mViewPager.setAdapter(mSectionsPagerAdapter);
            int pos1=savedInstanceState.getInt("pos",0);
            mViewPager.setCurrentItem(pos1);
            mSectionsPagerAdapter.notifyDataSetChanged();

        }
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        tabHandler.close();
    }
    TabHandler tabHandler;

    public void updatepaths(){
                tabHandler = new TabHandler(getActivity(), null, null, 1);
        int i=1;
        ArrayList<String> items=new ArrayList<String>();
        tabHandler.clear();for(Fragment fragment:fragments){
            if(fragment.getClass().getName().contains("Main")){
                Main m=(Main)fragment;
                items.add(m.current);
                tabHandler.addTab(new Tab(i,m.current,m.current,m.home));
                i++;
            }}
        try {
            tabSpinnerAdapter=new TabSpinnerAdapter(mainActivity.getSupportActionBar().getThemedContext(), R.layout.rowlayout,items,mainActivity.tabsSpinner,this);
            mainActivity.tabsSpinner.setAdapter(tabSpinnerAdapter);
            mainActivity.tabsSpinner.setSelection(mViewPager.getCurrentItem());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Sp.edit().putInt("currenttab",currenttab).apply();

        System.out.println("updatepaths");
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int i = 0;
        if (fragments != null && fragments.size() !=0) {
            for (Fragment fragment : fragments) {
                getActivity().getSupportFragmentManager().putFragment(outState, "tab" + i, fragment);
                i++;
            }
            outState.putStringArrayList("tabs", tabs);
            outState.putInt("pos", mViewPager.getCurrentItem());
        }Sp.edit().putInt("currenttab",currenttab).commit();
    }
    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        @Override
        public int getItemPosition (Object object)
        {int index = fragments.indexOf ((Fragment)object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            try {
                String name=fragments.get(position).getClass().getName();
                if(name.contains("Main")){
                Main ma = ((Main) fragments.get(position));
                if (ma.results) {
                    return utils.getString(getActivity(), R.string.searchresults);
                } else {
                    if (ma.current.equals("/")) {
                        return "Root";
                    } else {
                        return new File(ma.current).getName();
                    }
                }}else if(name.contains("ZipViewer")) {
                    ZipViewer ma = ((ZipViewer) fragments.get(position));

                    try {
                        return ma.f.getName();
                    } catch (Exception e) {
                        return "ZipViewer";
                      //  e.printStackTrace();
                    }

                }
                return fragments.get(position).getClass().getName();
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        public int getCount() {
            // TODO: Implement this method
            return fragments.size();
        }

        public ScreenSlidePagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            android.support.v4.app.Fragment f;
            f = fragments.get(position);
            return f;
        }

    }


    public void addTab(Tab text,int pos,String path) {
        android.support.v4.app.Fragment main = new Main();
        Bundle b = new Bundle();
        if (path != null && path.trim().length() != 0) {
            b.putString("path", path);

        }
        b.putString("lastpath",text.getPath());
        b.putString("home",text.getHome());
        b.putInt("no",pos);
        main.setArguments(b);
        fragments.add(main);
        tabs.add(main.getClass().getName());
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setOffscreenPageLimit(4);
    }
    public Fragment getTab() {
        return fragments.get(mViewPager.getCurrentItem());
    }
    public Fragment getTab1() {
        Fragment man = ( fragments.get(mViewPager.getCurrentItem()));
        return man;
    }
    public void updateSpinner(){
        System.out.println("updatespinner");
        ArrayList<String> items=new ArrayList<String>();
        items.add(((Main)fragments.get(0)).current);
        items.add(((Main)fragments.get(1)).current);
        tabSpinnerAdapter=new TabSpinnerAdapter(mainActivity.getSupportActionBar().getThemedContext(), R.layout.rowlayout,items,mainActivity.tabsSpinner,this);
        mainActivity.tabsSpinner.setAdapter(tabSpinnerAdapter);
        mainActivity.tabsSpinner.setSelection(mViewPager.getCurrentItem());
    }

}
