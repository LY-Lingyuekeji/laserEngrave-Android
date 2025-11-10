package in.co.gorest.grblcontroller.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import java.util.List;
import in.co.gorest.grblcontroller.PuzzleActivity;

public class PuzzleView extends View {
    private static final String TAG = PuzzleView.class.getSimpleName();
    private Context context;
    private List<Bitmap> imageList;
    private int currentLevel = 0;
    private int gridSize = 2;

    private Bitmap back;
    private Paint paint;
    private int tileWidth;
    private int tileHeight;
    private Bitmap[] bitmapTiles;
    private int[][] dataTiles;
    private Board tilesBoard;
    private int[][] dir = {
            {-1, 0},//左
            {0, -1},//上
            {1, 0},//右
            {0, 1}//下
    };
    private boolean isSuccess;
    private int steps = 0;

    public PuzzleView(Context context) {
        super(context);
        this.context = context;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    public void setImageList(List<Bitmap> imageList) {
        this.imageList = imageList;
    }

    public void startGame() {
        if (imageList == null || currentLevel >= imageList.size()) return;

        back = Bitmap.createScaledBitmap(imageList.get(currentLevel), PuzzleActivity.getScreenWidth(), PuzzleActivity.getScreenHeight(), true);
        int row = gridSize;
        int col = gridSize;

        tileWidth = back.getWidth() / col;
        tileHeight = back.getHeight() / row;
        bitmapTiles = new Bitmap[row * col];
        int idx = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                bitmapTiles[idx++] = Bitmap.createBitmap(back, j * tileWidth, i * tileHeight, tileWidth, tileHeight);
            }
        }

        tilesBoard = new Board();
        dataTiles = tilesBoard.createRandomBoard(row, col);
        isSuccess = false;
        steps = 0;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.GRAY);
        int row = gridSize;
        int col = gridSize;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                int idx = dataTiles[i][j];
                if (idx == row * col - 1 && !isSuccess) continue;
                canvas.drawBitmap(bitmapTiles[idx], j * tileWidth, i * tileHeight, paint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int col = x / tileWidth;
            int row = y / tileHeight;

            for (int[] d : dir) {
                int newX = col + d[0];
                int newY = row + d[1];

                if (newX >= 0 && newX < gridSize && newY >= 0 && newY < gridSize) {
                    if (dataTiles[newY][newX] == gridSize * gridSize - 1) {
                        steps++;
                        int temp = dataTiles[row][col];
                        dataTiles[row][col] = dataTiles[newY][newX];
                        dataTiles[newY][newX] = temp;
                        invalidate();
                        if (tilesBoard.isSuccess(dataTiles)) {
                            isSuccess = true;
                            invalidate();
                            showSuccessDialog();
                        }
                    }
                }
            }
        }
        return true;
    }

    private void showSuccessDialog() {
        String msg = String.format("恭喜你拼图成功，移动了%d次", steps);
        new AlertDialog.Builder(context)
                .setTitle("拼图成功")
                .setCancelable(false)
                .setMessage(msg)
                .setPositiveButton("下一关", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentLevel++;
                        gridSize++;
                        if (currentLevel < imageList.size()) {
                            startGame();
                        } else {
                            new AlertDialog.Builder(context)
                                    .setTitle("🎉 全部通关")
                                    .setMessage("你完成了所有拼图关卡！")
                                    .setPositiveButton("退出", (d, w) -> System.exit(0))
                                    .show();
                        }
                    }
                })
                .setNegativeButton("退出游戏", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                })
                .create()
                .show();
    }
}
