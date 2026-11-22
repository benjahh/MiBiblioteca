package com.devst.mibiblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private LibroRepositorio repositorio;

    private EditText etTitulo, etAutor, etGenero, etAnio;
    private TextView tvContador;
    private ArrayAdapter<Libro> adaptador;
    private final List<Libro> listaLibros = new ArrayList<>();

    // Libro que el usuario tocó en la lista (null si no hay ninguno seleccionado)
    private Libro libroSeleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();
        FirebaseUser usuario = auth.getCurrentUser();

        // Sin sesión iniciada no se puede entrar a esta pantalla
        if (usuario == null) {
            irAlLogin();
            return;
        }

        // El UID del usuario decide qué libros puede ver y modificar
        repositorio = new LibroRepositorio(usuario.getUid());

        TextView tvUsuario = findViewById(R.id.tvUsuario);
        tvUsuario.setText("Hola, " + usuario.getEmail());

        etTitulo = findViewById(R.id.etTitulo);
        etAutor = findViewById(R.id.etAutor);
        etGenero = findViewById(R.id.etGenero);
        etAnio = findViewById(R.id.etAnio);
        tvContador = findViewById(R.id.tvContador);
        ListView lvLibros = findViewById(R.id.lvLibros);

        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaLibros);
        lvLibros.setAdapter(adaptador);
        lvLibros.setOnItemClickListener((parent, vista, posicion, id) ->
                seleccionarLibro(listaLibros.get(posicion)));

        findViewById(R.id.btnGuardar).setOnClickListener(v -> guardarLibro());
        findViewById(R.id.btnActualizar).setOnClickListener(v -> actualizarLibro());
        findViewById(R.id.btnEliminar).setOnClickListener(v -> confirmarEliminar());
        findViewById(R.id.btnLimpiar).setOnClickListener(v -> limpiarFormulario());
        findViewById(R.id.btnCerrarSesion).setOnClickListener(v -> cerrarSesion());

        escucharLibros();
    }

    // ---------------- READ ----------------
    // Escucha la colección en tiempo real: la lista se actualiza sola con cada cambio
    private void escucharLibros() {
        repositorio.obtenerTodos().addSnapshotListener(this, (resultado, error) -> {
            if (error != null) {
                mensaje("Error al cargar los libros: " + error.getMessage());
                return;
            }
            if (resultado == null) return;

            listaLibros.clear();
            for (DocumentSnapshot doc : resultado.getDocuments()) {
                Libro libro = doc.toObject(Libro.class);
                if (libro != null) {
                    libro.setId(doc.getId());
                    listaLibros.add(libro);
                }
            }
            adaptador.notifyDataSetChanged();
            tvContador.setText("Mis libros (" + listaLibros.size() + ")");
        });
    }

    // ---------------- CREATE ----------------
    private void guardarLibro() {
        if (libroSeleccionado != null) {
            mensaje("Estás editando un libro: usa Actualizar o Limpiar");
            return;
        }
        Libro libro = leerFormulario();
        if (libro == null) return;

        repositorio.agregar(libro)
                .addOnSuccessListener(referencia -> {
                    mensaje("Libro guardado");
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> mensaje("Error al guardar: " + e.getMessage()));
    }

    // ---------------- UPDATE ----------------
    private void actualizarLibro() {
        if (libroSeleccionado == null) {
            mensaje("Primero toca un libro de la lista");
            return;
        }
        Libro libro = leerFormulario();
        if (libro == null) return;
        libro.setId(libroSeleccionado.getId());

        repositorio.actualizar(libro)
                .addOnSuccessListener(sinUso -> {
                    mensaje("Libro actualizado");
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> mensaje("Error al actualizar: " + e.getMessage()));
    }

    // ---------------- DELETE ----------------
    private void confirmarEliminar() {
        if (libroSeleccionado == null) {
            mensaje("Primero toca un libro de la lista");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("Eliminar libro")
                .setMessage("¿Seguro que quieres eliminar \"" + libroSeleccionado.getTitulo() + "\"?")
                .setPositiveButton("Eliminar", (dialogo, boton) -> eliminarLibro())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarLibro() {
        repositorio.eliminar(libroSeleccionado.getId())
                .addOnSuccessListener(sinUso -> {
                    mensaje("Libro eliminado");
                    limpiarFormulario();
                })
                .addOnFailureListener(e -> mensaje("Error al eliminar: " + e.getMessage()));
    }

    // ---------------- Validaciones ----------------
    // Devuelve el libro con los datos del formulario, o null si algo está mal
    private Libro leerFormulario() {
        String titulo = etTitulo.getText().toString().trim();
        String autor = etAutor.getText().toString().trim();
        String genero = etGenero.getText().toString().trim();
        String anioTexto = etAnio.getText().toString().trim();

        if (titulo.isEmpty()) {
            etTitulo.setError("Campo obligatorio");
            etTitulo.requestFocus();
            return null;
        }
        if (autor.isEmpty()) {
            etAutor.setError("Campo obligatorio");
            etAutor.requestFocus();
            return null;
        }
        if (genero.isEmpty()) {
            etGenero.setError("Campo obligatorio");
            etGenero.requestFocus();
            return null;
        }
        if (anioTexto.isEmpty()) {
            etAnio.setError("Campo obligatorio");
            etAnio.requestFocus();
            return null;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioTexto);
        } catch (NumberFormatException e) {
            etAnio.setError("Año no válido");
            etAnio.requestFocus();
            return null;
        }
        int anioActual = Calendar.getInstance().get(Calendar.YEAR);
        if (anio < 1000 || anio > anioActual) {
            etAnio.setError("Ingresa un año entre 1000 y " + anioActual);
            etAnio.requestFocus();
            return null;
        }

        return new Libro(titulo, autor, genero, anio);
    }

    // ---------------- Utilidades ----------------
    private void seleccionarLibro(Libro libro) {
        libroSeleccionado = libro;
        etTitulo.setText(libro.getTitulo());
        etAutor.setText(libro.getAutor());
        etGenero.setText(libro.getGenero());
        etAnio.setText(String.valueOf(libro.getAnio()));
        mensaje("Editando: " + libro.getTitulo());
    }

    private void limpiarFormulario() {
        libroSeleccionado = null;
        etTitulo.setText("");
        etAutor.setText("");
        etGenero.setText("");
        etAnio.setText("");
        etTitulo.setError(null);
        etAutor.setError(null);
        etGenero.setError(null);
        etAnio.setError(null);
        etTitulo.requestFocus();
    }

    private void cerrarSesion() {
        auth.signOut();
        mensaje("Sesión cerrada");
        irAlLogin();
    }

    private void irAlLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mensaje(String texto) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show();
    }
}