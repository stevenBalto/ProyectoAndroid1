package com.example.proyecto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class ClientesActivity extends AppCompatActivity {

    LinearLayout layoutLista;
    View layoutFormulario;
    EditText txtBuscar, txtCedula, txtNombre, txtTelefono;
    ListView listViewClientes;
    TextView btnAgregar, btnEditar, btnEliminar, btnSalir;
    Button btnGuardar, btnCancelar;

    ArrayList<Cliente> listaClientes = new ArrayList<>();
    ClienteAdapter adapter;

    DatabaseHelper db;
    String cedulaSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clientes);

        db = new DatabaseHelper(this);

        layoutLista = findViewById(R.id.layoutLista);
        layoutFormulario = findViewById(R.id.layoutFormulario);

        txtBuscar = findViewById(R.id.txtBuscar);
        txtCedula = findViewById(R.id.txtCedula);
        txtNombre = findViewById(R.id.txtNombre);
        txtTelefono = findViewById(R.id.txtTelefono);

        listViewClientes = findViewById(R.id.listViewClientes);

        btnAgregar = findViewById(R.id.btnAgregar);
        btnEditar = findViewById(R.id.btnEditar);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnSalir = findViewById(R.id.btnSalir);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnCancelar = findViewById(R.id.btnCancelar);

        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarClientes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listViewClientes.setOnItemClickListener((parent, view, position, id) -> {
            Cliente c = listaClientes.get(position);
            cedulaSeleccionada = c.getCedula();
            actualizarEstadoBotones(true);
        });

        cargarClientes("");

        btnAgregar.setOnClickListener(v -> mostrarFormulario(null));
        btnEditar.setOnClickListener(v -> {
            if (cedulaSeleccionada != null) {
                Cliente c = buscarPorCedula(cedulaSeleccionada);
                mostrarFormulario(c);
            }
        });
        btnEliminar.setOnClickListener(v -> {
            if (cedulaSeleccionada != null) {
                SQLiteDatabase writable = db.getWritableDatabase();
                writable.delete("cliente", "cedula=?", new String[]{cedulaSeleccionada});
                txtBuscar.setText("");
                cargarClientes("");
            }
        });

        btnSalir.setOnClickListener(v -> {
            if (layoutFormulario.getVisibility() == View.VISIBLE) {
                mostrarLista();
            } else {
                finish();
            }
        });

        btnGuardar.setOnClickListener(v -> guardarCliente());
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

        if (habilitados) {
            btnEditar.setAlpha(1.0f);
            btnEliminar.setAlpha(1.0f);
        } else {
            btnEditar.setAlpha(0.5f);
            btnEliminar.setAlpha(0.5f);
        }
    }

    private void mostrarFormulario(Cliente c) {
        layoutLista.setVisibility(View.GONE);
        layoutFormulario.setVisibility(View.VISIBLE);

        if (c == null) {
            txtCedula.setText("");
            txtNombre.setText("");
            txtTelefono.setText("");
            txtCedula.setEnabled(true);
        } else {
            txtCedula.setText(c.getCedula());
            txtNombre.setText(c.getNombre());
            txtTelefono.setText(c.getTelefono());
            txtCedula.setEnabled(false);
        }
    }

    private void mostrarLista() {
        layoutFormulario.setVisibility(View.GONE);
        layoutLista.setVisibility(View.VISIBLE);
    }

    private void guardarCliente() {
        String ced = txtCedula.getText().toString().trim();
        String nom = txtNombre.getText().toString().trim();
        String tel = txtTelefono.getText().toString().trim();

        if (ced.isEmpty() || nom.isEmpty()) return;

        SQLiteDatabase writable = db.getWritableDatabase();
        boolean esUpdate = !txtCedula.isEnabled();

        if (esUpdate) {
            writable.execSQL("UPDATE cliente SET nombre=?, telefono=? WHERE cedula=?", new Object[]{nom, tel, ced});
        } else {
            writable.execSQL("INSERT INTO cliente(cedula, nombre, telefono) VALUES(?,?,?)", new Object[]{ced, nom, tel});
        }

        txtBuscar.setText("");
        cargarClientes("");
        mostrarLista();
    }

    private void cargarClientes(String query) {
        listaClientes.clear();

        SQLiteDatabase readable = db.getReadableDatabase();
        Cursor c;

        if (query.trim().isEmpty()) {
            c = readable.rawQuery("SELECT cedula, nombre, telefono FROM cliente ORDER BY nombre", null);
        } else {
            String likeQuery = "%" + query + "%";
            c = readable.rawQuery("SELECT cedula, nombre, telefono FROM cliente WHERE nombre LIKE ? OR cedula LIKE ? ORDER BY nombre", new String[]{likeQuery, likeQuery});
        }

        while (c.moveToNext()) {
            Cliente cli = new Cliente(c.getString(0), c.getString(1), c.getString(2));
            listaClientes.add(cli);
        }
        c.close();

        adapter = new ClienteAdapter(this, listaClientes);
        listViewClientes.setAdapter(adapter);

        cedulaSeleccionada = null;
        actualizarEstadoBotones(false);
    }

    private Cliente buscarPorCedula(String cedula) {
        for (Cliente c : listaClientes) {
            if (c.getCedula().equals(cedula)) return c;
        }
        return null;
    }

    public class ClienteAdapter extends ArrayAdapter<Cliente> {
        public ClienteAdapter(Context context, ArrayList<Cliente> clientes) {
            super(context, 0, clientes);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Cliente cliente = getItem(position);

            TextView text1 = convertView.findViewById(android.R.id.text1);
            TextView text2 = convertView.findViewById(android.R.id.text2);

            text1.setText(cliente.getNombre());
            text2.setText("Cédula: " + cliente.getCedula() + " | Tel: " + cliente.getTelefono());

            return convertView;
        }
    }

    public class Cliente {
        private String cedula, nombre, telefono;

        public Cliente(String cedula, String nombre, String telefono) {
            this.cedula = cedula;
            this.nombre = nombre;
            this.telefono = telefono;
        }

        public String getCedula() { return cedula; }
        public String getNombre() { return nombre; }
        public String getTelefono() { return telefono; }
    }
}
