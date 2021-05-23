package de.culture4life.luca.ui.qrcode;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

@Config(sdk = 28)
@RunWith(RobolectricTestRunner.class)
public class QrCodeViewModelTest {

    private static final String WEB_APP_URL = "https://app.luca-app.de/webapp";
    private static final String SCANNER_ID = "81d9e1db-7050-4557-b1ca-a9e4fe899bd9";
    private static final String LUCA_DATA = "e30";
    private static final String CWA_URL_SUFFIX = "/CWA1/CAESLAgBEhNEb2xvcmVzIGN1bHBhIHV0IHNpGhNOb3N0cnVkIE5hbSBpZCBlbGlnGnYIARJggwLMzE153tQwAOf2MZoUXXfzWTdlSpfS99iZffmcmxOG9njSK4RTimFOFwDh6t0Tyw8XR01ugDYjtuKwjjuK49Oh83FWct6XpefPi9Skjxvvz53i9gaMmUEc96pbtoaAGhDL1rYQOi3Bh_YYps7XagWYIgcIARAIGIQF";

    private static final String CHECK_IN_URL = WEB_APP_URL + "/" + SCANNER_ID;
    private static final String CHECK_IN_URL_WITH_LUCA_DATA = CHECK_IN_URL + "#" + LUCA_DATA;
    private static final String CHECK_IN_URL_WITH_LUCA_AND_CWA_DATA = CHECK_IN_URL_WITH_LUCA_DATA + CWA_URL_SUFFIX;

    @Test
    public void getScannerIdFromUrl_validCheckInUrl_emitsUuid() {
        QrCodeViewModel.getScannerIdFromUrl(CHECK_IN_URL)
                .map(UUID::toString)
                .test()
                .assertValue(SCANNER_ID);
    }

    @Test
    public void getScannerIdFromUrl_validCheckInUrlWithLucaAndCwaData_emitsUuid() {
        QrCodeViewModel.getScannerIdFromUrl(CHECK_IN_URL_WITH_LUCA_AND_CWA_DATA)
                .map(UUID::toString)
                .test()
                .assertValue(SCANNER_ID);
    }

    @Test
    public void getEncodedAdditionalDataFromUrlIfAvailable_validCheckInUrl_completesEmpty() {
        QrCodeViewModel.getEncodedAdditionalDataFromUrlIfAvailable(CHECK_IN_URL)
                .test()
                .assertNoValues()
                .assertComplete();
    }

    @Test
    public void getEncodedAdditionalDataFromUrlIfAvailable_validCheckInUrlWithLucaData_emitsLucaData() {
        QrCodeViewModel.getEncodedAdditionalDataFromUrlIfAvailable(CHECK_IN_URL_WITH_LUCA_DATA)
                .test()
                .assertValue(LUCA_DATA);
    }

    @Test
    public void getEncodedAdditionalDataFromUrlIfAvailable_validCheckInUrlWithLucaAndCwaData_emitsLucaData() {
        QrCodeViewModel.getEncodedAdditionalDataFromUrlIfAvailable(CHECK_IN_URL_WITH_LUCA_AND_CWA_DATA)
                .test()
                .assertValue(LUCA_DATA);
    }

    @Test
    public void getEncodedAdditionalDataFromUrlIfAvailable_preparedUrl_doesNotCrash() {
        QrCodeViewModel.getEncodedAdditionalDataFromUrlIfAvailable(WEB_APP_URL + "/CWA1/#").blockingGet();
    }

}