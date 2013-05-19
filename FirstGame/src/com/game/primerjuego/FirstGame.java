package com.game.primerjuego;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

public class FirstGame extends Activity {
	RenderGame renderGame;

	float tamTableroPixel;
	float tamBloquePixel;
	public static int disponible = 0;
	public static int jNegro = 1;
	public static int jBlanco = 2;
	public int jActual;
	int[][] tablero = new int[tamTablero][tamTablero];
	int[] desplazar = { 1, -1 };
	float margenY;

	public static int tamTablero = 8;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		renderGame = new RenderGame(this);
		jActual = jNegro;
		iniciarTablero(disponible, jNegro, jBlanco);
		setContentView(renderGame);
		renderGame.setOnTouchListener(new TouchListener());
	}

	@Override
	protected void onResume() {
		super.onResume();
		renderGame.resume();
	}

	@Override
	protected void onPause() {
		super.onResume();
		renderGame.pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_game, menu);
		return true;
	}

	class TouchListener implements OnTouchListener {
		float inicioX, inicioY;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				inicioX = event.getX();
				inicioY = event.getY();
				break;
			case MotionEvent.ACTION_UP:
				jugar(inicioX, inicioY, event.getX(), event.getY());
			}
			return true;
		}
	}

	class RenderGame extends SurfaceView implements Runnable {
		Thread thread = null;
		SurfaceHolder holder;

		volatile boolean running = false;
		Paint paint;
		Paint[] paints = { new Paint(), new Paint() };

		public RenderGame(Context context) {
			super(context);
			holder = getHolder();
			paint = new Paint();
			paint.setARGB(255, 151, 86, 20);
			paint.setStyle(Style.FILL);
			//paints[0].setARGB(255, 128, 0, 0);
			paints[0].setColor(Color.BLACK);
			paints[0].setStyle(Style.FILL_AND_STROKE);
			paints[1].setARGB(255, 128, 128, 255);
			paints[1].setStyle(Style.FILL_AND_STROKE);
			invalidate();
		}

		public void resume() {
			running = true;
			thread = new Thread(this);
			thread.start();
		}

		public void run() {
			while (running) {
				if (!holder.getSurface().isValid())
					continue;
				holder.unlockCanvasAndPost(dibujarTablero(holder.lockCanvas()));
			}
		}

		public Canvas dibujarTablero(Canvas canvas) {
			canvas.drawRGB(255, 255, 255);
			tamTableroPixel = canvas.getHeight();
			margenY = (canvas.getWidth() - tamTableroPixel) / 2;

			tamBloquePixel = tamTableroPixel / tamTablero;

			for (int i = 0; i < tamTablero; i++)
				for (int j = Math.abs(i % 2); j < tamTablero; j += 2)
					canvas.drawRect(tamBloquePixel * j + margenY,
							tamBloquePixel * i, tamBloquePixel * (j + 1)
									+ margenY, tamBloquePixel * (i + 1), paint);
			for (int i = 0; i < tamTablero; i++) {
				for (int j = 0; j < tamTablero; j++) {
					if (tablero[i][j] != disponible)
						canvas.drawCircle(tamBloquePixel * i + margenY
								+ tamBloquePixel / 2, tamBloquePixel * j
								+ tamBloquePixel / 2, tamBloquePixel / 3
								+ tamBloquePixel / 10,
								paints[tablero[i][j] - 1]);
				}
			}
			return canvas;
		}

		public void pause() {
			running = false;
			while (true) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					// Nada
				}
			}
		}
	}

	public void iniciarTablero(int d, int jP, int jS) {
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < tamTablero; j++)
				tablero[i][j] = jP;
		for (int i = 6; i < tamTablero; i++)
			for (int j = 0; j < tamTablero; j++)
				tablero[i][j] = jS;
	}

	public void jugar(float posX, float posY, float fposX, float fposY) {
		int pX = (int) Math.floor((posX * tamTablero) / tamTableroPixel
				- margenY / tamBloquePixel);
		int pY = (int) Math.floor((posY * tamTablero) / tamTableroPixel);
		int fpX = (int) Math.floor((fposX * tamTablero) / tamTableroPixel
				- margenY / tamBloquePixel);
		int fpY = (int) Math.floor((fposY * tamTablero) / tamTableroPixel);
		if (pX < 0 || pX > (tamTablero - 1))
			return;
		if (fpX < 0 || fpX > (tamTablero - 1))
			return;
		if (pY < 0 || pY > (tamTablero - 1))
			return;
		if (fpY < 0 || fpY > (tamTablero - 1))
			return;
		esValido(pX, pY, fpX, fpY);
	}

	public void esValido(int X, int Y, int fX, int fY) {
		int jugador = tablero[X][Y];
		if (jActual == jugador) {
			if (tablero[fX][fY] == disponible) {
				if (fX == (X + 1 * desplazar[jugador - 1])
						&& (fY == (Y + 1) || fY == (Y - 1))) {
					tablero[fX][fY] = jActual;
					tablero[X][Y] = disponible;
					jActual = jActual % 2 + 1;
				} else {
					comer(X, Y, fX, fY);
				}
			}
		}
	}

	public boolean comer(int X, int Y, int fX, int fY) {
		int jugador = tablero[X][Y];
		if (fX == (X + 2 * desplazar[jugador - 1])) {
			if (fY == (Y + 2)
					&& (tablero[(X + 1 * desplazar[jugador - 1])][Y + 1] == (jActual % 2 + 1))) {
				tablero[(X + 1 * desplazar[jugador - 1])][Y + 1] = disponible;
				tablero[fX][fY] = jActual;
				tablero[X][Y] = disponible;
				jActual = jActual % 2 + 1;
			} else if (fY == (Y - 2)
					&& (tablero[(X + 1 * desplazar[jugador - 1])][Y - 1] == (jActual % 2 + 1))) {
				tablero[(X + 1 * desplazar[jugador - 1])][Y - 1] = disponible;
				tablero[fX][fY] = jActual;
				tablero[X][Y] = disponible;
				jActual = jActual % 2 + 1;
			}
		}
		return true;
	}
}
