package com.devst.mibiblioteca;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

// Repositorio: aquí están todas las operaciones con Cloud Firestore
public class LibroRepositorio {

    private static final String COLECCION_USUARIOS = "usuarios";
    private static final String COLECCION_LIBROS = "libros";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference libros;

    // Cada usuario tiene su propia colección: usuarios/{uid}/libros
    public LibroRepositorio(String uid) {
        libros = db.collection(COLECCION_USUARIOS)
                .document(uid)
                .collection(COLECCION_LIBROS);
    }

    // CREATE: add() crea un documento nuevo con un ID automático
    public Task<DocumentReference> agregar(Libro libro) {
        return libros.add(libro);
    }

    // READ: consulta todos los libros del usuario ordenados por título
    public Query obtenerTodos() {
        return libros.orderBy("titulo");
    }

    // UPDATE: set() reemplaza los datos del documento con ese ID
    public Task<Void> actualizar(Libro libro) {
        return libros.document(libro.getId()).set(libro);
    }

    // DELETE: borra el documento con ese ID
    public Task<Void> eliminar(String id) {
        return libros.document(id).delete();
    }
}