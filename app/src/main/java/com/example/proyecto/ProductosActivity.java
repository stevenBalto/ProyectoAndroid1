package com.example.proyecto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ProductosActivity extends AppCompatActivity {

    LinearLayout layoutLista;
    View layoutFormulario;
    EditText txtBuscar, txtCodigo, txtNombre, txtPrecio;
    ListView listViewProductos;
    TextView btnAgregar, btnEditar, btnEliminar, btnSalir;
    Button btnGuardar, btnCancelar;

    ArrayList<Producto> listaProductos = new ArrayList<>();
    ProductoAdapter adapter;

    DatabaseHelper db;
    String codigoSeleccionado = null;
    View vistaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        db = new DatabaseHelper(this);

        layoutLista      = findViewById(R.id.layoutLista);
        layoutFormulario = findViewById(R.id.layoutFormulario);

        txtBuscar = findViewById(R.id.txtBuscar);
        txtCodigo = findViewById(R.id.txtCodigo);
        txtNombre = findViewById(R.id.txtNombre);
        txtPrecio = findViewById(R.id.txtPrecio);

        listViewProductos = findViewById(R.id.listViewProductos);

        btnAgregar  = findViewById(R.id.btnAgregar);
        btnEditar   = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir    = findViewById(R.id.btnSalir);
        btnGuardar  = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        // --- BUSCADOR ---
        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarProductos(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // --- SELECCIÓN EN LISTA ---
        listViewProductos.setOnItemClickListener((parent, view, position, id) -> {
            if (vistaSeleccionada != null) {
                vistaSeleccionada.setBackgroundColor(Color.TRANSPARENT);
            }
            view.setBackgroundColor(Color.LTGRAY);
            vistaSeleccionada = view;

            Producto p = listaProductos.get(position);
            codigoSeleccionado = p.getCodigo();
            actualizarEstadoBotones(true);
        });

        // Cargar listado inicial
        cargarProductos("");

        // --- BOTÓN AGREGAR ---
        btnAgregar.setOnClickListener(v -> mostrarFormulario(null));

        // --- BOTÓN EDITAR ---
        btnEditar.setOnClickListener(v -> {
            if (codigoSeleccionado != null) {
                Producto p = buscarPorCodigo(codigoSeleccionado);
                mostrarFormulario(p);
            }
        });

        // --- BOTÓN ELIMINAR ---
        btnEliminar.setOnClickListener(v -> {
            if (codigoSeleccionado != null) {
                SQLiteDatabase writable = db.getWritableDatabase();
                writable.delete("productos", "codigo=?", new String[]{codigoSeleccionado});
                txtBuscar.setText("");
                cargarProductos("");
            }
        });

        // --- BOTÓN SALIR ---
        btnSalir.setOnClickListener(v -> {
            if (layoutFormulario.getVisibility() == View.VISIBLE) {
                mostrarLista();
            } else {
                finish();
            }
        });

        btnGuardar.setOnClickListener(v -> guardarProducto());
        btnCancelar.setOnClickListener(v -> mostrarLista());
        
        // Handle back press using the new OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (layoutFormulario.getVisibility() == View.VISIBLE) {
                    mostrarLista();
                } else {
                    finish(); // Finish the activity
                }
            }
        });
    }

    private void actualizarEstadoBotones(boolean habilitados) {
        btnEditar.setEnabled(habilitados);
        btnEliminar.setEnabled(habilitados);
        btnEditar.setAlpha(habilitados ? 1f : 0.5f);
        btnEliminar.setAlpha(habilitados ? 1f : 0.5f);
    }

    private void mostrarFormulario(Producto p) {
        layoutLista.setVisibility(View.GONE);
        layoutFormulario.setVisibility(View.VISIBLE);

        if (p == null) {
            txtCodigo.setText("");
            txtNombre.setText("");
            txtPrecio.setText("");
            txtCodigo.setEnabled(true);   // alta (INSERT)
        } else {
            txtCodigo.setText(p.getCodigo());
            txtNombre.setText(p.getNombre());
            // --- AJUSTE: Mostrar precio como entero ---
            txtPrecio.setText(String.valueOf((int) p.getPrecio()));
            txtCodigo.setEnabled(false);  // edición (UPDATE)
        }
    }

    private void mostrarLista() {
        layoutFormulario.setVisibility(View.GONE);
        layoutLista.setVisibility(View.VISIBLE);
    }

    private void guardarProducto() {
        String cod       = txtCodigo.getText().toString().trim();
        String nom       = txtNombre.getText().toString().trim();
        String precioStr = txtPrecio.getText().toString().trim();

        if (cod.isEmpty() || nom.isEmpty() || precioStr.isEmpty()) return;

        double precio = Double.parseDouble(precioStr);

        SQLiteDatabase writable = db.getWritableDatabase();
        boolean esUpdate = !txtCodigo.isEnabled(); // true = editar, false = insertar

        if (esUpdate) {
            writable.execSQL(
                    "UPDATE productos SET nombre=?, precio=? WHERE codigo=?",
                    new Object[]{nom, precio, cod}
            );
        } else {
            writable.execSQL(
                    "INSERT INTO productos(codigo, nombre, precio) VALUES(?,?,?)",
                    new Object[]{cod, nom, precio}
            );
        }

        txtBuscar.setText("");
        cargarProductos("");
        mostrarLista();
    }

    private void cargarProductos(String query) {
        listaProductos.clear();
        SQLiteDatabase readable = db.getReadableDatabase();
        Cursor c;

        if (query.trim().isEmpty()) {
            c = readable.rawQuery(
                    "SELECT codigo, nombre, precio FROM productos ORDER BY CAST(codigo AS INTEGER)",
                    null
            );
        } else {
            String likeQuery = "%" + query + "%";
            c = readable.rawQuery(
                    "SELECT codigo, nombre, precio FROM productos WHERE nombre LIKE ? OR codigo LIKE ? ORDER BY CAST(codigo AS INTEGER)",
                    new String[]{likeQuery, likeQuery}
            );
        }

        while (c.moveToNext()) {
            listaProductos.add(new Producto(
                    c.getString(0),
                    c.getString(1),
                    c.getDouble(2)
            ));
        }
        c.close();

        if (adapter == null) {
            adapter = new ProductoAdapter(this, listaProductos);
            listViewProductos.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (vistaSeleccionada != null) {
            vistaSeleccionada.setBackgroundColor(Color.TRANSPARENT);
            vistaSeleccionada = null;
        }
        codigoSeleccionado = null;
        actualizarEstadoBotones(false);
    }

    private Producto buscarPorCodigo(String codigo) {
        for (Producto p : listaProductos) {
            if (p.getCodigo().equals(codigo)) return p;
        }
        return null;
    }

    // ----------------- MODELO -----------------
    public class Producto {
        private String codigo, nombre;
        private double precio;

        public Producto(String codigo, String nombre, double precio) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }
    }

    // ------------- ADAPTADOR LISTVIEW -------------
    public class ProductoAdapter extends ArrayAdapter<Producto> {

        public ProductoAdapter(Context context, List<Producto> productos) {
            super(context, 0, productos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Producto p = getItem(position);

            TextView txt1 = convertView.findViewById(android.R.id.text1);
            TextView txt2 = convertView.findViewById(android.R.id.text2);

            txt1.setText(p.getNombre());
            // --- AJUSTE: Mostrar Código y Precio (como entero) ---
            txt2.setText("Código: " + p.getCodigo() + "  |  Precio: ₡" + (int)p.getPrecio());

            if (vistaSeleccionada != null &&
                    codigoSeleccionado != null &&
                    codigoSeleccionado.equals(p.getCodigo())) {
                convertView.setBackgroundColor(Color.LTGRAY);
            } else {
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }

            return convertView;
        }
    }
}
