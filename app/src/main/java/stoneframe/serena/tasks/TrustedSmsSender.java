package stoneframe.serena.tasks;

import androidx.annotation.NonNull;

public final class TrustedSmsSender
{
    private final String phoneNumber;
    private final String contactName;

    public TrustedSmsSender(@NonNull String phoneNumber, @NonNull String contactName)
    {
        this.phoneNumber = phoneNumber;
        this.contactName = contactName;
    }

    public @NonNull String getPhoneNumber()
    {
        return phoneNumber;
    }

    public @NonNull String getContactName()
    {
        return contactName;
    }
}
