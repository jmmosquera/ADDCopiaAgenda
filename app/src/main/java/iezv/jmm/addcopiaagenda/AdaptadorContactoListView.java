package iezv.jmm.addcopiaagenda;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorContactoListView extends ArrayAdapter<Contacto> {

    private List <Contacto> data;

    private int layout;
    private LayoutInflater inflater;


    public AdaptadorContactoListView(@NonNull Context context, List<Contacto> data) {
        this(context, R.layout.item, data);
    }

    public AdaptadorContactoListView(@NonNull Context context, int layout, List<Contacto> data) {
        super(context, layout, data);
        this.data = data;

        this.layout = layout;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView== null){
            convertView = inflater.inflate(layout, null);
        }
        Contacto c = data.get(position);
        TextView tv = convertView.findViewById(R.id.textView2);
        tv.setText(c.getNombre());
        TextView tv2 = convertView.findViewById(R.id.textView);
        tv2.setText(c.getTelefono());
        return convertView;
    }



}
