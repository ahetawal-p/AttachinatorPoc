package com.example.myswiplelist.util;

import com.example.myswipelist.R;
import com.example.myswiplelist.data.DeferDialogModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by beckchen on 1/28/15.
 */
public class DeferDialogUtil {

    public List<DeferDialogModel> getDeferDialogItems(){
        List<DeferDialogModel> dialogItems = new ArrayList<DeferDialogModel>();
        DeferDialogModel model = new DeferDialogModel();
        model.setItemLabel("Later Today");
        model.setItemImageResourceId(R.drawable.clock118);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("Tomorrow Eve");
        model.setItemImageResourceId(R.drawable.moon144);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("Tomorrow");
        model.setItemImageResourceId(R.drawable.cup16);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("This Weekend");
        model.setItemImageResourceId(R.drawable.sun94);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("Next Week");
        model.setItemImageResourceId(R.drawable.briefcase56);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("In a Month");
        model.setItemImageResourceId(R.drawable.small58);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("Someday");
        model.setItemImageResourceId(R.drawable.water70);
        dialogItems.add(model);

        //This is empty cell
        model = new DeferDialogModel();
        model.setItemLabel("");
        model.setItemImageResourceId(0);
        dialogItems.add(model);

        model = new DeferDialogModel();
        model.setItemLabel("Pick a Date");
        model.setItemImageResourceId(R.drawable.love86);
        dialogItems.add(model);

        return dialogItems;
    }
}
