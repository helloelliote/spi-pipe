//package kr.djspi.pipe01;
//
//import android.support.annotation.NonNull;
//import android.support.v7.widget.RecyclerView.Adapter;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import java.util.List;
//
//import kr.djspi.pipe01.dto.Pipe_Alt;
//
///**
// * Adapter used to show a simple grid.
// */
//public class PipeCardRecyclerViewAdapter extends Adapter<PipeCardViewHolder> {
//
//    private List<Pipe_Alt> pipeAltEntries;
//
//    PipeCardRecyclerViewAdapter(List<Pipe_Alt> pipeAltEntries) {
//        this.pipeAltEntries = pipeAltEntries;
//    }
//
//    @NonNull
//    @Override
//    public PipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_pipe, parent, false);
//        return new PipeCardViewHolder(layoutView);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull PipeCardViewHolder holder, int position) {
//        if (pipeAltEntries != null && position < pipeAltEntries.size()) {
////            if (entry.shape != null) {
////                holder.shape.setText(entry.shape);
////                holder.shape.setEnabled(false);
////            }
////            holder.spec.setPrefix(entry.header + "  ");
////            holder.spec.setSuffix(entry.unit);
//
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return pipeAltEntries.size();
//    }
//}
