package in.co.gorest.grblcontroller.model;

public enum ScanDirection {
    HORIZONTAL,      // 横向：左右蛇形
    VERTICAL,        // 纵向：上下蛇形
    DIAGONAL_LD_RU,  // 左上 → 右下 ↘
    DIAGONAL_LU_RD   // 左下 → 右上 ↗
}
