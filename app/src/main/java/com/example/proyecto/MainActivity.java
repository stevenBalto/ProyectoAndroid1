package com.example.proyecto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        while (cursor.moveToNext()) {
            Log.d("TABLA", cursor.getString(0));
        }

        cursor.close();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Referenciar el ListView
        ListView listViewMenu = findViewById(R.id.listViewMenu);

        // 2. Crear la lista de ítems del menú
        List<MenuItem> menuItems = new ArrayList<>();
        // TODO: Agrega tus propios íconos a la carpeta res/drawable y reemplaza el '0' con R.drawable.<nombre_del_icono>
        menuItems.add(new MenuItem(0, getString(R.string.menu_clientes)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_productos)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_facturacion)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_salir)));

        // 3. Crear y asignar el adaptador
        MenuAdapter adapter = new MenuAdapter(this, menuItems);
        listViewMenu.setAdapter(adapter);

        // 4. Configurar el listener para los clics en los ítems
        listViewMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 5. Determinar la acción según la posición del ítem
                switch (position) {
                    case 0: // Clientes
                        startActivity(new Intent(MainActivity.this, ClientesActivity.class));
                        break;
                    case 1: // Productos
                        startActivity(new Intent(MainActivity.this, ProductosActivity.class));
                        break;
                    case 2: // Facturación
                        startActivity(new Intent(MainActivity.this, FacturaActivity.class));
                        break;
                    case 3: // Salir
                        finish(); // Cierra la aplicación
                        break;
                }
            }
        });
    }

    // Modelo para cada ítem del menú
    public class MenuItem {
        private int icon; // Recurso drawable del ícono
        private String title; // Título del ítem

        public MenuItem(int icon, String title) {
            this.icon = icon;
            this.title = title;
        }

        public int getIcon() {
            return icon;
        }

        public String getTitle() {
            return title;
        }
    }

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
}
