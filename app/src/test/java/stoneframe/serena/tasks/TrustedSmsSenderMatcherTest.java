package stoneframe.serena.tasks;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TrustedSmsSenderMatcherTest
{
    private final TrustedSmsSender ada = new TrustedSmsSender("+46701234567", "Ada");

    @Test
    public void findMatch_localSwedishNumber_matchesInternationalTrustedNumber()
    {
        assertSame(ada, find("070 123 45 67", Collections.singletonList(ada), "SE"));
    }

    @Test
    public void findMatch_internationalNumberWithoutCountryIso_matchesTrustedNumber()
    {
        assertSame(ada, find("+46 70 123 45 67", Collections.singletonList(ada), null));
    }

    @Test
    public void findMatch_formattedInternationalNumberWithoutCountryIso_matchesTrustedNumber()
    {
        assertSame(ada, find("(+46) 70 123 45 67", Collections.singletonList(ada), null));
    }

    @Test
    public void findMatch_nationalNumberWithoutCountryIso_returnsNull()
    {
        assertNull(find("0701234567", Collections.singletonList(ada), null));
    }

    @Test
    public void findMatch_nullOriginatingNumber_returnsNull()
    {
        assertNull(find(null, Collections.singletonList(ada), "SE"));
    }

    @Test
    public void findMatch_trustedSenderWithNullContactName_matchesNumber()
    {
        TrustedSmsSender sender = new TrustedSmsSender("+46701234567", null);

        assertSame(sender, find("+46701234567", Collections.singletonList(sender), "SE"));
    }

    @Test
    public void findMatch_missingOrUnsupportedCountryIso_onlyMatchesInternationalNumbers()
    {
        for (String countryIso : Arrays.asList(null, "", "  ", "XX", "ZZ"))
        {
            assertSame(ada, find("(+46) 70 123 45 67", Collections.singletonList(ada), countryIso));
            assertNull(find("0701234567", Collections.singletonList(ada), countryIso));
            assertNull(find("0046701234567", Collections.singletonList(ada), countryIso));
            assertNull(find("+46701234567", Collections.singletonList(
                new TrustedSmsSender("0701234567", "Ada")), countryIso));
        }
    }

    @Test
    public void findMatch_paddedLowercaseCountryIso_matchesNationalNumbers()
    {
        assertSame(ada, find("0701234567", Collections.singletonList(ada), " se "));
    }

    @Test
    public void findMatch_differentFullNumberWithSameSuffix_returnsNull()
    {
        assertNull(find("+46711234567", Collections.singletonList(ada), "SE"));
    }

    @Test
    public void findMatch_invalidOrAlphabeticNumber_returnsNull()
    {
        assertNull(find("not a phone number", Collections.singletonList(ada), "SE"));
        assertNull(find("+999", Collections.singletonList(ada), "SE"));
        assertNull(find("+46701234567#", Collections.singletonList(ada), "SE"));
    }

    @Test
    public void findMatch_numberWithExtension_returnsNull()
    {
        assertNull(find("0701234567 ext 8", Collections.singletonList(ada), "SE"));
    }

    @Test
    public void findMatch_multipleTrustedEntriesNormalizeToSameNumber_returnsFirstMatch()
    {
        TrustedSmsSender duplicate = new TrustedSmsSender("070 123 45 67", "Bea");

        assertSame(ada, find("+46701234567", Arrays.asList(ada, duplicate), "SE"));
        assertSame(duplicate, find("+46701234567", Arrays.asList(duplicate, ada), "SE"));
    }

    private static TrustedSmsSender find(String origin, List<TrustedSmsSender> senders, String countryIso)
    {
        return TrustedSmsSenderMatcher.findMatch(origin, senders, countryIso);
    }
}
