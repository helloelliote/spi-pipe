package kr.djspi.pipe01;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kr.djspi.pipe01.dto.PipeEntry;

/**
 * Adapter used to show a simple grid.
 */
public class PipeCardRecyclerViewAdapter extends Adapter<PipeCardViewHolder> {

    private List<PipeEntry> pipeList;

    PipeCardRecyclerViewAdapter(List<PipeEntry> pipeList) {
        this.pipeList = pipeList;
    }

    @NonNull
    @Override
    public PipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_pipe, parent, false);
        return new PipeCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull PipeCardViewHolder holder, int position) {
        if (pipeList != null && position < pipeList.size()) {
            PipeEntry pipe = pipeList.get(position);
//            holder.title_attr.setLabelText(pipe.title);
        }
    }

    @Override
    public int getItemCount() {
        return pipeList.size();
    }
}
