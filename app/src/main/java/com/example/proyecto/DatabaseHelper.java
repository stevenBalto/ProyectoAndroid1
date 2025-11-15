package com.example.proyecto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

    public class DatabaseHelper extends SQLiteOpenHelper {

        public static final String DB_NAME = "verduleria.db";
        public static final int DB_VERSION = 3; // <-- VERSIÓN FORZADA A 3

        public DatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            // ========== TABLA CATEGORIAS ==========
            db.execSQL("CREATE TABLE categorias (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nombre TEXT NOT NULL)");

            // ========== TABLA CLIENTE ==========
            db.execSQL("CREATE TABLE cliente (" +
                    "cedula TEXT PRIMARY KEY," +
                    "nombre TEXT NOT NULL," +
                    "telefono TEXT)");

            // ========== TABLA PRODUCTOS ==========
            db.execSQL("CREATE TABLE productos (" +
                    "codigo TEXT PRIMARY KEY," +
                    "nombre TEXT NOT NULL," +
                    "precio REAL NOT NULL)");


            // ========== TABLA ENCABEZADO FACTURA ==========
            db.execSQL("CREATE TABLE encabezado_factura (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "cedula_cliente TEXT," +
                    "fecha TEXT NOT NULL," +
                    "total REAL NOT NULL DEFAULT 0.00," +
                    "FOREIGN KEY(cedula_cliente) REFERENCES cliente(cedula))");

            // ========== TABLA DETALLE FACTURA ==========
            db.execSQL("CREATE TABLE detalle_factura (" +
                    "id_detalle INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "id_factura INTEGER," +
                    "codigo_producto TEXT," +
                    "precio_producto REAL NOT NULL," +
                    "cantidad_producto INTEGER NOT NULL," +
                    "subtotal REAL NOT NULL," +
                    "FOREIGN KEY(id_factura) REFERENCES encabezado_factura(id)," +
                    "FOREIGN KEY(codigo_producto) REFERENCES productos(codigo))");

            // ========== TABLA USUARIOS ==========
            db.execSQL("CREATE TABLE usuarios (" +
                    "cedula TEXT PRIMARY KEY," +
                    "nombre TEXT NOT NULL," +
                    "llave TEXT NOT NULL)");

            // ========== INSERTS ==========
            db.execSQL("INSERT INTO categorias (id, nombre) VALUES (10, 'Frutas'), (11, 'Helados'), (12, 'Verduras'), (13, 'Picantes')");
            db.execSQL("INSERT INTO cliente (cedula, nombre, telefono) VALUES " +
                    "('123', 'Pablo Osorno', '12345123')," +
                    "('12345678', 'Juan Pérez', '555-1234')," +
                    "('504570475', 'Luis', '85198095')," +
                    "('87654321', 'María González', '555-5678')");
            // --- INSERT CORREGIDO --- 
            db.execSQL("INSERT INTO productos (codigo, nombre, precio) VALUES ('1', 'Fresa', 1000)");
            db.execSQL("INSERT INTO usuarios (cedula, nombre, llave) VALUES ('1', 'admin', '123456')");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS detalle_factura");
            db.execSQL("DROP TABLE IF EXISTS encabezado_factura");
            db.execSQL("DROP TABLE IF EXISTS productos");
            db.execSQL("DROP TABLE IF EXISTS cliente");
            db.execSQL("DROP TABLE IF EXISTS categorias");
            db.execSQL("DROP TABLE IF EXISTS usuarios");

            onCreate(db);
        }
    }


