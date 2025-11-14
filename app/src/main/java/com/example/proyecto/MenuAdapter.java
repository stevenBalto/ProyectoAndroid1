package com.example.proyecto;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

// Adaptador personalizado para el ListView del menú
public class MenuAdapter extends ArrayAdapter<MenuItem> {

    public MenuAdapter(Context context, List<MenuItem> menuItems) {
        super(context, 0, menuItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Obtener el ítem actual
        MenuItem menuItem = getItem(position);

        // Inflar la vista si es necesario
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_item, parent, false);
        }

        // Referenciar los elementos de la vista
        ImageView imageViewIcon = convertView.findViewById(R.id.imageViewIcon);
        TextView textViewTitle = convertView.findViewById(R.id.textViewTitle);

        // Asignar los datos del ítem a la vista
        imageViewIcon.setImageResource(menuItem.getIcon());
        textViewTitle.setText(menuItem.getTitle());

        return convertView;
    }
}
