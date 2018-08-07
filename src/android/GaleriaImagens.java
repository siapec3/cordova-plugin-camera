package smsgi.com.br.cameraapp;

import android.app.Activity;
import android.os.Bundle;

import smsgi.com.br.cameraapp.reflect.Meta;

/**
 * Created by desenvolvimento10 on 03/07/18.
 */

public class GaleriaImagens extends Activity {

    private Activity activity;

    @Override
    protected void onCreate( Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(Meta.getResId(activity, "layout", "galeria_activity"));

//        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
//        recyclerView.setHasFixedSize(true);
//
//        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),2);
//        recyclerView.setLayoutManager(layoutManager);
//        ArrayList<CreateList> createLists = prepareData();
//        MyAdapter adapter = new MyAdapter(getApplicationContext(), createLists);
//        recyclerView.setAdapter(adapter);
    }
}
