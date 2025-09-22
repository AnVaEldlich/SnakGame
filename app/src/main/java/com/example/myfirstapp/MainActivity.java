package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeMessage;
    private TextView gameTitle;
    private TextView motivationMessage;
    private Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        welcomeMessage = findViewById(R.id.welcome_message);
        gameTitle = findViewById(R.id.game_title);
        motivationMessage = findViewById(R.id.motivation_message);
        startGameButton = findViewById(R.id.start_game_button);

        // Configurar botón de inicio
        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animación de salida del botón
                startGameButton.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(150)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MainActivity.this, SnakeGameActivity.class);
                                startActivity(intent);
                            }
                        });
            }
        });

        // Iniciar animaciones de bienvenida
        startWelcomeAnimations();

        // Cambiar mensaje de motivación cada 3 segundos
        startMotivationMessages();
    }

    private void startWelcomeAnimations() {
        // Hacer invisibles los elementos al inicio
        welcomeMessage.setAlpha(0f);
        gameTitle.setAlpha(0f);
        startGameButton.setAlpha(0f);
        motivationMessage.setAlpha(0f);

        welcomeMessage.setTranslationY(-100f);
        gameTitle.setTranslationY(-50f);
        startGameButton.setTranslationY(100f);
        motivationMessage.setTranslationY(50f);

        // Animar mensaje de bienvenida
        welcomeMessage.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Animar título del juego con delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                gameTitle.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(800)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }, 300);

        // Animar botón de inicio con delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startGameButton.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(800)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();

                // Agregar pulsación continua al botón
                startButtonPulse();
            }
        }, 600);

        // Animar mensaje de motivación con delay
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                motivationMessage.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(800)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
            }
        }, 900);
    }

    private void startButtonPulse() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(startGameButton, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(startGameButton, "scaleY", 1f, 1.05f, 1f);

        scaleX.setDuration(2000);
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setInterpolator(new DecelerateInterpolator());

        scaleY.setDuration(2000);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setInterpolator(new DecelerateInterpolator());

        scaleX.start();
        scaleY.start();
    }

    private void startMotivationMessages() {
        final String[] messages = {
                "¿Estás listo para el desafío? 🏆",
                "¡Conviértete en el maestro de la serpiente! 🐍",
                "¿Puedes superar tu récord anterior? 🎯",
                "¡La aventura te espera! ✨",
                "¿Serás el campeón del Snake? 👑"
        };

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int currentIndex = 0;

            @Override
            public void run() {
                // Animación de salida
                motivationMessage.animate()
                        .alpha(0f)
                        .setDuration(300)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                // Cambiar texto
                                currentIndex = (currentIndex + 1) % messages.length;
                                motivationMessage.setText(messages[currentIndex]);

                                // Animación de entrada
                                motivationMessage.animate()
                                        .alpha(1f)
                                        .setDuration(300)
                                        .start();
                            }
                        })
                        .start();

                handler.postDelayed(this, 4000); // Cambiar cada 4 segundos
            }
        };

        handler.postDelayed(runnable, 4000);
    }
}