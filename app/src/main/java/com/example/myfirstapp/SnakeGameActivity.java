package com.example.myfirstapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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

        // Control de tiempo mejorado
        private long lastFrameTime;
        private final long TARGET_FPS = 10;
        private final long FRAME_TIME = 1000 / TARGET_FPS;

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
            paint.setAntiAlias(true);

            // Inicializar botones de control con mejor posicionamiento
            int buttonSize = blockSize * 3;
            int margin = blockSize;

            // Posicionar botones en la parte inferior
            int controlAreaY = screenY - buttonSize * 5;

            upButton = new Rect(screenX / 2 - buttonSize / 2, controlAreaY,
                    screenX / 2 + buttonSize / 2, controlAreaY + buttonSize);

            downButton = new Rect(screenX / 2 - buttonSize / 2, controlAreaY + buttonSize * 2,
                    screenX / 2 + buttonSize / 2, controlAreaY + buttonSize * 3);

            leftButton = new Rect(screenX / 2 - buttonSize * 2, controlAreaY + buttonSize,
                    screenX / 2 - buttonSize, controlAreaY + buttonSize * 2);

            rightButton = new Rect(screenX / 2 + buttonSize, controlAreaY + buttonSize,
                    screenX / 2 + buttonSize * 2, controlAreaY + buttonSize * 2);

            startGame();
        }

        @Override
        public void run() {
            while (playing) {
                long startFrameTime = System.currentTimeMillis();

                // Solo actualizar si ha pasado suficiente tiempo
                if (updateRequired()) {
                    updateGame();
                }

                drawGame();

                // Controlar la velocidad del juego
                long frameTime = System.currentTimeMillis() - startFrameTime;
                if (frameTime < FRAME_TIME) {
                    try {
                        Thread.sleep(FRAME_TIME - frameTime);
                    } catch (InterruptedException e) {
                        // Manejar interrupción
                    }
                }
            }
        }

        public void pause() {
            playing = false;
            try {
                if (thread != null) {
                    thread.join();
                }
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

            // Reiniciar tiempo
            lastFrameTime = System.currentTimeMillis();
        }

        public void spawnBob() {
            Random random = new Random();
            boolean validPosition = false;

            // Asegurar que la comida no aparezca en el área de controles
            int maxY = numBlocksHigh - 8; // Evitar área de botones

            while (!validPosition) {
                bobX = random.nextInt(NUM_BLOCKS_WIDE - 1) + 1;
                bobY = random.nextInt(maxY - 1) + 1;

                // Verificar que no esté en la serpiente
                validPosition = true;
                for (Point segment : snakeXYs) {
                    if (segment.x == bobX && segment.y == bobY) {
                        validPosition = false;
                        break;
                    }
                }

                // Verificar que no esté en la cabeza
                if (headX == bobX && headY == bobY) {
                    validPosition = false;
                }
            }
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
            // Añadir nuevo segmento si es necesario
            if (snakeXYs.size() < snakeLength) {
                snakeXYs.add(new Point());
            }

            // Mover el cuerpo
            for (int i = snakeXYs.size() - 1; i > 0; i--) {
                snakeXYs.get(i).x = snakeXYs.get(i - 1).x;
                snakeXYs.get(i).y = snakeXYs.get(i - 1).y;
            }

            // Mover el primer segmento a la posición anterior de la cabeza
            if (snakeXYs.size() > 0) {
                snakeXYs.get(0).x = headX;
                snakeXYs.get(0).y = headY;
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
            if (headY >= numBlocksHigh - 8) dead = true; // Considerar área de botones

            // Comerse a sí mismo
            for (Point segment : snakeXYs) {
                if (headX == segment.x && headY == segment.y) {
                    dead = true;
                    break;
                }
            }

            return dead;
        }

        public void updateGame() {
            // ¿La serpiente ha golpeado la comida?
            if (headX == bobX && headY == bobY) {
                eatBob();
            }

            moveSnake();

            if (detectDeath()) {
                // Pequeña pausa antes de reiniciar
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // Manejar interrupción
                }
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

                // Dibujar el cuerpo de la serpiente
                for (Point segment : snakeXYs) {
                    canvas.drawRect(segment.x * blockSize,
                            segment.y * blockSize,
                            (segment.x * blockSize) + blockSize,
                            (segment.y * blockSize) + blockSize,
                            paint);
                }

                // Dibujar la cabeza con color diferente
                paint.setColor(Color.argb(255, 200, 255, 200));
                canvas.drawRect(headX * blockSize,
                        headY * blockSize,
                        (headX * blockSize) + blockSize,
                        (headY * blockSize) + blockSize,
                        paint);

                // Dibujar la comida
                paint.setColor(Color.argb(255, 255, 0, 0));
                canvas.drawRect(bobX * blockSize,
                        bobY * blockSize,
                        (bobX * blockSize) + blockSize,
                        (bobY * blockSize) + blockSize,
                        paint);

                // Dibujar controles con mejor visibilidad
                paint.setColor(Color.argb(120, 255, 255, 255));
                canvas.drawRect(upButton, paint);
                canvas.drawRect(downButton, paint);
                canvas.drawRect(leftButton, paint);
                canvas.drawRect(rightButton, paint);

                // Dibujar bordes de los botones
                paint.setColor(Color.argb(180, 255, 255, 255));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                canvas.drawRect(upButton, paint);
                canvas.drawRect(downButton, paint);
                canvas.drawRect(leftButton, paint);
                canvas.drawRect(rightButton, paint);
                paint.setStyle(Paint.Style.FILL);

                // Dibujar texto en los botones
                paint.setColor(Color.argb(255, 0, 0, 0));
                paint.setTextSize(blockSize * 1.5f);
                paint.setTextAlign(Paint.Align.CENTER);

                canvas.drawText("↑", upButton.centerX(), upButton.centerY() + blockSize/2, paint);
                canvas.drawText("↓", downButton.centerX(), downButton.centerY() + blockSize/2, paint);
                canvas.drawText("←", leftButton.centerX(), leftButton.centerY() + blockSize/2, paint);
                canvas.drawText("→", rightButton.centerX(), rightButton.centerY() + blockSize/2, paint);

                // Dibujar la puntuación
                paint.setColor(Color.argb(255, 255, 255, 255));
                paint.setTextSize(blockSize * 2);
                paint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText("Puntuación: " + score, blockSize, blockSize * 2, paint);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

        public boolean updateRequired() {
            // Verificar si es tiempo de actualizar el juego
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastFrameTime >= FRAME_TIME) {
                lastFrameTime = currentTime;
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