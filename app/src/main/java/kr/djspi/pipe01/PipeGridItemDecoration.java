//package kr.djspi.pipe01;
//
//import android.graphics.Rect;
//import android.support.v7.widget.RecyclerView;
//import android.support.v7.widget.RecyclerView.ItemDecoration;
//import android.support.v7.widget.RecyclerView.State;
//import android.view.View;
//
//import org.jetbrains.annotations.NotNull;
//
///**
// * Custom item decoration for a vertical {@link PipeRecordActivity} {@link RecyclerView}. Adds a
// * small amount of padding to the left of grid items, and a large amount of padding to the right.
// */
//public class PipeGridItemDecoration extends ItemDecoration {
//
//    private int largePadding;
//    private int smallPadding;
//
//    PipeGridItemDecoration(int largePadding, int smallPadding) {
//        this.largePadding = largePadding;
//        this.smallPadding = smallPadding;
//    }
//
//    @Override
//    public void getItemOffsets(
//            @NotNull Rect outRect, @NotNull View view, @NotNull RecyclerView parent, @NotNull State state) {
//        outRect.left = smallPadding;
//        outRect.right = largePadding;
//        outRect.top = largePadding;
//        outRect.bottom = largePadding;
//    }
//}
//
