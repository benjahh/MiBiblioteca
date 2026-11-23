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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class RegistroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputLayout tilCorreo, tilClave, tilConfirmar;
    private EditText etCorreo, etClave, etConfirmar;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        tilCorreo = findViewById(R.id.tilCorreo);
        tilClave = findViewById(R.id.tilClave);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        etCorreo = findViewById(R.id.etCorreo);
        etClave = findViewById(R.id.etClave);
        etConfirmar = findViewById(R.id.etConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        tvIrLogin.setOnClickListener(v -> finish()); // vuelve al login
    }

    private void registrarUsuario() {
        String correo = etCorreo.getText().toString().trim();
        String clave = etClave.getText().toString();
        String confirmar = etConfirmar.getText().toString();

        // Limpia los errores anteriores
        tilCorreo.setError(null);
        tilClave.setError(null);
        tilConfirmar.setError(null);

        // Validaciones
        if (correo.isEmpty()) {
            tilCorreo.setError("Ingresa un correo");
            etCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            tilCorreo.setError("Correo no válido");
            etCorreo.requestFocus();
            return;
        }
        if (clave.length() < 6) {
            tilClave.setError("Mínimo 6 caracteres");
            etClave.requestFocus();
            return;
        }
        if (!clave.equals(confirmar)) {
            tilConfirmar.setError("Las contraseñas no coinciden");
            etConfirmar.requestFocus();
            return;
        }

        btnRegistrar.setEnabled(false);

        // Firebase crea la cuenta y guarda la contraseña de forma segura (no en Firestore)
        auth.createUserWithEmailAndPassword(correo, clave)
                .addOnCompleteListener(tarea -> {
                    btnRegistrar.setEnabled(true);
                    if (tarea.isSuccessful()) {
                        Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                        // Al registrarse, Firebase deja la sesión iniciada
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else if (tarea.getException() instanceof FirebaseAuthUserCollisionException) {
                        tilCorreo.setError("Ese correo ya está registrado");
                        etCorreo.requestFocus();
                    } else if (tarea.getException() instanceof FirebaseAuthWeakPasswordException) {
                        tilClave.setError("Contraseña muy débil");
                        etClave.requestFocus();
                    } else {
                        Toast.makeText(this, "No se pudo registrar. Revisa tu conexión", Toast.LENGTH_LONG).show();
                    }
                });
    }
}