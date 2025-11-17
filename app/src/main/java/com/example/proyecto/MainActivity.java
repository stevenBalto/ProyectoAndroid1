package com.example.proyecto;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

        LinearLayout menuContainer = findViewById(R.id.menuContainer);


        addMenuItem(menuContainer, R.drawable.rating_5957121, getString(R.string.menu_clientes), v -> startActivity(new Intent(MainActivity.this, ClientesActivity.class)));
        addMenuItem(menuContainer, R.drawable.shopping_1301306, getString(R.string.menu_productos), v -> startActivity(new Intent(MainActivity.this, ProductosActivity.class)));
        addMenuItem(menuContainer, R.drawable.invoice_12560218, getString(R.string.menu_facturacion), v -> startActivity(new Intent(MainActivity.this, FacturaActivity.class)));
        addMenuItem(menuContainer, R.drawable.exit_8835433, getString(R.string.menu_salir), v -> finish());
    }

    private void addMenuItem(LinearLayout container, int iconRes, String title, View.OnClickListener listener) {

        LayoutInflater inflater = LayoutInflater.from(this);
        View menuItemView = inflater.inflate(R.layout.menu_item, container, false);


        ImageView icon = menuItemView.findViewById(R.id.imageViewIcon);
        TextView text = menuItemView.findViewById(R.id.textViewTitle);

        icon.setImageResource(iconRes);
        text.setText(title);


        menuItemView.setOnClickListener(listener);


        container.addView(menuItemView);
    }
}
