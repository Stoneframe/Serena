package stoneframe.serena.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.List;
import java.util.Locale;

public final class TrustedSmsSenderMatcher
{
    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();
    private static final String UNKNOWN_REGION = "ZZ";

    /**
     * Trusted senders must contain non-null entries with non-null phone numbers.
     */
    @Nullable
    public static TrustedSmsSender findMatch(
        @Nullable String originatingNumber,
        @NonNull List<TrustedSmsSender> trustedSenders,
        @Nullable String countryIso)
    {
        String normalizedOrigin = originatingNumber == null ? null : normalize(originatingNumber, countryIso);

        if (normalizedOrigin == null)
        {
            return null;
        }

        return findFirstMatch(normalizedOrigin, trustedSenders, countryIso);
    }

    @Nullable
    private static TrustedSmsSender findFirstMatch(
        @NonNull String normalizedOrigin,
        @NonNull List<TrustedSmsSender> trustedSenders,
        @Nullable String countryIso)
    {
        for (TrustedSmsSender sender : trustedSenders)
        {
            if (matches(sender, normalizedOrigin, countryIso))
            {
                return sender;
            }
        }

        return null;
    }

    private static boolean matches(
        @NonNull TrustedSmsSender sender,
        @NonNull String normalizedOrigin,
        @Nullable String countryIso)
    {
        return normalizedOrigin.equals(normalize(sender.getPhoneNumber(), countryIso));
    }

    @Nullable
    private static String normalize(@NonNull String rawNumber, @Nullable String countryIso)
    {
        return normalizeNumber(rawNumber.trim(), countryIso);
    }

    @Nullable
    private static String normalizeNumber(@NonNull String number, @Nullable String countryIso)
    {
        if (!isValidNumberInput(number))
        {
            return null;
        }

        return normalizeWithRegion(number, getDefaultRegion(countryIso));
    }

    private static boolean isValidNumberInput(@NonNull String number)
    {
        return !number.isEmpty() && !hasUnsupportedCharacters(number);
    }

    @Nullable
    private static String normalizeWithRegion(@NonNull String number, @NonNull String region)
    {
        try
        {
            return parseAndFormat(number, region);
        }
        catch (NumberParseException e)
        {
            return null;
        }
    }

    @Nullable
    private static String parseAndFormat(@NonNull String number, @NonNull String region)
        throws NumberParseException
    {
        PhoneNumber parsedNumber = PHONE_NUMBER_UTIL.parse(number, region);

        if (parsedNumber.hasExtension() || !PHONE_NUMBER_UTIL.isValidNumber(parsedNumber))
        {
            return null;
        }

        return PHONE_NUMBER_UTIL.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

    @NonNull
    private static String getDefaultRegion(@Nullable String countryIso)
    {
        if (countryIso == null)
        {
            return UNKNOWN_REGION;
        }

        String region = countryIso.trim().toUpperCase(Locale.ROOT);

        if (PHONE_NUMBER_UTIL.getSupportedRegions().contains(region))
        {
            return region;
        }

        return UNKNOWN_REGION;
    }

    private static boolean hasUnsupportedCharacters(@NonNull String number)
    {
        PhoneNumberCharacterValidator validator = new PhoneNumberCharacterValidator();
        for (int i = 0; i < number.length(); i++)
        {
            if (!validator.isAllowed(number.charAt(i)))
            {
                return true;
            }
        }

        return false;
    }

    private static final class PhoneNumberCharacterValidator
    {
        private boolean hasDigit;
        private boolean hasPlus;

        private boolean isAllowed(char character)
        {
            boolean isDigit = Character.isDigit(character);
            hasDigit |= isDigit;
            return isDigit || isFormattingCharacter(character) || acceptPlus(character);
        }

        private boolean acceptPlus(char character)
        {
            if (character != '+' || hasPlus || hasDigit)
            {
                return false;
            }

            hasPlus = true;
            return true;
        }

        private boolean isFormattingCharacter(char character)
        {
            return Character.isWhitespace(character)
                || Character.isSpaceChar(character)
                || "().-".indexOf(character) >= 0;
        }
    }
}
