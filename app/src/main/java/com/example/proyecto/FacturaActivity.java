package com.example.proyecto;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

public class FacturaActivity extends AppCompatActivity {

    // ========= LAYOUTS =========
    LinearLayout layoutLista;
    ScrollView layoutFormulario;

    // ========= LISTA =========
    ListView listViewDetalle;
    ArrayList<Detalle> listaDetalle = new ArrayList<>();
    DetalleAdapter detalleAdapter;
    int posicionSeleccionada = -1;

    // ========= FORMULARIO =========
    EditText txtCantidad, txtNumeroFactura;
    TextView txtFecha, txtNombreProducto, txtPrecioProducto;
    Spinner spinnerClientes, spinnerProductos;
    Button btnGuardar, btnCancelar;

    // ========= BOTONES LISTA =========
    TextView btnEditar, btnEliminar, btnSalir;

    // ========= BD =========
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    // ========= SPINNERS =========
    ArrayList<String> listaClientes = new ArrayList<>();
    ArrayList<String> listaProductos = new ArrayList<>();
    ArrayAdapter<String> adapterClientes, adapterProductos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getReadableDatabase();

        // ========= ENLACES =========
        layoutLista = findViewById(R.id.layoutLista);
        layoutFormulario = findViewById(R.id.layoutFormulario);

        listViewDetalle = findViewById(R.id.listViewDetalle);

        txtFecha = findViewById(R.id.txtFecha);
        txtNumeroFactura = findViewById(R.id.txtNumeroFactura);
        txtNombreProducto = findViewById(R.id.txtNombreProducto);
        txtPrecioProducto = findViewById(R.id.txtPrecioProducto);
        txtCantidad = findViewById(R.id.txtCantidad);

        spinnerClientes = findViewById(R.id.spinnerClientes);
        spinnerProductos = findViewById(R.id.spinnerProductos);

        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir = findViewById(R.id.btnSalir);

        // ========= CARGAR DATOS =========
        cargarNumeroFactura();
        cargarClientes();
        cargarProductos();

        // ========= DATE PICKER =========
        txtFecha.setOnClickListener(v -> seleccionarFecha());

        // ========= ADAPTADOR LISTA =========
        detalleAdapter = new DetalleAdapter();
        listViewDetalle.setAdapter(detalleAdapter);

        listViewDetalle.setOnItemClickListener((parent, view, position, id) -> {
            posicionSeleccionada = position;
            actualizarBotones(true);
        });

        actualizarBotones(false);

        // ========= BOTÓN EDITAR =========
        btnEditar.setOnClickListener(v -> {
            if (posicionSeleccionada == -1) return;

            Detalle d = listaDetalle.get(posicionSeleccionada);
            mostrarFormulario(d);
        });

        // ========= BOTÓN ELIMINAR =========
        btnEliminar.setOnClickListener(v -> {
            if (posicionSeleccionada == -1) return;

            listaDetalle.remove(posicionSeleccionada);
            detalleAdapter.notifyDataSetChanged();
            posicionSeleccionada = -1;
            actualizarBotones(false);
        });

        // ========= BOTÓN SALIR =========
        btnSalir.setOnClickListener(v -> finish());

        // ========= GUARDAR DETALLE EDITADO =========
        btnGuardar.setOnClickListener(v -> guardarDetalle());

