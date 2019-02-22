package kr.djspi.pipe01;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class PipeCardViewHolder extends RecyclerView.ViewHolder {

    public TextView pipeTitle;
    public TextView pipeType;

    public PipeCardViewHolder(@NonNull View itemView) {
        super(itemView);
        pipeTitle = itemView.findViewById(R.id.pipe_title);
        pipeType = itemView.findViewById(R.id.pipe_type);
    }
}
