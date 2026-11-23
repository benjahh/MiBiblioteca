package com.devst.mibiblioteca;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputLayout tilCorreo, tilClave;
    private EditText etCorreo, etClave;
    private Button btnIngresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Conexión con Firebase Authentication
        auth = FirebaseAuth.getInstance();

        // Si el usuario ya inició sesión antes, pasa directo a sus libros
        if (auth.getCurrentUser() != null) {
            irAMisLibros();
            return;
        }

        tilCorreo = findViewById(R.id.tilCorreo);
        tilClave = findViewById(R.id.tilClave);
        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        btnIngresar = findViewById(R.id.btnIngresar);
        TextView tvIrRegistro = findViewById(R.id.tvIrRegistro);

        btnIngresar.setOnClickListener(v -> iniciarSesion());
        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void iniciarSesion() {
        String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString();

        // Limpia los errores anteriores
        tilCorreo.setError(null);
        tilClave.setError(null);

        // Validaciones de campos obligatorios
        if (correo.isEmpty()) {
            tilCorreo.setError("Ingresa tu correo");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Correo no válido");
            etCorreo.requestFocus();
            return;
        }
        if (clave.isEmpty()) {
            tilClave.setError("Ingresa tu contraseña");
            etClave.requestFocus();
            return;
        }

        btnIngresar.setEnabled(false); // evita doble clic mientras responde Firebase

        auth.signInWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(tarea -> {
                    btnIngresar.setEnabled(true);
                    if (tarea.isSuccessful()) {
                        Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
                        irAMisLibros();
                    } else {
                        Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void irAMisLibros() {
        Intent intent = new Intent(this, MainActivity.class);
        // Borra el login del historial para que "atrás" no vuelva a él
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}