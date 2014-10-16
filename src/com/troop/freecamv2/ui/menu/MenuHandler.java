package com.troop.freecamv2.ui.menu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.troop.freecam.R;
import com.troop.freecamv2.camera.CameraUiWrapper;
import com.troop.freecamv2.camera.parameters.I_ParametersLoaded;
import com.troop.freecamv2.ui.AppSettingsManager;
import com.troop.freecamv2.ui.MainActivity_v2;
import com.troop.freecamv2.ui.TextureView.ExtendedSurfaceView;

import java.util.ArrayList;

/**
 * Created by troop on 19.08.2014.
 */
public class MenuHandler  implements ExpandableListView.OnChildClickListener, ListView.OnItemClickListener, I_ParametersLoaded
{
    MainActivity_v2 context;
    CameraUiWrapper cameraUiWrapper;
    MenuCreator menuCreator;
    ExtendedSurfaceView surfaceView;

    /**
     * this holds the mainmenu
     */
    ExpandableListView expandableListView;
    ExpandableListViewMenuAdapter expandableListViewMenuAdapter;
    /**
     * this hold the main submenu
     */
    ListView listView;

    int mShortAnimationDuration = 200;

    ExpandableChild selectedChild;
    AppSettingsManager appSettingsManager;

    public MenuHandler(MainActivity_v2 context, CameraUiWrapper cameraUiWrapper, AppSettingsManager appSettingsManager, ExtendedSurfaceView surfaceView)
    {
        this.context = context;
        this.cameraUiWrapper = cameraUiWrapper;
        this.appSettingsManager = appSettingsManager;
        this.surfaceView = surfaceView;
        cameraUiWrapper.camParametersHandler.ParametersEventHandler.AddParametersLoadedListner(this);
        menuCreator = new MenuCreator(context, cameraUiWrapper, appSettingsManager);
    }

    private ArrayList<ExpandableGroup> createMenu() {
        ArrayList<ExpandableGroup> grouplist = new ArrayList<ExpandableGroup>();
        grouplist.add(menuCreator.CreatePictureSettings(surfaceView));
        grouplist.add(menuCreator.CreateModeSettings());
        grouplist.add(menuCreator.CreateQualitySettings());
        //grouplist.add(menuCreator.CreatePreviewSettings(surfaceView));
        return grouplist;
    }

    //Expendable LIstview click
    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
    {
        //get the group
        ExpandableGroup group = (ExpandableGroup)expandableListViewMenuAdapter.getGroup(groupPosition);
        //get the child from group
        selectedChild = group.getItems().get(childPosition);


        //get values from child attached parameter
        String[] values = selectedChild.getParameterHolder().GetValues();
        //set values to the adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                R.layout.simpel_list_item_v2, R.id.textView_simple_list_item_v2, values);
        //attach adapter to the listview and fill
        listView.setAdapter(adapter);
        hideMenuAndShowSubMenu();
        return false;
    }


    private void hideMenuAndShowSubMenu()
    {
        context.settingsLayoutHolder.removeView(expandableListView);
        context.settingsLayoutHolder.addView(listView);
       /* expandableListView.setAlpha(1f);
        expandableListView.setVisibility(View.VISIBLE);
        expandableListView.animate()
                .alpha(0f)
                .translationXBy(-expandableListView.getWidth())
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        expandableListView.setVisibility(View.GONE);
                        showSubMenu();
                    }
                });*/
    }

    private void showSubMenu()
    {
        listView.setAlpha(0f);
        listView.setVisibility(View.VISIBLE);
        listView.animate()
                .alpha(1f)
                .translationX(0)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {

                    }
                });
    }

    private void hideSubMenuAndShowMenu()
    {
        context.settingsLayoutHolder.removeView(listView);
        context.settingsLayoutHolder.addView(expandableListView);
        /*listView.setAlpha(1f);
        listView.setVisibility(View.VISIBLE);
        listView.animate()
                .alpha(0f)
                .translationX(-expandableListView.getWidth())
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        listView.setVisibility(View.GONE);
                        showMenu();
                    }
                });*/
    }

    private void showMenu()
    {
        expandableListView.setAlpha(0f);
        expandableListView.setVisibility(View.VISIBLE);
        expandableListView.animate()
                .alpha(1f)
                .translationX(0)
                .setDuration(mShortAnimationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }
                });
    }

    //this get fired when the cameraparametershandler has finished loading the parameters and all values are availible
    @Override
    public void ParametersLoaded()
    {
        ArrayList<ExpandableGroup> grouplist = createMenu();
        expandableListViewMenuAdapter = new ExpandableListViewMenuAdapter(context, grouplist);
        expandableListView = (ExpandableListView) context.settingsLayoutHolder.findViewById(R.id.expandableListViewSettings);
        expandableListView.setAdapter(expandableListViewMenuAdapter);
        expandableListView.setOnChildClickListener(this);

        if (listView == null) {
            listView = (ListView) context.settingsLayoutHolder.findViewById(R.id.subMenuSettings);
            listView.setOnItemClickListener(this);
            context.settingsLayoutHolder.removeView(listView);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if (selectedChild != null) {
            String value = (String) listView.getItemAtPosition(position);
            selectedChild.setValue(value);
            selectedChild = null;
            hideSubMenuAndShowMenu();
        }

    }


}