        // ========= CANCELAR =========
        btnCancelar.setOnClickListener(v -> mostrarLista());
    }

    // ======================================================
    //              FECHA – DATE PICKER
    // ======================================================
    private void seleccionarFecha() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int mes = c.get(Calendar.MONTH);
        int dia = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dp = new DatePickerDialog(
                this,
                (view, y, m, d) -> txtFecha.setText(y + "-" + (m + 1) + "-" + d),
                year, mes, dia
        );
        dp.show();
    }

    // ======================================================
    //                 CARGAR NÚMERO FACTURA
    // ======================================================
    private void cargarNumeroFactura() {
        Cursor c = db.rawQuery("SELECT MAX(id) FROM encabezado_factura", null);
        if (c.moveToFirst()) {
            txtNumeroFactura.setText(String.valueOf(c.getInt(0) + 1));
        }
        c.close();
    }

    // ======================================================
    //                 CARGAR CLIENTES
    // ======================================================
    private void cargarClientes() {
        listaClientes.clear();
        listaClientes.add("Seleccione un cliente");

        Cursor c = db.rawQuery("SELECT cedula, nombre FROM cliente", null);
        while (c.moveToNext()) {
            listaClientes.add(c.getString(0) + " - " + c.getString(1));
        }
        c.close();

        adapterClientes = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaClientes);
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClientes.setAdapter(adapterClientes);
    }

    // ======================================================
    //                 CARGAR PRODUCTOS
    // ======================================================
    private void cargarProductos() {
        listaProductos.clear();
        listaProductos.add("Seleccione un producto");

        Cursor c = db.rawQuery("SELECT codigo, nombre FROM productos", null);
        while (c.moveToNext()) {
            listaProductos.add(c.getString(0) + " - " + c.getString(1));
        }
        c.close();

        adapterProductos = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaProductos);
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProductos.setAdapter(adapterProductos);

        spinnerProductos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {
                    txtNombreProducto.setText("");
                    txtPrecioProducto.setText("");
                    return;
                }

                String codigo = listaProductos.get(pos).split(" - ")[0];
                Cursor c = db.rawQuery("SELECT nombre, precio FROM productos WHERE codigo=?", new String[]{codigo});
                if (c.moveToFirst()) {
                    txtNombreProducto.setText(c.getString(0));
                    txtPrecioProducto.setText(String.valueOf(c.getDouble(1)));
                }
                c.close();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // ======================================================
    //         MOSTRAR FORMULARIO (AGREGAR/EDITAR)
    // ======================================================
    private void mostrarFormulario(Detalle d) {

        layoutLista.setVisibility(View.GONE);
        layoutFormulario.setVisibility(View.VISIBLE);

        if (d == null) {
            txtCantidad.setText("");
            spinnerProductos.setSelection(0);
        } else {
            txtCantidad.setText(String.valueOf(d.cantidad));

            // Seleccionar producto en spinner
            for (int i = 0; i < listaProductos.size(); i++) {
                if (listaProductos.get(i).startsWith(d.codigo)) {
                    spinnerProductos.setSelection(i);
                    break;
                }
            }
        }
    }

    private void mostrarLista() {
        layoutFormulario.setVisibility(View.GONE);
        layoutLista.setVisibility(View.VISIBLE);
    }

    // ======================================================
    //                GUARDAR DETALLE
    // ======================================================
    private void guardarDetalle() {

        if (spinnerProductos.getSelectedItemPosition() == 0) return;
        if (txtCantidad.getText().toString().isEmpty()) return;

        String codigo = listaProductos.get(spinnerProductos.getSelectedItemPosition()).split(" - ")[0];
        String nombre = txtNombreProducto.getText().toString();
        double precio = Double.parseDouble(txtPrecioProducto.getText().toString());
        int cantidad = Integer.parseInt(txtCantidad.getText().toString());
        double subtotal = precio * cantidad;

        if (posicionSeleccionada == -1) {
            listaDetalle.add(new Detalle(codigo, nombre, cantidad, precio, subtotal));
        } else {
            Detalle d = listaDetalle.get(posicionSeleccionada);
            d.codigo = codigo;
            d.nombre = nombre;
            d.precio = precio;
            d.cantidad = cantidad;
            d.subtotal = subtotal;
        }

        detalleAdapter.notifyDataSetChanged();
        posicionSeleccionada = -1;
        actualizarBotones(false);
        mostrarLista();
    }

    // ======================================================
    //                  BOTONES EDITAR/ELIMINAR
    // ======================================================
    private void actualizarBotones(boolean habilitado) {
        btnEditar.setEnabled(habilitado);
        btnEliminar.setEnabled(habilitado);

        float alpha = habilitado ? 1f : 0.4f;
        btnEditar.setAlpha(alpha);
        btnEliminar.setAlpha(alpha);
    }

    // ======================================================
    //                   CLASE DETALLE
    // ======================================================
    public static class Detalle {
        String codigo, nombre;
        int cantidad;
        double precio, subtotal;

        public Detalle(String codigo, String nombre, int cantidad, double precio, double subtotal) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.precio = precio;
            this.subtotal = subtotal;
        }
    }

    // ======================================================
    //               ADAPTADOR DETALLE
    // ======================================================
    private class DetalleAdapter extends android.widget.BaseAdapter {

        @Override
        public int getCount() {
            return listaDetalle.size();
        }

        @Override
        public Object getItem(int position) {
            return listaDetalle.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            TextView line1 = convertView.findViewById(android.R.id.text1);
            TextView line2 = convertView.findViewById(android.R.id.text2);

            Detalle d = listaDetalle.get(pos);

            line1.setText(d.codigo + " - " + d.nombre);
            line2.setText("Cant: " + d.cantidad +
                    " | Precio: ₡" + d.precio +
                    " | Subtotal: ₡" + d.subtotal);

            return convertView;
        }
    }
}
