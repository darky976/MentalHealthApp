package com.example.mentalhealth;

import static org.junit.Assert.*;
import org.junit.Test;
import com.example.mentalhealth.models.Note;
import com.example.mentalhealth.models.MoodEntry;

public class AppLogicTest {
    // Тесты для модели Заметок (Note)
    @Test
    public void testNoteContent() {
        Note note = new Note("1", "nb1", "u1", "Тестовая заметка", "2024", "2024");
        assertEquals("Тестовая заметка", note.content);
    }
    @Test
    public void testNoteDefaultState() {
        Note note = new Note();
        assertFalse(note.pinned);
        assertFalse(note.bold);
    }
    @Test
    public void testNoteFormatting() {
        Note note = new Note();
        note.bold = true;
        assertTrue(note.bold);
    }
    // Тесты для модели Настроения (MoodEntry)
    @Test
    public void testMoodValue() {
        MoodEntry entry = new MoodEntry();
        entry.mood = 5;
        assertEquals(5, entry.mood);
    }
    @Test
    public void testMoodComment() {
        MoodEntry entry = new MoodEntry();
        entry.comment = "Чувствую себя отлично";
        assertEquals("Чувствую себя отлично", entry.comment);
    }
    @Test
    public void testMoodDate() {
        MoodEntry entry = new MoodEntry();
        entry.date = "2024-05-20";
        assertEquals("2024-05-20", entry.date);
    }
    // Тесты логики объектов
    @Test
    public void testNoteIdNotNull() {
        Note note = new Note("123", "1", "1", "text", "now", "now");
        assertNotNull(note.id);
    }
    @Test
    public void testNoteContainer() {
        Note note = new Note();
        note.containerName = "General";
        assertEquals("General", note.containerName);
    }
    @Test
    public void testMoodIdAssignment() {
        MoodEntry entry = new MoodEntry();
        entry.id = "uuid-123";
        assertEquals("uuid-123", entry.id);
    }
    @Test
    public void testNoteTimestamp() {
        Note note = new Note();
        assertTrue(note.localCreatedAt > 0);
    }
}
