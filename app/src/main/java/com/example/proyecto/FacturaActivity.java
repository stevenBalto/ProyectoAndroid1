package com.example.proyecto;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class FacturaActivity extends AppCompatActivity {

    // Vistas
    LinearLayout layoutLista, bottomNavLista;
    ScrollView layoutFormulario;
    LinearLayout bottomNavForm;
    ListView listViewFacturas;
    TextView lblTituloFormulario;

    // Botones
    TextView btnAgregar, btnEditar, btnEliminar, btnSalir;
    Button btnGuardarFactura, btnCancelar;
    Button btnAgregarProductoAlDetalle;

    // Componentes del Formulario
    EditText txtNumeroFactura, txtCantidadForm;
    TextView txtFecha;
    Spinner spinnerClientes, spinnerProductosForm;
    ListView listViewDetalleFormulario;

    // Datos
    ArrayList<Factura> listaFacturas = new ArrayList<>();
    FacturaAdapter facturaAdapter;
    ArrayList<Detalle> listaDetalleTemporal = new ArrayList<>();
    DetalleFormularioAdapter detalleFormularioAdapter;
    int facturaSeleccionadaIndex = -1;

    // Base de Datos
    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factura);

        dbHelper = new DatabaseHelper(this);
        db = dbHelper.getWritableDatabase();

        // --- Enlaces a Vistas ---
        layoutLista = findViewById(R.id.layoutLista);
        layoutFormulario = findViewById(R.id.layoutFormulario);
        listViewFacturas = findViewById(R.id.listViewFacturas);
        lblTituloFormulario = findViewById(R.id.lblTituloFormulario);
        bottomNavLista = findViewById(R.id.bottom_navigation_factura);
        bottomNavForm = findViewById(R.id.bottom_navigation_factura_form);

        btnAgregar = findViewById(R.id.btnAgregar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir = findViewById(R.id.btnSalir);

        txtNumeroFactura = findViewById(R.id.txtNumeroFactura);
        txtFecha = findViewById(R.id.txtFecha);
        spinnerClientes = findViewById(R.id.spinnerClientes);
        spinnerProductosForm = findViewById(R.id.spinnerProductosForm);
        txtCantidadForm = findViewById(R.id.txtCantidadForm);
        btnAgregarProductoAlDetalle = findViewById(R.id.btnAgregarProductoAlDetalle);
        listViewDetalleFormulario = findViewById(R.id.listViewDetalleFormulario);
        
        btnGuardarFactura = findViewById(R.id.btnGuardarFactura);
        btnCancelar = findViewById(R.id.btnCancelar);

        // --- Configuración ---
        facturaAdapter = new FacturaAdapter();
        listViewFacturas.setAdapter(facturaAdapter);
        detalleFormularioAdapter = new DetalleFormularioAdapter();
        listViewDetalleFormulario.setAdapter(detalleFormularioAdapter);

        setupListeners();
        mostrarLista();
    }

    private void setupListeners() {
        btnAgregar.setOnClickListener(v -> mostrarFormulario(null));
        btnEditar.setOnClickListener(v -> {
            if (facturaSeleccionadaIndex != -1) mostrarFormulario(listaFacturas.get(facturaSeleccionadaIndex));
        });
        btnEliminar.setOnClickListener(v -> {
            if (facturaSeleccionadaIndex != -1) confirmarEliminacion(listaFacturas.get(facturaSeleccionadaIndex).id);
        });
        btnSalir.setOnClickListener(v -> finish());
        listViewFacturas.setOnItemClickListener((p, v, pos, id) -> {
            facturaSeleccionadaIndex = pos;
            actualizarBotonesPrincipales(true);
            facturaAdapter.notifyDataSetChanged();
        });

        // Formulario
        txtFecha.setOnClickListener(v -> seleccionarFecha());
        btnAgregarProductoAlDetalle.setOnClickListener(v -> agregarProductoAlDetalle());
        btnGuardarFactura.setOnClickListener(v -> guardarFactura());
        btnCancelar.setOnClickListener(v -> mostrarLista());
        
        // Listener para EDITAR/QUITAR productos del detalle en el modo edición
        listViewDetalleFormulario.setOnItemLongClickListener((parent, view, position, id) -> {
            final CharSequence[] options = {"Editar Cantidad", "Quitar Producto"};
            new AlertDialog.Builder(FacturaActivity.this)
                .setTitle("Acciones para " + listaDetalleTemporal.get(position).nombre)
                .setItems(options, (dialog, item) -> {
                    if (options[item].equals("Editar Cantidad")) {
                        dialogoEditarCantidad(position);
                    } else if (options[item].equals("Quitar Producto")) {
                        listaDetalleTemporal.remove(position);
                        detalleFormularioAdapter.notifyDataSetChanged();
                    }
                })
                .show();
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (layoutFormulario.getVisibility() == View.VISIBLE) {
            mostrarLista();
        } else {
            super.onBackPressed();
        }
    }

    // ================== VISTAS ==================
    private void mostrarLista() {
        layoutFormulario.setVisibility(View.GONE);
        bottomNavForm.setVisibility(View.GONE);
        layoutLista.setVisibility(View.VISIBLE);
        bottomNavLista.setVisibility(View.VISIBLE);
        facturaSeleccionadaIndex = -1;
        cargarFacturasDeDB();
        actualizarBotonesPrincipales(false);
    }

    private void mostrarFormulario(Factura factura) {
        layoutLista.setVisibility(View.GONE);
        bottomNavLista.setVisibility(View.GONE);
        layoutFormulario.setVisibility(View.VISIBLE);
        bottomNavForm.setVisibility(View.VISIBLE);
        listaDetalleTemporal.clear();
        
        cargarClientesSpinner();
        cargarProductosSpinner();

        if (factura == null) { // Nueva
            lblTituloFormulario.setText("Nueva Factura");
            txtFecha.setText("");
            if(spinnerClientes.getAdapter() != null && spinnerClientes.getCount() > 0) spinnerClientes.setSelection(0);
            cargarSiguienteNumeroFactura();
        } else { // Editar
            lblTituloFormulario.setText("Editar Factura #" + factura.id);
            txtNumeroFactura.setText(String.valueOf(factura.id));
            txtFecha.setText(factura.fecha);
            for (int i = 0; i < spinnerClientes.getAdapter().getCount(); i++) {
                if (spinnerClientes.getItemAtPosition(i).toString().contains(factura.cliente)) {
                    spinnerClientes.setSelection(i);
                    break;
                }
            }
            cargarDetallesDeDB(factura.id);
        }
        detalleFormularioAdapter.notifyDataSetChanged();
    }

    // ================== LÓGICA ==================
    private void guardarFactura() {
        if (spinnerClientes.getSelectedItemPosition() == 0 || txtFecha.getText().toString().isEmpty() || listaDetalleTemporal.isEmpty()) {
            Toast.makeText(this, "Complete los datos y agregue productos.", Toast.LENGTH_SHORT).show();
            return;
        }

        db.beginTransaction();
        try {
            int total = 0;
            for (Detalle d : listaDetalleTemporal) total += d.subtotal;

            ContentValues header = new ContentValues();
            header.put("cedula_cliente", spinnerClientes.getSelectedItem().toString().split(" - ")[0]);
            header.put("fecha", txtFecha.getText().toString());
            header.put("total", total);
            
            int idFactura;
            if (facturaSeleccionadaIndex == -1) { // Guardar nueva
                idFactura = (int) db.insertOrThrow("encabezado_factura", null, header);
            } else { // Actualizar
                idFactura = listaFacturas.get(facturaSeleccionadaIndex).id;
                db.update("encabezado_factura", header, "id = ?", new String[]{String.valueOf(idFactura)});
                db.delete("detalle_factura", "id_factura = ?", new String[]{String.valueOf(idFactura)});
            }

            for (Detalle d : listaDetalleTemporal) {
                ContentValues detail = new ContentValues();
                detail.put("id_factura", idFactura);
                detail.put("codigo_producto", d.codigo);
                detail.put("precio_producto", d.precio);
                detail.put("cantidad_producto", d.cantidad);
                detail.put("subtotal", d.subtotal);
                db.insert("detalle_factura", null, detail);
            }
            
            db.setTransactionSuccessful();
            Toast.makeText(this, "Factura guardada.", Toast.LENGTH_SHORT).show();
            mostrarLista();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar.", Toast.LENGTH_LONG).show();
        } finally {
            db.endTransaction();
        }
    }
    
    private void agregarProductoAlDetalle() {
        if (spinnerProductosForm.getSelectedItemPosition() > 0 && !txtCantidadForm.getText().toString().isEmpty()) {
            String selected = spinnerProductosForm.getSelectedItem().toString();
            String codigo = selected.split(" - ")[0];
            String nombre = selected.split(" - ")[1].split(" \\(")[0];
            int precio = Integer.parseInt(selected.split("\\(₡")[1].replace(")", ""));
            int cantidad = Integer.parseInt(txtCantidadForm.getText().toString());

            if(cantidad > 0){
                boolean existe = false;
                for(Detalle d : listaDetalleTemporal) {
                    if(d.codigo.equals(codigo)) {
                        d.cantidad += cantidad;
                        d.subtotal = d.cantidad * d.precio;
                        existe = true;
                        break;
                    }
                }
                if(!existe) {
                    listaDetalleTemporal.add(new Detalle(codigo, nombre, cantidad, precio, cantidad * precio));
                }
                detalleFormularioAdapter.notifyDataSetChanged();
                txtCantidadForm.setText("");
                spinnerProductosForm.setSelection(0);
            } else {
                Toast.makeText(this, "La cantidad debe ser mayor a 0", Toast.LENGTH_SHORT).show();
            }
        } else {
             Toast.makeText(this, "Seleccione un producto y una cantidad", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void confirmarEliminacion(int idFactura) {
         new AlertDialog.Builder(this)
            .setTitle("Eliminar Factura")
            .setMessage("¿Seguro que quieres eliminar la factura #" + idFactura + "?")
            .setPositiveButton("Sí", (dialog, which) -> {
                db.delete("detalle_factura", "id_factura = ?", new String[]{String.valueOf(idFactura)});
                db.delete("encabezado_factura", "id = ?", new String[]{String.valueOf(idFactura)});
                Toast.makeText(this, "Factura eliminada.", Toast.LENGTH_SHORT).show();
                mostrarLista();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void dialogoEditarCantidad(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Cantidad");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        final Detalle d = listaDetalleTemporal.get(position);
        input.setText(String.valueOf(d.cantidad));
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                int nuevaCantidad = Integer.parseInt(input.getText().toString());
                if (nuevaCantidad > 0) {
                    d.cantidad = nuevaCantidad;
                    d.subtotal = d.cantidad * d.precio;
                    detalleFormularioAdapter.notifyDataSetChanged();
                } else {
                    listaDetalleTemporal.remove(position);
                    detalleFormularioAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Producto eliminado (cantidad 0)", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Cantidad no válida", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // ================== HELPERS ==================
    private void cargarFacturasDeDB() {
        listaFacturas.clear();
        Cursor c = db.rawQuery("SELECT e.id, e.fecha, e.total, c.nombre FROM encabezado_factura e " +
                "JOIN cliente c ON e.cedula_cliente = c.cedula ORDER BY e.id DESC", null);
        while(c.moveToNext()) listaFacturas.add(new Factura(c.getInt(0), c.getString(1), c.getString(3), c.getInt(2)));
        c.close();
        facturaAdapter.notifyDataSetChanged();
    }
    
    private void cargarDetallesDeDB(int idFactura) {
        listaDetalleTemporal.clear();
        Cursor c = db.rawQuery("SELECT p.nombre, d.cantidad_producto, d.precio_producto, d.subtotal, d.codigo_producto " +
                               "FROM detalle_factura d JOIN productos p ON d.codigo_producto = p.codigo " +
                               "WHERE d.id_factura = ?", new String[]{String.valueOf(idFactura)});
        while(c.moveToNext()) listaDetalleTemporal.add(new Detalle(c.getString(4), c.getString(0), c.getInt(1), c.getInt(2), c.getInt(3)));
        c.close();
        detalleFormularioAdapter.notifyDataSetChanged();
    }
    
    private void cargarClientesSpinner() {
        ArrayList<String> clientes = new ArrayList<>();
        clientes.add("Seleccione un cliente");
        Cursor c = db.rawQuery("SELECT cedula, nombre FROM cliente ORDER BY nombre", null);
        while(c.moveToNext()) clientes.add(c.getString(0) + " - " + c.getString(1));
        c.close();
        spinnerClientes.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientes));
    }

    private void cargarProductosSpinner() {
        ArrayList<String> productos = new ArrayList<>();
        productos.add("Seleccione un producto");
        Cursor c = db.rawQuery("SELECT codigo, nombre, precio FROM productos ORDER BY nombre", null);
        while(c.moveToNext()) productos.add(c.getString(0) + " - " + c.getString(1) + " (₡" + c.getInt(2) + ")");
        c.close();
        spinnerProductosForm.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, productos));
    }
    
    private void cargarSiguienteNumeroFactura() {
        Cursor c = db.rawQuery("SELECT IFNULL(MAX(id), 0) + 1 FROM encabezado_factura", null);
        if (c.moveToNext()) txtNumeroFactura.setText(String.valueOf(c.getInt(0)));
        c.close();
    }

    private void seleccionarFecha() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> txtFecha.setText(String.format(Locale.getDefault(), "%d-%02d-%02d", y, m + 1, d)),
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void actualizarBotonesPrincipales(boolean seleccionado) {
        btnEditar.setEnabled(seleccionado);
        btnEliminar.setEnabled(seleccionado);
        btnEditar.setAlpha(seleccionado ? 1f : 0.5f);
        btnEliminar.setAlpha(seleccionado ? 1f : 0.5f);
    }
    
    // ================== CLASES INTERNAS ==================
    public static class Factura {
        int id, total; String fecha, cliente;
        public Factura(int id, String f, String c, int t) { this.id=id; this.fecha=f; this.cliente=c; this.total=t; }
    }

    private class FacturaAdapter extends BaseAdapter {
        public int getCount() { return listaFacturas.size(); }
        public Object getItem(int pos) { return listaFacturas.get(pos); }
        public long getItemId(int pos) { return listaFacturas.get(pos).id; }
        public View getView(int pos, View cv, ViewGroup p) {
            if (cv == null) cv = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, p, false);
            TextView t1 = cv.findViewById(android.R.id.text1), t2 = cv.findViewById(android.R.id.text2);
            Factura f = listaFacturas.get(pos);
            t1.setText("Factura #" + f.id + " - " + f.cliente);
            t2.setText(String.format(Locale.getDefault(), "Fecha: %s | Total: ₡%d", f.fecha, f.total));
            cv.setBackgroundColor(facturaSeleccionadaIndex == pos ? Color.LTGRAY : Color.TRANSPARENT);
            return cv;
        }
    }

    public static class Detalle {
        String codigo, nombre; int cantidad, precio, subtotal;
        public Detalle(String c, String n, int cant, int p, int s) {this.codigo=c; this.nombre=n; this.cantidad=cant; this.precio=p; this.subtotal=s;}
    }

    private class DetalleFormularioAdapter extends BaseAdapter {
        public int getCount() { return listaDetalleTemporal.size(); }
        public Object getItem(int pos) { return listaDetalleTemporal.get(pos); }
        public long getItemId(int pos) { return pos; }
        public View getView(int pos, View cv, ViewGroup p) {
            if (cv == null) cv = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, p, false);
            TextView t1 = cv.findViewById(android.R.id.text1);
            Detalle d = listaDetalleTemporal.get(pos);
            t1.setText(String.format(Locale.getDefault(), "%s - %d x ₡%d = ₡%d", d.nombre, d.cantidad, d.precio, d.subtotal));
            return cv;
        }
    }
}
