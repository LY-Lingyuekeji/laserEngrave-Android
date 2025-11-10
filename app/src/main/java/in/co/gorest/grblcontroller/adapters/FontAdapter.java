package in.co.gorest.grblcontroller.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class FontAdapter extends RecyclerView.Adapter<FontAdapter.FontViewHolder> {
    private String[] fontNames;
    private OnFontClickListener listener;

    public FontAdapter(String[] fontNames, OnFontClickListener listener) {
        this.fontNames = fontNames;
        this.listener = listener;
    }

    @Override
    public FontViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new FontViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FontViewHolder holder, int position) {
        String fontName = fontNames[position];
        holder.textView.setText(fontName);
        holder.itemView.setOnClickListener(v -> listener.onFontClick(fontName));
    }

    @Override
    public int getItemCount() {
        return fontNames.length;
    }

    public static class FontViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public FontViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnFontClickListener {
        void onFontClick(String fontName);
    }
}
