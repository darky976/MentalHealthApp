package com.example.mentalhealth.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mentalhealth.R;
import com.example.mentalhealth.adapters.NoteAdapter;
import com.example.mentalhealth.models.Note;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NotesFragment extends Fragment {

    private NoteAdapter adapter;
    private EditText editNoteContent;
    private ImageButton btnSaveNote;
    private ImageButton btnCancelEdit;
    private ImageButton btnToggleBold;
    private ImageButton btnToggleItalic;
    private ImageButton btnPrevPin;
    private ImageButton btnNextPin;
    private LinearLayout editingStateContainer;
    private TextView tvEditingState;
    private TextView tvPinnedNav;
    private TextView tvSelectedContainer;
    private Button btnFilterAll;
    private Button btnFilterPinned;
    private Button btnFilterArchive;
    private Button btnSwitchContainer;
    private Button btnAddContainer;
    private ImageButton btnDeleteContainer;
    private String editingNoteId = null;
    private String userId;
    private SharedPreferences prefs;
    private final Gson gson = new Gson();
    private final List<Note> localNotes = new ArrayList<>();
    private final List<Note> visibleNotes = new ArrayList<>();
    private final List<String> containers = new ArrayList<>();
    private boolean draftBold = false;
    private boolean draftItalic = false;
    private int currentFilter = 0; // 0 all, 1 pinned, 2 archived
    private int pinnedNavIndex = -1;
    private String selectedContainer = null;

    private static final String PREFS_NAME = "app_prefs";
    private static final String NOTES_KEY_PREFIX = "local_notes_";
    private static final String CONTAINERS_KEY_PREFIX = "local_containers_";
    private static final String SELECTED_CONTAINER_KEY_PREFIX = "selected_container_";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
        if (userId == null || userId.trim().isEmpty()) {
            userId = "guest";
        }

        editNoteContent = view.findViewById(R.id.edit_note_content);
        btnSaveNote = view.findViewById(R.id.btn_save_note);
        btnCancelEdit = view.findViewById(R.id.btn_cancel_edit);
        btnToggleBold = view.findViewById(R.id.btn_toggle_bold);
        btnToggleItalic = view.findViewById(R.id.btn_toggle_italic);
        btnPrevPin = view.findViewById(R.id.btn_prev_pin);
        btnNextPin = view.findViewById(R.id.btn_next_pin);
        editingStateContainer = view.findViewById(R.id.editing_state_container);
        tvEditingState = view.findViewById(R.id.tv_editing_state);
        tvPinnedNav = view.findViewById(R.id.tv_pinned_nav);
        tvSelectedContainer = view.findViewById(R.id.tv_selected_container);
        btnFilterAll = view.findViewById(R.id.btn_filter_all);
        btnFilterPinned = view.findViewById(R.id.btn_filter_pinned);
        btnFilterArchive = view.findViewById(R.id.btn_filter_archive);
        btnSwitchContainer = view.findViewById(R.id.btn_switch_container);
        btnAddContainer = view.findViewById(R.id.btn_add_container);
        btnDeleteContainer = view.findViewById(R.id.btn_delete_container);
        RecyclerView rvNotes = view.findViewById(R.id.rv_notes);

        adapter = new NoteAdapter(new ArrayList<>(), new NoteAdapter.OnNoteClickListener() {
            @Override
            public void onDeleteClick(Note note) {
                confirmDelete(note.id);
            }

            @Override
            public void onEditClick(Note note) {
                editingNoteId = note.id;
                editNoteContent.setText(note.content);
                editNoteContent.setSelection(editNoteContent.getText().length());
                editNoteContent.requestFocus();
                draftBold = note.bold;
                draftItalic = note.italic;
                updateFormattingButtons();
                enterEditMode();
            }

            @Override
            public void onLongClick(Note note) {
                showNoteActions(note);
            }
        });

        rvNotes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotes.setAdapter(adapter);

        loadContainers();
        loadNotes();

        btnSaveNote.setOnClickListener(v -> {
            String content = editNoteContent.getText().toString().trim();
            if (content.isEmpty()) return;
            if (selectedContainer == null || selectedContainer.trim().isEmpty()) {
                Toast.makeText(getContext(), "Create and select a container first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (editingNoteId != null) {
                updateNote(content);
            } else {
                saveNewNote(content);
            }
        });
        btnCancelEdit.setOnClickListener(v -> exitEditMode());
        btnToggleBold.setOnClickListener(v -> {
            draftBold = !draftBold;
            updateFormattingButtons();
        });
        btnToggleItalic.setOnClickListener(v -> {
            draftItalic = !draftItalic;
            updateFormattingButtons();
        });
        btnFilterAll.setOnClickListener(v -> setFilter(0));
        btnFilterPinned.setOnClickListener(v -> setFilter(1));
        btnFilterArchive.setOnClickListener(v -> setFilter(2));
        btnPrevPin.setOnClickListener(v -> navigatePinned(-1, rvNotes));
        btnNextPin.setOnClickListener(v -> navigatePinned(1, rvNotes));
        btnAddContainer.setOnClickListener(v -> showAddContainerDialog());
        btnSwitchContainer.setOnClickListener(v -> showSwitchContainerDialog());
        btnDeleteContainer.setOnClickListener(v -> confirmDeleteContainer());

        updateFormattingButtons();
        updateFilterButtons();
        updateContainerUi();
        return view;
    }

    private void loadContainers() {
        containers.clear();
        String json = prefs.getString(getContainersKey(), "[]");
        List<String> fromStorage = gson.fromJson(json, new TypeToken<List<String>>() {}.getType());
        if (fromStorage != null) {
            for (String c : fromStorage) {
                if (c != null && !c.trim().isEmpty() && !containers.contains(c.trim())) {
                    containers.add(c.trim());
                }
            }
        }
        selectedContainer = prefs.getString(getSelectedContainerKey(), null);
        if (selectedContainer != null && !containers.contains(selectedContainer)) {
            selectedContainer = null;
        }
        if (selectedContainer == null && !containers.isEmpty()) {
            selectedContainer = containers.get(0);
            prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
        }
        if (containers.isEmpty()) {
            containers.add("General");
            selectedContainer = "General";
            persistContainers();
            prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
        }
    }

    private void showAddContainerDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Container name");
        new AlertDialog.Builder(requireContext())
                .setTitle("Create container")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) return;
                    if (containers.contains(name)) {
                        Toast.makeText(getContext(), "Container already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    containers.add(name);
                    selectedContainer = name;
                    persistContainers();
                    prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
                    updateContainerUi();
                    applyFilterAndRender();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSwitchContainerDialog() {
        if (containers.isEmpty()) {
            Toast.makeText(getContext(), "No containers yet. Create one first.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] items = containers.toArray(new String[0]);
        int selectedIndex = selectedContainer == null ? 0 : Math.max(0, containers.indexOf(selectedContainer));
        new AlertDialog.Builder(requireContext())
                .setTitle("Choose container")
                .setSingleChoiceItems(items, selectedIndex, (dialog, which) -> {
                    selectedContainer = items[which];
                })
                .setPositiveButton("Select", (dialog, which) -> {
                    prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
                    updateContainerUi();
                    applyFilterAndRender();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void confirmDeleteContainer() {
        if (selectedContainer == null || selectedContainer.trim().isEmpty()) {
            Toast.makeText(getContext(), "No container selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (containers.size() <= 1) {
            Toast.makeText(getContext(), "At least one container must remain", Toast.LENGTH_SHORT).show();
            return;
        }
        int notesCount = 0;
        for (Note note : localNotes) {
            if (selectedContainer.equals(note.containerName)) {
                notesCount++;
            }
        }
        String deletingContainer = selectedContainer;
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete container?")
                .setMessage("Delete '" + deletingContainer + "' and its " + notesCount + " notes?")
                .setPositiveButton("Delete", (dialog, which) -> deleteContainer(deletingContainer))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteContainer(String containerName) {
        localNotes.removeIf(note -> containerName.equals(note.containerName));
        containers.remove(containerName);
        if (containers.isEmpty()) {
            containers.add("General");
        }
        selectedContainer = containers.get(0);
        persistNotes();
        persistContainers();
        prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
        updateContainerUi();
        applyFilterAndRender();
        Toast.makeText(getContext(), "Container deleted", Toast.LENGTH_SHORT).show();
    }

    private void loadNotes() {
        localNotes.clear();
        String json = prefs.getString(getNotesKey(), "[]");
        List<Note> fromStorage = gson.fromJson(json, new TypeToken<List<Note>>() {}.getType());
        if (fromStorage != null) {
            long fallbackTime = System.currentTimeMillis();
            boolean migrated = false;
            for (Note note : fromStorage) {
                if (note.id == null || note.id.trim().isEmpty()) {
                    note.id = UUID.randomUUID().toString();
                    migrated = true;
                }
                if (note.localCreatedAt <= 0) {
                    note.localCreatedAt = fallbackTime;
                    fallbackTime -= 1000;
                    migrated = true;
                }
                if (note.containerName == null || note.containerName.trim().isEmpty()) {
                    note.containerName = "Inbox";
                    if (!containers.contains("Inbox")) {
                        containers.add("Inbox");
                    }
                    if (selectedContainer == null) {
                        selectedContainer = "Inbox";
                    }
                    migrated = true;
                }
            }
            localNotes.addAll(fromStorage);
            if (migrated) {
                persistContainers();
                prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
                persistNotes();
            }
        }
        if ((selectedContainer == null || selectedContainer.trim().isEmpty()) && !containers.isEmpty()) {
            selectedContainer = containers.get(0);
            prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
        }
        sortNotes(localNotes);
        updateContainerUi();
        applyFilterAndRender();
    }

    private void saveNewNote(String content) {
        Note note = new Note();
        note.id = UUID.randomUUID().toString();
        note.content = content;
        note.userId = userId;
        note.bold = draftBold;
        note.italic = draftItalic;
        note.localCreatedAt = System.currentTimeMillis();
        note.containerName = selectedContainer;
        localNotes.add(note);
        persistNotes();
        exitEditMode();
        loadNotes();
        Toast.makeText(getContext(), "Saved in " + selectedContainer, Toast.LENGTH_SHORT).show();
    }

    private void updateNote(String content) {
        for (Note note : localNotes) {
            if (note.id != null && note.id.equals(editingNoteId)) {
                note.content = content;
                note.userId = userId;
                note.bold = draftBold;
                note.italic = draftItalic;
                note.containerName = selectedContainer;
                note.updatedAt = String.valueOf(System.currentTimeMillis());
                break;
            }
        }
        persistNotes();
        exitEditMode();
        loadNotes();
        Toast.makeText(getContext(), "Updated", Toast.LENGTH_SHORT).show();
    }

    private void deleteNote(String id) {
        localNotes.removeIf(note -> note.id != null && note.id.equals(id));
        persistNotes();
        loadNotes();
        Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete(String id) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete note?")
                .setMessage("This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteNote(id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void togglePin(String id) {
        if (id == null || id.trim().isEmpty()) return;
        for (Note note : localNotes) {
            if (id.equals(note.id)) {
                note.pinned = !note.pinned;
                Toast.makeText(getContext(), note.pinned ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        persistNotes();
        loadNotes();
    }

    private void toggleArchive(String id) {
        if (id == null || id.trim().isEmpty()) return;
        for (Note note : localNotes) {
            if (id.equals(note.id)) {
                note.archived = !note.archived;
                if (note.archived) note.pinned = false;
                Toast.makeText(getContext(), note.archived ? "Moved to archive" : "Restored", Toast.LENGTH_SHORT).show();
                break;
            }
        }
        persistNotes();
        loadNotes();
    }

    private void showNoteActions(Note note) {
        String archiveLabel = note.archived ? "Restore from archive" : "Move to archive";
        String pinLabel = note.pinned ? "Unpin" : "Pin";
        String[] actions = new String[] {"Edit", pinLabel, archiveLabel};
        new AlertDialog.Builder(requireContext())
                .setTitle("Note actions")
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        editingNoteId = note.id;
                        editNoteContent.setText(note.content);
                        editNoteContent.setSelection(editNoteContent.getText().length());
                        draftBold = note.bold;
                        draftItalic = note.italic;
                        if (note.containerName != null && !note.containerName.trim().isEmpty()) {
                            selectedContainer = note.containerName;
                        }
                        updateContainerUi();
                        updateFormattingButtons();
                        enterEditMode();
                    } else if (which == 1) {
                        togglePin(note.id);
                    } else if (which == 2) {
                        toggleArchive(note.id);
                    }
                })
                .show();
    }

    private void enterEditMode() {
        editingStateContainer.setVisibility(View.VISIBLE);
        tvEditingState.setText("Editing note");
        btnSaveNote.setImageResource(android.R.drawable.ic_menu_edit);
    }

    private void exitEditMode() {
        editingNoteId = null;
        editNoteContent.setText("");
        draftBold = false;
        draftItalic = false;
        updateFormattingButtons();
        editingStateContainer.setVisibility(View.GONE);
        btnSaveNote.setImageResource(android.R.drawable.ic_menu_send);
    }

    private void updateFormattingButtons() {
        btnToggleBold.setAlpha(draftBold ? 1f : 0.45f);
        btnToggleItalic.setAlpha(draftItalic ? 1f : 0.45f);
        applyEditorTypeface();
    }

    private void applyEditorTypeface() {
        int style = Typeface.NORMAL;
        if (draftBold && draftItalic) style = Typeface.BOLD_ITALIC;
        else if (draftBold) style = Typeface.BOLD;
        else if (draftItalic) style = Typeface.ITALIC;
        editNoteContent.setTypeface(null, style);
    }

    private void sortNotes(List<Note> list) {
        Collections.sort(list, (left, right) -> {
            if (left.pinned != right.pinned) return left.pinned ? -1 : 1;
            return Long.compare(right.localCreatedAt, left.localCreatedAt);
        });
    }

    private void setFilter(int filter) {
        currentFilter = filter;
        applyFilterAndRender();
    }

    private void applyFilterAndRender() {
        visibleNotes.clear();
        for (Note note : localNotes) {
            if (selectedContainer != null && note.containerName != null && !selectedContainer.equals(note.containerName)) {
                continue;
            }
            if (selectedContainer != null && (note.containerName == null || note.containerName.trim().isEmpty())) {
                continue;
            }
            boolean include;
            if (currentFilter == 1) include = note.pinned && !note.archived;
            else if (currentFilter == 2) include = note.archived;
            else include = true;
            if (include) visibleNotes.add(note);
        }
        if (visibleNotes.isEmpty() && currentFilter == 0 && !localNotes.isEmpty()) {
            String fallbackContainer = localNotes.get(0).containerName;
            if (fallbackContainer != null && !fallbackContainer.trim().isEmpty() && !fallbackContainer.equals(selectedContainer)) {
                selectedContainer = fallbackContainer;
                prefs.edit().putString(getSelectedContainerKey(), selectedContainer).apply();
                updateContainerUi();
                applyFilterAndRender();
                return;
            }
        }
        adapter.updateData(new ArrayList<>(visibleNotes));
        updatePinnedNav();
        updateFilterButtons();
    }

    private void updatePinnedNav() {
        List<Note> pinned = getPinnedVisibleNotes();
        if (pinned.isEmpty()) {
            pinnedNavIndex = -1;
            tvPinnedNav.setText("No pinned notes");
            btnPrevPin.setEnabled(false);
            btnNextPin.setEnabled(false);
            return;
        }
        if (pinnedNavIndex < 0 || pinnedNavIndex >= pinned.size()) pinnedNavIndex = 0;
        tvPinnedNav.setText("Pinned " + (pinnedNavIndex + 1) + "/" + pinned.size());
        btnPrevPin.setEnabled(true);
        btnNextPin.setEnabled(true);
    }

    private List<Note> getPinnedVisibleNotes() {
        List<Note> pinned = new ArrayList<>();
        for (Note note : visibleNotes) {
            if (note.pinned && !note.archived) pinned.add(note);
        }
        return pinned;
    }

    private void navigatePinned(int step, RecyclerView recyclerView) {
        List<Note> pinned = getPinnedVisibleNotes();
        if (pinned.isEmpty()) return;
        pinnedNavIndex = (pinnedNavIndex + step + pinned.size()) % pinned.size();
        Note target = pinned.get(pinnedNavIndex);
        int position = -1;
        for (int i = 0; i < visibleNotes.size(); i++) {
            Note n = visibleNotes.get(i);
            if (n.id != null && n.id.equals(target.id)) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            recyclerView.smoothScrollToPosition(position);
            tvPinnedNav.setText("Pinned " + (pinnedNavIndex + 1) + "/" + pinned.size());
        }
    }

    private void updateFilterButtons() {
        btnFilterAll.setAlpha(currentFilter == 0 ? 1f : 0.55f);
        btnFilterPinned.setAlpha(currentFilter == 1 ? 1f : 0.55f);
        btnFilterArchive.setAlpha(currentFilter == 2 ? 1f : 0.55f);
    }

    private void updateContainerUi() {
        String label = selectedContainer == null ? "Container: none" : "Container: " + selectedContainer;
        tvSelectedContainer.setText(label);
        btnDeleteContainer.setEnabled(selectedContainer != null && !selectedContainer.trim().isEmpty());
    }

    private void persistNotes() {
        prefs.edit().putString(getNotesKey(), gson.toJson(localNotes)).apply();
    }

    private void persistContainers() {
        prefs.edit().putString(getContainersKey(), gson.toJson(containers)).apply();
    }

    private String getNotesKey() {
        return NOTES_KEY_PREFIX + userId;
    }

    private String getContainersKey() {
        return CONTAINERS_KEY_PREFIX + userId;
    }

    private String getSelectedContainerKey() {
        return SELECTED_CONTAINER_KEY_PREFIX + userId;
    }
}
