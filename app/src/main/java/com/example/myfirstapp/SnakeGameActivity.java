package com.example.myfirstapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGameActivity extends Activity {

    SnakeEngine snakeEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        snakeEngine = new SnakeEngine(this, size);
        setContentView(snakeEngine);
    }

    @Override
    protected void onResume() {
        super.onResume();
        snakeEngine.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        snakeEngine.pause();
    }

    static class SnakeEngine extends SurfaceView implements Runnable {

        private Thread thread = null;
        private Context context;
        private SurfaceHolder surfaceHolder;
        private volatile boolean playing;
        private Canvas canvas;
        private Paint paint;

        // Configuración del juego
        private int screenX;
        private int screenY;
        private int blockSize;
        private final int NUM_BLOCKS_WIDE = 40;
        private int numBlocksHigh;

        // Objetos del juego
        private int headX;
        private int headY;
        private ArrayList<Point> snakeXYs;
        private int snakeLength;

        // Comida
        private int bobX;
        private int bobY;

        // La dirección de la serpiente
        private enum Heading {UP, RIGHT, DOWN, LEFT}
        private Heading heading = Heading.RIGHT;

        // Puntuación
        private int score;

        // Control de FPS
        private long fps;
        private long timeThisFrame;

        // Botones de control
        private Rect upButton;
        private Rect downButton;
        private Rect leftButton;
        private Rect rightButton;

        public SnakeEngine(Context context, Point size) {
            super(context);

            this.context = context;
            screenX = size.x;
            screenY = size.y;

            // Calcular el tamaño de los bloques en píxeles
            blockSize = screenX / NUM_BLOCKS_WIDE;
            numBlocksHigh = screenY / blockSize;

            surfaceHolder = getHolder();
            paint = new Paint();

            // Inicializar botones de control
            int buttonSize = blockSize * 3;
            int margin = blockSize;

            upButton = new Rect(screenX / 2 - buttonSize / 2, screenY - buttonSize * 4 - margin * 3,
                    screenX / 2 + buttonSize / 2, screenY - buttonSize * 3 - margin * 3);

            downButton = new Rect(screenX / 2 - buttonSize / 2, screenY - buttonSize - margin,
                    screenX / 2 + buttonSize / 2, screenY - margin);

            leftButton = new Rect(screenX / 2 - buttonSize * 2 - margin, screenY - buttonSize * 2 - margin * 2,
                    screenX / 2 - buttonSize - margin, screenY - buttonSize - margin * 2);

            rightButton = new Rect(screenX / 2 + buttonSize + margin, screenY - buttonSize * 2 - margin * 2,
                    screenX / 2 + buttonSize * 2 + margin, screenY - buttonSize - margin * 2);

            startGame();
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();

                if (!updateRequired()) {
                    updateGame();
                    drawGame();
                }

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void pause() {
            playing = false;
            try {
                thread.join();
            } catch (InterruptedException e) {
                // Error
            }
        }

        public void resume() {
            playing = true;
            thread = new Thread(this);
            thread.start();
        }

        public void startGame() {
            // Inicializar la serpiente
            snakeXYs = new ArrayList<>();
            snakeLength = 1;
            headX = NUM_BLOCKS_WIDE / 2;
            headY = numBlocksHigh / 2;

            // Generar primera comida
            spawnBob();

            // Reiniciar puntuación
            score = 0;
        }

        public void spawnBob() {
            Random random = new Random();
            bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
            bobY = random.nextInt(numBlocksHigh - 1) + 1;
        }

        private void eatBob() {
            // Incrementar la longitud de la serpiente
            snakeLength++;
            // Reemplazar la comida
            spawnBob();
            // Aumentar puntuación
            score = score + 1;
        }

        private void moveSnake() {
            // Mover el cuerpo
            for (int i = snakeXYs.size() - 1; i >= 0; i--) {
                if (i == 0) {
                    // Mover la cabeza
                    snakeXYs.get(0).x = headX;
                    snakeXYs.get(0).y = headY;
                } else {
                    // Mover el cuerpo
                    snakeXYs.get(i).x = snakeXYs.get(i - 1).x;
                    snakeXYs.get(i).y = snakeXYs.get(i - 1).y;
                }
            }

            // Mover la cabeza en la dirección actual
            switch (heading) {
                case UP:
                    headY--;
                    break;
                case RIGHT:
                    headX++;
                    break;
                case DOWN:
                    headY++;
                    break;
                case LEFT:
                    headX--;
                    break;
            }
        }

        private boolean detectDeath() {
            // Golpear las paredes
            boolean dead = false;

            if (headX == -1) dead = true;
            if (headX >= NUM_BLOCKS_WIDE) dead = true;
            if (headY == -1) dead = true;
            if (headY >= numBlocksHigh) dead = true;

            // Comerse a sí mismo
            for (int i = 0; i < snakeXYs.size(); i++) {
                if (headX == snakeXYs.get(i).x && headY == snakeXYs.get(i).y) {
                    dead = true;
                }
            }

            return dead;
        }

        public void updateGame() {
            // ¿La serpiente ha golpeado la comida?
            if (headX == bobX && headY == bobY) {
                eatBob();
            }

            // ¿Necesitamos agregar un nuevo segmento al cuerpo de la serpiente?
            if (snakeXYs.size() < snakeLength) {
                snakeXYs.add(new Point());
            }

            moveSnake();

            if (detectDeath()) {
                startGame();
            }
        }

        public void drawGame() {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();

                // Color de fondo
                canvas.drawColor(Color.argb(255, 26, 128, 182));

                // Configurar el pincel para la serpiente
                paint.setColor(Color.argb(255, 255, 255, 255));

                // Dibujar la serpiente
                for (int i = 0; i < snakeXYs.size(); i++) {
                    canvas.drawRect(snakeXYs.get(i).x * blockSize,
                            (snakeXYs.get(i).y * blockSize),
                            (snakeXYs.get(i).x * blockSize) + blockSize,
                            (snakeXYs.get(i).y * blockSize) + blockSize,
                            paint);
                }

                // Dibujar la cabeza
                canvas.drawRect(headX * blockSize,
                        (headY * blockSize),
                        (headX * blockSize) + blockSize,
                        (headY * blockSize) + blockSize,
                        paint);

                // Dibujar la comida
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawRect(bobX * blockSize,
                        (bobY * blockSize),
                        (bobX * blockSize) + blockSize,
                        (bobY * blockSize) + blockSize,
                        paint);

                // Dibujar controles
                paint.setColor(Color.argb(100, 255, 255, 255));
                canvas.drawRect(upButton, paint);
                canvas.drawRect(downButton, paint);
                canvas.drawRect(leftButton, paint);
                canvas.drawRect(rightButton, paint);

                // Dibujar texto en los botones
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(blockSize);
                canvas.drawText("↑", upButton.centerX() - blockSize/3, upButton.centerY() + blockSize/3, paint);
                canvas.drawText("↓", downButton.centerX() - blockSize/3, downButton.centerY() + blockSize/3, paint);
                canvas.drawText("←", leftButton.centerX() - blockSize/3, leftButton.centerY() + blockSize/3, paint);
                canvas.drawText("→", rightButton.centerX() - blockSize/3, rightButton.centerY() + blockSize/3, paint);

                // Dibujar la puntuación
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(blockSize * 2);
                canvas.drawText("Puntuación:" + score, blockSize, blockSize * 2, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        public boolean updateRequired() {
            // Solo actualizar cada 10 frames
            final long TARGET_FPS = 10;
            if (fps >= TARGET_FPS) {
                return true;
            }
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    int x = (int) motionEvent.getX();
                    int y = (int) motionEvent.getY();

                    if (upButton.contains(x, y)) {
                        if (heading != Heading.DOWN) {
                            heading = Heading.UP;
                        }
                    } else if (downButton.contains(x, y)) {
                        if (heading != Heading.UP) {
                            heading = Heading.DOWN;
                        }
                    } else if (leftButton.contains(x, y)) {
                        if (heading != Heading.RIGHT) {
                            heading = Heading.LEFT;
                        }
                    } else if (rightButton.contains(x, y)) {
                        if (heading != Heading.LEFT) {
                            heading = Heading.RIGHT;
                        }
                    }
                    break;
            }
            return true;
        }
    }
}