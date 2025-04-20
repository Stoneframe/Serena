package stoneframe.serena.notes;

import androidx.annotation.NonNull;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class NoteEditor extends Editor<NoteEditor.NoteEditorListener>
{
    private final NoteManager noteManager;

    private final Note note;

    private final PropertyUtil<String> titleProperty;
    private final PropertyUtil<String> textProperty;

    public NoteEditor(TimeService timeService, NoteManager noteManager, Note note)
    {
        super(timeService);

        this.noteManager = noteManager;
        this.note = note;
        this.note.edit();

        titleProperty = getTitleProperty(note);
        textProperty = getTextProperty(note);
    }

    public String getTitle()
    {
        return titleProperty.getValue();
    }

    public void setTitle(String title)
    {
        titleProperty.setValue(title);
    }

    public String getText()
    {
        return textProperty.getValue();
    }

    public void setText(String text)
    {
        textProperty.setValue(text);
    }

    public void save()
    {
        note.save();

        if (!noteManager.containsNote(note))
        {
            noteManager.addNote(note);
        }
    }

    public void revert()
    {
        note.revert();
    }

    public void remove()
    {
        if (noteManager.containsNote(note))
        {
            noteManager.removeNote(note);
        }
    }

    private @NonNull PropertyUtil<String> getTitleProperty(Note note)
    {
        return new PropertyUtil<>(
            note::getTitle,
            note::setTitle,
            v -> notifyListeners(NoteEditorListener::titleChanged));
    }

    private @NonNull PropertyUtil<String> getTextProperty(Note note)
    {
        return new PropertyUtil<>(
            note::getText,
            note::setText,
            v -> notifyListeners(NoteEditorListener::textChanged));
    }

    public interface NoteEditorListener
    {
        void titleChanged();

        void textChanged();
    }
}
