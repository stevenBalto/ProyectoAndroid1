package com.example.proyecto;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Referenciar el ListView
        ListView listViewMenu = findViewById(R.id.listViewMenu);

        // 2. Crear la lista de ítems del menú
        List<MenuItem> menuItems = new ArrayList<>();
        // TODO: Agrega tus propios íconos a la carpeta res/drawable y reemplaza el '0' con R.drawable.<nombre_del_icono>
        menuItems.add(new MenuItem(0, getString(R.string.menu_clientes)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_productos)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_categorias)));
        menuItems.add(new MenuItem(0, getString(R.string.menu_usuarios)));
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
                    case 2: // Categorías
                        startActivity(new Intent(MainActivity.this, CategoriasActivity.class));
                        break;
                    case 3: // Usuarios
                        startActivity(new Intent(MainActivity.this, UsuariosActivity.class));
                        break;
                    case 4: // Facturación
                        startActivity(new Intent(MainActivity.this, FacturaActivity.class));
                        break;
                    case 5: // Salir
                        finish(); // Cierra la aplicación
                        break;
                }
            }
        });
    }
}
