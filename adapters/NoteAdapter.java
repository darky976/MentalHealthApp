package com.example.mentalhealth.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mentalhealth.R;
import com.example.mentalhealth.models.Note;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private List<Note> notes;
    private final OnNoteClickListener listener;

    public interface OnNoteClickListener {
        void onDeleteClick(Note note);
        void onEditClick(Note note);
        void onLongClick(Note note);
    }

    public NoteAdapter(List<Note> notes, OnNoteClickListener listener) {
        this.notes = notes;
        this.listener = listener;
    }

    public void updateData(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvContent.setText(note.content);
        int typeface = Typeface.NORMAL;
        if (note.bold && note.italic) {
            typeface = Typeface.BOLD_ITALIC;
        } else if (note.bold) {
            typeface = Typeface.BOLD;
        } else if (note.italic) {
            typeface = Typeface.ITALIC;
        }
        holder.tvContent.setTypeface(null, typeface);
        holder.tvDate.setText(formatDate(note.localCreatedAt));
        holder.tvPinnedBadge.setVisibility(note.pinned ? View.VISIBLE : View.GONE);
        holder.tvArchivedBadge.setVisibility(note.archived ? View.VISIBLE : View.GONE);
        
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(note);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLongClick(note);
                return true;
            }
            return false;
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(note);
            }
        });
    }

    private String formatDate(long timestamp) {
        if (timestamp <= 0) {
            return "No date";
        }
        return new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return notes == null ? 0 : notes.size();
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;
        TextView tvDate;
        TextView tvPinnedBadge;
        TextView tvArchivedBadge;
        ImageButton btnDelete;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_note_content);
            tvDate = itemView.findViewById(R.id.tv_note_date);
            tvPinnedBadge = itemView.findViewById(R.id.tv_note_pinned);
            tvArchivedBadge = itemView.findViewById(R.id.tv_note_archived);
            btnDelete = itemView.findViewById(R.id.btn_delete_note);
        }
    }
}
