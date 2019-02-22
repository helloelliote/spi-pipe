package kr.djspi.pipe01;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Custom item decoration for a vertical {@link PipeRecordActivity} {@link RecyclerView}. Adds a
 * small amount of padding to the left of grid items, and a large amount of padding to the right.
 */
public class PipeGridItemDecoration extends RecyclerView.ItemDecoration {

    private int largePadding;
    private int smallPadding;

    public PipeGridItemDecoration(int largePadding, int smallPadding) {
        this.largePadding = largePadding;
        this.smallPadding = smallPadding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = smallPadding;
        outRect.right = largePadding;
        outRect.top = largePadding;
        outRect.bottom = largePadding;
    }
}

