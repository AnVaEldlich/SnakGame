package com.example.myfirstapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referencia al botón
        Button btnAction = findViewById(R.id.btnAction);

        // Evento click
        btnAction.setOnClickListener(v -> mostrarDialogo());
    }

    private void mostrarDialogo() {
        // Opciones de dificultad
        String[] niveles = {"Fácil", "Medio", "Difícil"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona el nivel de dificultad")
                .setItems(niveles, (dialog, which) -> {
                    String nivelSeleccionado = niveles[which];
                    Toast.makeText(this, "Nivel elegido: " + nivelSeleccionado, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}
