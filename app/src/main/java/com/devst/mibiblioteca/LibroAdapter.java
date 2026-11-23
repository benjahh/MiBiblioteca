package com.devst.mibiblioteca;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

// Adaptador: dibuja cada libro de la lista como una tarjeta
public class LibroAdapter extends ArrayAdapter<Libro> {

    public LibroAdapter(Context context, List<Libro> libros) {
        super(context, 0, libros);
    }

    @NonNull
    @Override
    public View getView(int posicion, View vista, @NonNull ViewGroup padre) {
        // Reutiliza la vista si ya existe (más eficiente al hacer scroll)
        if (vista == null) {
            vista = LayoutInflater.from(getContext()).inflate(R.layout.item_libro, padre, false);
        }

        Libro libro = getItem(posicion);
        if (libro == null) {
            return vista;
        }

        TextView tvInicial = vista.findViewById(R.id.tvInicial);
        TextView tvTitulo = vista.findViewById(R.id.tvTitulo);
        TextView tvAutor = vista.findViewById(R.id.tvAutor);
        TextView tvGenero = vista.findViewById(R.id.tvGenero);
        TextView tvAnio = vista.findViewById(R.id.tvAnio);

        String titulo = libro.getTitulo();
        tvInicial.setText(titulo == null || titulo.isEmpty() ? "?" : titulo.substring(0, 1).toUpperCase());
        tvTitulo.setText(titulo);
        tvAutor.setText(libro.getAutor());
        tvGenero.setText(libro.getGenero());
        tvAnio.setText(String.valueOf(libro.getAnio()));

        return vista;
    }
}